package classwork;

public abstract class Channel {
	
	/*
	 * Entrée : tableau de bytes "bytes", entiers "offset", "length"
	 * Sortie : Renvoie un entier 
	 * 
	 * 
	 */
	int read(byte[] bytes, int offset, int length);
	
	/*
	 * Entrée : tableau de bytes "bytes", entiers "offset", "length"
	 * Sortie : Renvoie un entier 
	 * 
	 * 
	 */
	int write(byte[] bytes, int offset, int length);
	
	/*
	 * Entrée : Rien
	 * Sortie : Rien
	 * 
	 * 
	 */
	void disconnect();
	
	/*
	 * Entrée : Rien
	 * Sortie : Rien
	 * 
	 * 
	 */
	boolean disconnected();
}
