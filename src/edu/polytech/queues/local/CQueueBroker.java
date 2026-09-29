package edu.polytech.queues.local;

import java.util.HashMap;
import java.util.Map;

import edu.polytech.queues.QueueBroker;
import edu.polytech.queues.Task;

public class CQueueBroker implements QueueBroker {

    private final String name;
    private final Task task;
    private final Map<Integer, BindListener> bindings;

    private static final Map<String, CQueueBroker> registry = new HashMap<>();

    public CQueueBroker(String name) {
        this.name = name;
        this.task = Executor.task();
        this.bindings = new HashMap<>();

        Executor.self().set(this.task, this);

        synchronized (registry) {
            if (registry.containsKey(name)) {
                throw new IllegalStateException("Broker name already used: " + name);
            }
            registry.put(name, this);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public boolean bind(int port, BindListener listener) {
        if (listener == null) {
            return false;
        }

        task.post(() -> {
            if (!bindings.containsKey(port)) {
                bindings.put(port, listener);
            }
        });

        return true;
    }

    @Override
    public boolean unbind(int port) {
        task.post(() -> {
            BindListener listener = bindings.remove(port);
            if (listener != null) {
                listener.unbound();
            }
        });

        return true;
    }

    @Override
    public boolean connect(String name, int port, ConnectListener listener) {
        CQueueBroker target;

        synchronized (registry) {
            target = registry.get(name);
        }

        if (target == null) {
            return false;
        }

        Task clientTask = Executor.task();

        target.task.post(() -> {
            BindListener bindListener = target.bindings.get(port);

            if (bindListener == null) {
                clientTask.post(listener::refused);
                return;
            }

            CMessageQueue[] pair = CMessageQueue.createPair(this, target);

            bindListener.accepted(pair[1]);
            clientTask.post(() -> listener.connected(pair[0]));
        });

        return true;
    }
}