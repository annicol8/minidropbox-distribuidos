package compartido;

public class Protocolo {
	public static final int PUERTO = 7777;
	
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
		
		if (!nombre.equals(nombre.trim())) return false;
		
		if (nombre.equals(".") || nombre.contains("..")) return false;
		
		if (nombre.contains("/") || nombre.contains("\\")) return false;
		
		if (nombre.endsWith(".tmp")) return false;
        
        // c < 32: caracteres de control (incluye \n y \r)
        // c > 255: writeBytes solo conserva 1 byte por carácter
        for (int i = 0; i < nombre.length(); i++) {
            char c = nombre.charAt(i);
            if (c < 32 || c > 255) {
                return false;
            }
        }
        return true;
	}
	
}