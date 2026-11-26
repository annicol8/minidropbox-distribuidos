package servidor;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import compartido.*;


public class AlmacenServidor {
	
	private final File carpeta;
	private final Map<String, InfoFichero> indice = new ConcurrentHashMap<>();
	//indice en memoria para consultar los fichero que hay más rapidamente
	private final Object lock = new Object();
	
	public AlmacenServidor(File carpeta) throws IOException {
		this.carpeta = carpeta;
		if (!carpeta.exists()) {
			carpeta.mkdirs();
		}
		borrarTemporales();
		indice.putAll(Escaner.escanearCarpeta(carpeta)); 
	}
	
	private void borrarTemporales() {
		File[] ficheros = carpeta.listFiles();
		if (ficheros == null) {
			return;
		}
		
		for (File f: ficheros) {
			if (f.isFile() && f.getName().endsWith(".tmp")) {
				if (!f.delete()) {
					System.err.println("No se pudo borrar el temporal " + f.getName());
				}
			}
		}
	} 
	
	public List<InfoFichero> listar() {
		return new ArrayList<>(indice.values());
	}
	
	public InfoFichero obtener(String nombre) {
		return indice.get(nombre);
	}
	
	public File getFichero(String nombre) {
		return new File(carpeta, nombre);
	}
	
	public File crearTemporal(String nombre) {
		return new File(carpeta, nombre + "." + Thread.currentThread().getName() + ".tmp");
	}
	
	// convierte el temporal en el fichero definitivo. Devuelve false si ya hay una versión igual o más actual
	public boolean confirmarSubida(String nombre, File tmp, long fecha) throws IOException {
		String hash = Hash.sha256(tmp);
		long tam = tmp.length();
		
		if (!tmp.setLastModified(fecha)) {
			throw new IOException("No se pudo ajustar la fecha de " + nombre);
		}
		
		File destino = getFichero(nombre);
		
		synchronized(lock) {
			InfoFichero actual = indice.get(nombre);
			if (actual != null && actual.getFecha() >= fecha) {
				return false;
			}
			
			if (destino.exists() && !destino.delete()) {
				throw new IOException("No se pudo reemplazar " + nombre + " (¿en uso?)");
            }
			if (!tmp.renameTo(destino)) {
				indice.remove(nombre);
				throw new IOException("No se pudo renombrar el temporal de " + nombre);
            }
			
			indice.put(nombre, new InfoFichero(nombre, tam, fecha, hash));
		}
		return true;
	}
	
 }
