package edu.polytech.queues.local;

import edu.polytech.queues.MessageQueue;
import edu.polytech.queues.QueueBroker;
import edu.polytech.queues.Task;

public class CMessageQueue implements MessageQueue {

    private final CQueueBroker broker;
    private CMessageQueue peer;
    private Listener listener;
    private Task listenerTask;
    private boolean closed;

    private CMessageQueue(CQueueBroker broker) {
        this.broker = broker;
        this.closed = false;
        Executor.self().register(broker.getTask(), this);
    }

    public static CMessageQueue[] createPair(CQueueBroker clientBroker, CQueueBroker serverBroker) {
        CMessageQueue clientQueue = new CMessageQueue(clientBroker);
        CMessageQueue serverQueue = new CMessageQueue(serverBroker);

        clientQueue.peer = serverQueue;
        serverQueue.peer = clientQueue;

        return new CMessageQueue[] { clientQueue, serverQueue };
    }

    @Override
    public QueueBroker broker() {
        return broker;
    }

    @Override
    public void setListener(Listener l) {
        listener = l;
        listenerTask = Executor.task();
    }

    @Override
    public boolean send(byte[] bytes, int offset, int length, SendListener l) {
        if (bytes == null || offset < 0 || length < 0 || offset > bytes.length - length) {
            throw new IllegalArgumentException();
        }

        Task callerTask = Executor.task();

        if (closed) {
            if (l != null) {
                callerTask.post(() -> l.sent(bytes, offset, length));
            }
            return false;
        }

        byte[] payload = new byte[length];
        System.arraycopy(bytes, offset, payload, 0, length);

        if (l != null) {
            callerTask.post(() -> l.sent(bytes, offset, length));
        }

        peer.broker().getTask().post(() -> peer.receive(payload));
        return true;
    }

    private void receive(byte[] msg) {
        if (closed || listener == null || listenerTask == null) {
            return;
        }

        Listener l = listener;
        listenerTask.post(() -> l.received(msg));
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;

        if (listener != null && listenerTask != null) {
            Listener l = listener;
            listenerTask.post(l::closed);
        }

        peer.broker().getTask().post(peer::handleRemoteClose);
    }

    private void handleRemoteClose() {
        if (closed) {
            return;
        }

        closed = true;

        if (listener != null && listenerTask != null) {
            Listener l = listener;
            listenerTask.post(l::closed);
        }
    }

    @Override
    public boolean closed() {
        return closed;
    }
}