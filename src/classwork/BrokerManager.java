package classwork;

import java.util.HashMap;

public class BrokerManager {

    private static BrokerManager instance = new BrokerManager();

    private HashMap<String, Broker> brokers;

    private BrokerManager() {
        brokers = new HashMap<>();
    }

    public static BrokerManager getInstance() {
        return instance;
    }

    public void add(Broker broker) {
        brokers.put(broker.getName(), broker);
    }

    public Broker get(String name) {
        return brokers.get(name);
    }
}