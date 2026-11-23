package servidor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import compartido.*;


public class AlmacenServidor {
	
	private final File carpeta;
	private final ConcurrentHashMap<String, InfoFichero> indice = new ConcurrentHashMap<>();
	//indice en memoria para consultar los fichero que hay más rapidamente
	
	public AlmacenServidor(File carpeta) throws IOException {
		this.carpeta = carpeta;
		carpeta.mkdirs();
		indice.putAll(Escaner.escanear(carpeta)); 
	}
	
	public static boolean nombreValido(String nombre) {
		return nombre != null && !nombre.isEmpty() 
				&& !nombre.contains("/") && !nombre.contains("\\")
	            && !nombre.equals("..") && !nombre.equals(".")
	            && !nombre.endsWith(".tmp");
	    }
	
	public File fichero(String nombre) {
		return new File(carpeta, nombre);
	}
	
	public File temporal(String nombre) {
		return new File(carpeta, nombre + "." + Thread.currentThread().getId() + ".tmp");
	}
	
	public InfoFichero get(String nombre) {
		return indice.get(nombre);
	}
	
	public List<InfoFichero> listar() {
		return new ArrayList<>(indice.values());
	}
	
	//se llama cunado el temporal esta completo: lo mueve a su sitio y actualiza el indice
	public void confirmar(String nombre, File temporal) throws IOException {
		InfoFichero info = new InfoFichero(nombre, temporal.length(), temporal.lastModified(), Hash.sha256(temporal));
		synchronized (this) { //mover y actualizar el indice de una sola vez
			Files.move(temporal.toPath(), fichero(nombre).toPath(), StandardCopyOption.REPLACE_EXISTING);
			indice.put(nombre, info);
			
		}
	}
 }
