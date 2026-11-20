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
	
	// main de prueba
	public static void main(String[] args) {
		File carpeta = new File("carpetaPruebas");
		System.out.println("Ruta " + carpeta.getAbsolutePath());
				
		try {
			Map<String, InfoFichero> resultado = escanear(carpeta);
			for (InfoFichero info: resultado.values()) {
				System.out.println(info.toString());
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
