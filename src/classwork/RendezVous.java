package classwork;

public class RendezVous {

    private boolean acceptReserved;
    private boolean connectReserved;

    private Channel acceptChannel;
    private Channel connectChannel;

    private boolean established;

    public RendezVous() {
        acceptReserved = false;
        connectReserved = false;
        established = false;
    }

    public synchronized boolean tryReserveAccept() {
        if (acceptReserved) {
            return false;
        }

        acceptReserved = true;

        if (connectReserved) {
            establish();
        }

        return true;
    }

    public synchronized boolean tryReserveConnect() {
        if (connectReserved) {
            return false;
        }

        connectReserved = true;

        if (acceptReserved) {
            establish();
        }

        return true;
    }

    private void establish() {
        if (established) {
            return;
        }

        Channel[] channels = Channel.createPair();

        acceptChannel = channels[0];
        connectChannel = channels[1];

        established = true;

        notifyAll();
    }

    public synchronized boolean isEstablished() {
        return established;
    }

    public synchronized Channel awaitAccept() {
        waitUntilEstablished();
        return acceptChannel;
    }

    public synchronized Channel awaitConnect() {
        waitUntilEstablished();
        return connectChannel;
    }

    private void waitUntilEstablished() {
        boolean interrupted = false;

        while (!established) {
            try {
                wait();
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}