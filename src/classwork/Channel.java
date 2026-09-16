package classwork;

public abstract class Channel {
	
	/** 
	       [ Task A ] ---> [ Canal (Flux d'octets) ] --->  [ Task B ]
		       |                  (Synchronisé)                 |
		  Écriture (write)                               Lecture (read)
		       | <---------------- Bidirectionnel ------------> |
	*/
	
	/**
	 * Reçoit un tableau bytes de longueur "length", et commence à y écrire 
	 * les données lues depuis le canal à partir de l'indice "offset".
	 * Cette méthode est bloquante si le canal est vide (attend des données).
	 * 
	 * @param bytes  le tableau de destination pour les données lues
	 * @param offset l'indice de départ dans le tableau
	 * @param length le nombre maximum de caractères (octets) à lire
	 * @return un entier correspondant au nombre de caractères effectivement lus, 
	 * ou -1 si le canal a été déconnecté
	 */
	int read(byte[] bytes, int offset, int length) {
		return 0;
	}
	
	/**
	 * Reçoit un tableau bytes, commence à lire ses données à partir
	 * de l'indice "offset" sur une longueur "length", et les écrit dans le canal.
	 * Cette méthode est bloquante si le canal de destination est plein (attend de la place).
	 * 
	 * @param bytes  le tableau source contenant les données à envoyer
	 * @param offset l'indice de départ dans le tableau
	 * @param length le nombre de caractères (octets) à écrire
	 * @return un entier correspondant au nombre de caractères effectivement écrits
	 */
	int write(byte[] bytes, int offset, int length) {
		return 0;
	}
	
	/**
	 * Se déconnecte du channel de communication dès qu'invoqué.
	 * Réveille également les tâches qui seraient bloquées dans un read() ou write().
	 * 
	 * Finir de read tous les write effectués avant le disconnect
	 */
	void disconnect() {
		
	}
	
	/**
	 * Renvoie un booléen qui indique si le channel est déconnecté ou non.
	 * 
	 * @return true si le channel est déconnecté, false s'il est toujours actif
	 */
	boolean disconnected() {
		return false;
	}
}





