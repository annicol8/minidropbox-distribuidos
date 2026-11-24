package compartido;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Escaner {
	
	private Escaner() { }
	
	public static Map<String, InfoFichero> escanearCarpeta(File carpeta) throws IOException {
		Map<String, InfoFichero> mapa = new HashMap<>();
		
		if(carpeta == null) return mapa;
		if (!carpeta.exists()) carpeta.mkdirs();
		if(!carpeta.isDirectory()) {
			throw new IOException("La ruta indicada no es un directorio: " + carpeta.getAbsolutePath());
		}
		
		File[] ficheros = carpeta.listFiles();
		if (ficheros == null) {
			throw new IOException("No se puede leer la carpeta " + carpeta);
		}
		
		for (int i = 0; i < ficheros.length; i++) {
            File f = ficheros[i];
            if (f.isFile() && Protocolo.esNombreValido(f.getName())) {
                String hash = Hash.sha256(f);
                mapa.put(f.getName(), new InfoFichero(f.getName(), f.length(), f.lastModified(), hash));
            }
        }
        return mapa;
	}
	
}