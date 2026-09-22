package classwork;

public class Channel implements edu.polytech.channels.Channel {

    private static final int BUFFER_SIZE = 1024;

    private final Buffer input;
    private final Buffer output;
    private final State state;

    private Channel(Buffer input, Buffer output, State state) {
        this.input = input;
        this.output = output;
        this.state = state;
    }

    public static Channel[] createPair() {

        Buffer aToB = new Buffer(BUFFER_SIZE);
        Buffer bToA = new Buffer(BUFFER_SIZE);

        State state = new State();

        Channel a = new Channel(bToA, aToB, state);
        Channel b = new Channel(aToB, bToA, state);

        return new Channel[]{a, b};
    }

    @Override
    public int read(byte[] bytes, int offset, int length) {

        if (bytes == null) {
            throw new IllegalArgumentException();
        }

        if (offset < 0 || length < 0 || offset + length > bytes.length) {
            throw new IndexOutOfBoundsException();
        }

        if (length == 0) {
            return 0;
        }

        return input.read(bytes, offset, length, state);
    }

    @Override
    public int write(byte[] bytes, int offset, int length) {

        if (bytes == null) {
            throw new IllegalArgumentException();
        }

        if (offset < 0 || length < 0 || offset + length > bytes.length) {
            throw new IndexOutOfBoundsException();
        }

        if (length == 0) {
            return 0;
        }

        return output.write(bytes, offset, length, state);
    }

    @Override
    public void disconnect() {

        synchronized (state) {

            if (state.disconnected) {
                return;
            }

            state.disconnected = true;

            input.wakeUp();
            output.wakeUp();
        }
    }

    @Override
    public boolean disconnected() {

        synchronized (state) {
            return state.disconnected;
        }
    }

    private static class State {

        private boolean disconnected;
    }

    private static class Buffer {

        private final byte[] data;

        private int readIndex;
        private int writeIndex;
        private int size;

        Buffer(int capacity) {
            data = new byte[capacity];
        }

        synchronized int read(byte[] destination,
                              int offset,
                              int length,
                              State state) {

            boolean interrupted = false;

            while (size == 0) {

                synchronized (state) {
                    if (state.disconnected) {
                        return -1;
                    }
                }

                try {
                    wait();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }

            if (interrupted) {
                Thread.currentThread().interrupt();
            }

            int count = Math.min(length, size);

            for (int i = 0; i < count; i++) {
                destination[offset + i] = data[readIndex];

                readIndex++;
                if (readIndex == data.length) {
                    readIndex = 0;
                }
            }

            size -= count;

            notifyAll();

            return count;
        }

        synchronized int write(byte[] source,
                               int offset,
                               int length,
                               State state) {

            int written = 0;

            while (written < length) {

                synchronized (state) {
                    if (state.disconnected) {
                        return written;
                    }
                }

                while (size == data.length) {

                    synchronized (state) {
                        if (state.disconnected) {
                            return written;
                        }
                    }

                    boolean interrupted = false;

                    try {
                        wait();
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }

                    if (interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }

                synchronized (state) {
                    if (state.disconnected) {
                        return written;
                    }
                }

                data[writeIndex] = source[offset + written];

                writeIndex++;
                if (writeIndex == data.length) {
                    writeIndex = 0;
                }

                size++;
                written++;

                notifyAll();
            }

            return written;
        }

        synchronized void wakeUp() {
            notifyAll();
        }
    }
}