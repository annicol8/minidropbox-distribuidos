package compartido;

public class Protocolo {
	public static final int PUERTO = 8777;
	
	public static final String HELLO = "HELLO";
	public static final String LIST = "LIST";
	public static final String PUT = "PUT";
	public static final String GET = "GET";
	public static final String BYE = "BYE";
	
	public static final String OK ="OK";
	public static final String ERROR = "ERROR";
	
	private Protocolo() { }  
	
	//Comprobamos que el nombre sea un fichero simple para no romper el protocolo
	public static boolean esNombreValido(String nombre) {
		if (nombre == null || nombre.isEmpty()) return false;
		
		String limpio = nombre.trim();
		if (!nombre.equals(limpio) || limpio.isEmpty()) return false;
		
		if (limpio.equals(".") || limpio.contains("..") || limpio.contains("/") || limpio.contains("\\")) return false;
		
		if (limpio.endsWith(".tmp")) return false;
        
        return true;
	}
	
}