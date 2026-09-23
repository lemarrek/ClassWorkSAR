package edu.polytech.channels.local;

import java.util.HashMap;
import java.util.Map;

public class BrokerManager {

    private static final BrokerManager INSTANCE = new BrokerManager();
    private final Map<String, CBroker> brokers;

    private BrokerManager() {
        brokers = new HashMap<>();
    }

    public static BrokerManager getInstance() {
        return INSTANCE;
    }

    public synchronized void addBroker(CBroker broker) {
        brokers.put(broker.getName(), broker);
    }

    public synchronized CBroker getBroker(String name) {
        return brokers.get(name);
    }
}