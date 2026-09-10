package classwork;

public abstract class Channel {
	
	/*
	 * Entrée : 
	 * Sortie : 
	 * 
	 */
	int read(byte[] bytes, int offset, int length);
	
	/*
	 * Entrée : 
	 * Sortie : 
	 * 
	 */
	int write(byte[] bytes, int offset, int length);
	
	/*
	 * Entrée : 
	 * Sortie : 
	 * 
	 */
	void disconnect();
	
	/*
	 * Entrée : 
	 * Sortie : 
	 * 
	 */
	boolean disconnected();
}
