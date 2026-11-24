package servidor;

import compartido.Protocolo;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ServidorSync {
	
	private static final String CARPETA = "servidor_datos";
	
	public static void main(String[] args) {
		
		AlmacenServidor almacen;
		try {
			almacen = new AlmacenServidor(new File(CARPETA));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.err.println("No se pudo inicializar el almacén: ");
			e1.printStackTrace();
			return;
		}
		
		System.out.println("Almacén cargado: " + almacen.listar().size() + " ficheros en " + CARPETA);
		
		// un hilo por cliente conectado
		ExecutorService pool = Executors.newCachedThreadPool();
		try (ServerSocket ss = new ServerSocket(Protocolo.PUERTO)) {
			System.out.println("Servidor escuchando en el puerto " + Protocolo.PUERTO);
			while (true) {
				try {
					Socket s = ss.accept();
					pool.execute(new AtenderCliente(s, almacen));
				} catch (IOException e) {
                    e.printStackTrace();
                }
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			pool.shutdown();
		}
	}
}

