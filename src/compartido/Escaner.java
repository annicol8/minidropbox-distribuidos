package compartido;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class Escaner {
	public static Map<String, InfoFichero> escanear(File carpeta) throws IOException {
		Map<String, InfoFichero> mapa = new TreeMap<>();
		File[] ficheros = carpeta.listFiles();
		if (ficheros == null) {
			throw new IOException("No se puede leer la carpeta " + carpeta);
		}
		
		for (File f : ficheros) {
			if (f.isFile() && !f.getName().endsWith(".tmp")) {
				mapa.put(f.getName(), new InfoFichero(f.getName(), f.length(), f.lastModified(), Hash.sha256(f)));
			}
		}
		return mapa;
	}
	
	private Escaner()  { }
	
	public static boolean nombreValido(String nombre) {
        return nombre != null && !nombre.isEmpty()
                && !nombre.contains("/") && !nombre.contains("\\")
                && !nombre.equals("..") && !nombre.equals(".")
                && !nombre.endsWith(".tmp")
                && nombre.chars().noneMatch(c -> c < 32); //evitar tener caracteres de control
    }
}
