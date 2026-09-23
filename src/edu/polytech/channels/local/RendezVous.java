package edu.polytech.channels.local;

import java.util.LinkedList;
import java.util.Queue;

public class RendezVous {

    private int waitingAccepts = 0;
    private int waitingConnects = 0;
    
    private final Queue<CChannel> acceptedChannels = new LinkedList<>();
    private final Queue<CChannel> connectedChannels = new LinkedList<>();

    public synchronized CChannel accept() {
        if (waitingConnects > 0) {
            // Un client est déjà là, on crée la connexion et on le réveille
            waitingConnects--;
            CChannel[] pair = CChannel.createPair();
            connectedChannels.add(pair[1]);
            notifyAll(); 
            return pair[0];
        } else {
            // Aucun client, on attend
            waitingAccepts++;
            while (acceptedChannels.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return acceptedChannels.poll();
        }
    }

    public synchronized CChannel connect() {
        if (waitingAccepts > 0) {
            // Le serveur attend déjà, on se connecte
            waitingAccepts--;
            CChannel[] pair = CChannel.createPair();
            acceptedChannels.add(pair[0]);
            notifyAll();
            return pair[1];
        } else {
            // Le serveur n'est pas prêt, on attend
            waitingConnects++;
            while (connectedChannels.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return connectedChannels.poll();
        }
    }
}