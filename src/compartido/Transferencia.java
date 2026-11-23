package compartido;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class Transferencia {
	private static final int TAM_BUFFER = 8192;
	
	private Transferencia(){ }
	
	//escribir linea terminada en \n
	public static void escribirLinea(DataOutputStream out, String texto) throws IOException {
        out.writeBytes(texto + "\n");
    }
	
	public static String leerLinea(DataInputStream in) throws IOException {
        String linea = in.readLine();
        if (linea == null) {
            throw new EOFException("La otra parte cerró la conexión");
        }
        return linea;
    }
	
	public static long leerLong(DataInputStream in) throws IOException {
        String linea = leerLinea(in);
        try {
            return Long.parseLong(linea.trim());
        } catch (NumberFormatException e) {
            throw new IOException("Se esperaba un número y llegó: " + linea, e);
        }
    }
	
	
	public static void enviarFichero(File origen, DataOutputStream out) throws IOException{
		long tamano = origen.length();
		escribirLinea(out, Long.toString(tamano));
		
		try(FileInputStream in = new FileInputStream(origen)){
			byte[] buffer = new byte[TAM_BUFFER];
			long restantes = tamano;
			
			while (restantes > 0) {
				int aLeer = (int) Math.min(buffer.length, restantes);
				int leidos = in.read(buffer, 0, aLeer);
				
				if(leidos == -1) {
					throw new EOFException("Fin de fichero inesperado al leer " + origen.getName());
				}
				
				out.write(buffer, 0, leidos);
				restantes -= leidos;
			}
		}
		out.flush();
	}
	
	public static void recibirFichero(DataInputStream in, File destino) throws IOException{
		long tamano = leerLong(in);
		if (tamano < 0) {
			throw new IOException("Tamaño de fichero no válido: " + tamano);
		}
		
		try (FileOutputStream out = new FileOutputStream(destino)) {
			byte[] buffer = new byte[TAM_BUFFER];
			long restantes = tamano;
			
			while (restantes > 0) {
				int aLeer = (int) Math.min(buffer.length, restantes);
				int leidos = in.read(buffer, 0, aLeer);
				
				if(leidos == -1) {
					throw new EOFException("Conexión cerrada antes de recibir todos los datos de " + destino.getName());
				}
				
				out.write(buffer, 0, leidos);
				restantes -= leidos;
			}
		}
	}

}
