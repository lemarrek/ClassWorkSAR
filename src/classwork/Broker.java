package classwork;

public abstract class Broker {
	
	/**
	 * Construit un Broker avec le nom "name".
	 * C'est l'équivalent d'un ServerSocket. Chaque Task
	 * possède un Broker identifié par un name.
	 * 
	 * @param name le nom identifiant ce Broker
	 */
	Broker(String name) {
	}
	
	/**
	 * Met la tâche en attente (bloquante) jusqu'à ce qu'une 
	 * autre tâche tente de se connecter à ce port.
	 * Elle retourne alors un Channel.
	 * 
	 * @param port un entier représentant le port d'écoute (ex : 8080)
	 * @return une instance de Channel représentant la connexion établie
	 */
	Channel accept(int port) {
		return null;
	}
	
	/**
	 * Tente d'établir une connexion avec le Broker nommé 
	 * "name" sur le port spécifié. Cette méthode est bloquante :
	 * si le Broker distant est en train de faire un accept, 
	 * la connexion réussit et retourne le Channel correspondant.
	 * 
	 * @param name le nom du Broker distant auquel se connecter
	 * @param port le port sur lequel se connecter
	 * @return une instance de Channel correspondant à la connexion établie
	 */
	Channel connect(String name, int port) {
		return null;
	}
}





