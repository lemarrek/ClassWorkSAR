package classwork;

public abstract class Task extends Thread {
	
	/**
	 * Construit une nouvelle tâche en associant un environnement 
	 * de communication (Broker) à un code à exécuter (Runnable).
	 * 
	 * @param b le Broker associé à cette tâche
	 * @param r le code que cette tâche va exécuter
	 */
	Task(Broker b, Runnable r) {
	}
	
	/**
	 * Permet au code actuellement exécuté par ce thread de récupérer 
	 * son propre Broker sans avoir à le passer en paramètre dans toutes les fonctions.
	 * 
	 * @return le Broker de la tâche courante
	 */
	static Broker getBroker() {
		return null;
	}
}