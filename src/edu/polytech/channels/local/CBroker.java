package edu.polytech.channels.local;

import java.util.HashMap;
import java.util.Map;
import edu.polytech.channels.Channel;

public class CBroker implements edu.polytech.channels.Broker {

    private final String name;
    private final Map<Integer, RendezVous> ports;

    public CBroker(String name) {
        this.name = name;
        this.ports = new HashMap<>();
        BrokerManager.getInstance().addBroker(this);
    }

    public String getName() {
        return name;
    }

    private synchronized RendezVous getRendezVous(int port) {
        return ports.computeIfAbsent(port, k -> new RendezVous());
    }

    @Override
    public Channel accept(int port) {
        return getRendezVous(port).accept();
    }

    @Override
    public Channel connect(String name, int port) {
        CBroker target = BrokerManager.getInstance().getBroker(name);
        if (target == null) {
            return null;
        }
        return target.getRendezVous(port).connect();
    }
}