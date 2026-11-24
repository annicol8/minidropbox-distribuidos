package servidor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import compartido.*;


public class AlmacenServidor {
	
	private final File carpeta;
	private final Map<String, InfoFichero> indice = new ConcurrentHashMap<>();
	//indice en memoria para consultar los fichero que hay más rapidamente
	
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
 }
