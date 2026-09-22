package classwork;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Broker implements edu.polytech.channels.Broker {

    private final String name;

    private final Map<Integer, Deque<RendezVous>> rendezVous;

    public Broker(String name) {
        this.name = name;
        this.rendezVous = new HashMap<>();

        BrokerManager.getInstance().add(this);
    }

    public String getName() {
        return name;
    }

    @Override
    public Channel accept(int port) {
        RendezVous rendezVous = reserveRendezVous(port, true);

        return rendezVous.awaitAccept();
    }

    @Override
    public Channel connect(String name, int port) {
        Broker broker = BrokerManager.getInstance().get(name);

        if (broker == null) {
            return null;
        }

        RendezVous rendezVous = broker.reserveRendezVous(port, false);

        return rendezVous.awaitConnect();
    }

    private RendezVous reserveRendezVous(int port, boolean accept) {

        synchronized (rendezVous) {

            Deque<RendezVous> queue =
                    rendezVous.computeIfAbsent(port, k -> new ArrayDeque<>());

            for (Iterator<RendezVous> iterator = queue.iterator(); iterator.hasNext();) {

                RendezVous rv = iterator.next();

                boolean reserved;

                if (accept) {
                    reserved = rv.tryReserveAccept();
                } else {
                    reserved = rv.tryReserveConnect();
                }

                if (reserved) {

                    if (rv.isEstablished()) {
                        iterator.remove();
                    }

                    return rv;
                }
            }

            RendezVous rv = new RendezVous();

            if (accept) {
                rv.tryReserveAccept();
            } else {
                rv.tryReserveConnect();
            }

            queue.addLast(rv);

            return rv;
        }
    }
}