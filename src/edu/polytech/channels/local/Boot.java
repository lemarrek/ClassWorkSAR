package edu.polytech.channels.local;

import edu.polytech.channels.Bootstrap;
import edu.polytech.channels.Broker;
import edu.polytech.channels.Task;

public class Boot implements Bootstrap {

    // Le singleton s'instancie proprement via getInstance() sans new explicite
    public Boot() {
        BrokerManager.getInstance();
    }

    @Override
    public Broker newBroker(String name) {
        return new CBroker(name);
    }

    @Override
    public Task newTask(Broker b, Runnable r, String name) {
        return new CTask(b, r, name);
    }
}