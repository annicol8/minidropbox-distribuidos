package compartido;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Protocolo {
	public static final int PUERTO = 7777;
	private static final int MAX_TEXTO = 65535;
	
	public static final String HELLO = "HELLO";
	public static final String LIST = "LIST";
	public static final String PUT = "PUT";
	public static final String GET = "GET";
	public static final String BYE = "BYE";
	
	public static final String OK ="OK";
	public static final String ERROR = "ERROR";
	
	private Protocolo() { }  
	
	public static void enviarTexto(DataOutputStream out, String texto) throws IOException {
        byte[] bytes = texto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_TEXTO) {
            throw new IOException("Texto demasiado largo");
        }
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    public static String recibirTexto(DataInputStream in) throws IOException {
        int n = in.readInt();
        if (n < 0 || n > MAX_TEXTO) {
            throw new IOException("Longitud de texto no válida: " + n);
        }
        byte[] bytes = new byte[n];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
	
}
