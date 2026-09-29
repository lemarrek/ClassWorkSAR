package edu.polytech.queues.local;

import edu.polytech.queues.Bootstrap;
import edu.polytech.queues.Task;
import edu.polytech.queues.local.Executor;

public class Boot implements Bootstrap {

    public Boot() {
        // Le constructeur par défaut garantit l'instanciation 
        // et le démarrage du thread de l'Executor (Event Pump)
        Executor.self();
    }

    @Override
    public Task newTask(Runnable r, String name) {
        // Utilise la méthode de l'Executor pour créer la tâche
        Task task = Executor.self().newTask(name);
        
        // Poste le traitement initial dans la file d'événements de la tâche
        task.post(r);
        
        return task;
    }
}