package servidor;

import compartido.Protocolo;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ServidorSync {
	public static void main(String[] args) {
		ExecutorService pool = Executors.newCachedThreadPool();
		try (ServerSocket ss = new ServerSocket(Protocolo.PUERTO)) {
			AlmacenServidor almacen = new AlmacenServidor(new File("serverDatos"));
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

