package edu.polytech.channels.local;

import edu.polytech.channels.Channel;
import edu.polytech.utils.CircularBuffer;

public class CChannel implements Channel {

    private static final int BUFFER_SIZE = 1024;

    private final Pipe input;
    private final Pipe output;
    private final State state;

    private CChannel(Pipe input, Pipe output, State state) {
        this.input = input;
        this.output = output;
        this.state = state;
    }

    public static CChannel[] createPair() {

        State state = new State();

        Pipe aToB = new Pipe(BUFFER_SIZE, state);
        Pipe bToA = new Pipe(BUFFER_SIZE, state);

        CChannel a = new CChannel(bToA, aToB, state);
        CChannel b = new CChannel(aToB, bToA, state);

        return new CChannel[]{a, b};
    }

    @Override
    public int read(byte[] bytes, int offset, int length) {

        if (bytes == null) {
            throw new IllegalArgumentException();
        }

        if (offset < 0 || length < 0 || offset > bytes.length - length) {
            throw new IndexOutOfBoundsException();
        }

        if (length == 0) {
            return 0;
        }

        return input.read(bytes, offset, length);
    }

    @Override
    public int write(byte[] bytes, int offset, int length) {

        if (bytes == null) {
            throw new IllegalArgumentException();
        }

        if (offset < 0 || length < 0 || offset > bytes.length - length) {
            throw new IndexOutOfBoundsException();
        }

        if (length == 0) {
            return 0;
        }

        return output.write(bytes, offset, length);
    }

    @Override
    public void disconnect() {
        state.disconnect();
    }

    @Override
    public boolean disconnected() {
        return state.isDisconnected();
    }

    private static class State {

        private boolean disconnected = false;

        synchronized void disconnect() {
            disconnected = true;
        }

        synchronized boolean isDisconnected() {
            return disconnected;
        }
    }

    private static class Pipe {

        private final CircularBuffer buffer;
        private final State state;

        Pipe(int capacity, State state) {
            this.buffer = new CircularBuffer(capacity);
            this.state = state;
        }

        synchronized void wakeUp() {
            notifyAll();
        }

        synchronized int read(byte[] bytes, int offset, int length) {

            if (length == 0) {
                return 0;
            }

            boolean interrupted = false;

            while (buffer.empty()) {

                if (state.isDisconnected()) {
                    return -1;
                }

                try {
                    wait();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }

            int readCount = 0;

            while (readCount < length && !buffer.empty()) {
                bytes[offset + readCount] = buffer.pull();
                readCount++;
            }

            notifyAll();

            if (interrupted) {
                Thread.currentThread().interrupt();
            }

            return readCount;
        }

        synchronized int write(byte[] bytes, int offset, int length) {

            if (length == 0) {
                return 0;
            }

            int written = 0;
            boolean interrupted = false;

            while (written < length) {

                if (state.isDisconnected()) {
                    break;
                }

                while (buffer.full()) {

                    if (state.isDisconnected()) {
                        if (interrupted) {
                            Thread.currentThread().interrupt();
                        }
                        return written;
                    }

                    try {
                        wait();
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }

                if (state.isDisconnected()) {
                    break;
                }

                while (written < length && !buffer.full()) {

                    if (state.isDisconnected()) {
                        break;
                    }

                    buffer.push(bytes[offset + written]);
                    written++;
                }

                notifyAll();
            }

            if (interrupted) {
                Thread.currentThread().interrupt();
            }

            return written;
        }
    }
}