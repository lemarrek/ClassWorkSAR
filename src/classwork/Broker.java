package classwork;

public abstract class Broker {
	
	/*
	 * Entrée : un String "name"
	 * Sortie : Construit un Broker avec le nom "name"
	 * C'est l'équivalent d'un ServerSocket. Chaque Task 
	 * possède un Broker identifié par un name.
	 */
	Broker(String name);
	
	/*
	 * Entrée : un entier port ex : "8080"
	 * Sortie : Renvoie une classe Channel
	 * Elle met la tâche en attente jusqu'à ce qu'une 
	 * autre tâche tente de se connecter à ce port.
	 * Elle retourne alors un Channel
	 */
	Channel accept(int port);
	
	/*
	 * Entrée : un String "name", un entier "port"
	 * Sortie : Renvoie une classe Channel
	 * Tente d'établir une connexion avec le Broker nommé 
	 * "name" sur le port spécifié. Si le Broker distant 
	 * est en train de faire un accept, la connexion réussit 
	 * et retourne le Channel correspondant.
	 */
	Channel connect(String name, int port);
}
