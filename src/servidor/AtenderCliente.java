package servidor;

import compartido.*;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class AtenderCliente implements Runnable{
	
	private final Socket socket;
	private final AlmacenServidor almacen;
	private String usuario = "user";
	
	public AtenderCliente(Socket s, AlmacenServidor a) {
		this.socket = s;
		this.almacen = a;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try (Socket s = socket;
			DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
	        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream())) ) 
		{
	        System.out.println("Conexión desde " + s.getInetAddress().getHostAddress() + ":" + s.getPort());
	       
	        boolean seguir = true;
	        while(seguir) {
	        	String comando = Transferencia.leerLinea(in);
	        	
	        	if (comando.equals(Protocolo.HELLO)) {
	        		
	        		String nombre = Transferencia.leerLinea(in);
	        		if (nombre.trim().isEmpty()) {
	        			enviarError(out, "Nombre de usuario vacío");
	        		} else {
	        			usuario = nombre;
		        		System.out.println("Sesion iniciada: " + usuario);
		        		Transferencia.escribirLinea(out, Protocolo.OK);
		                out.flush();
	        		}
	        		
	                
	        	} else if (comando.equals(Protocolo.LIST)) {
	        		
	        		List<InfoFichero> lista = almacen.listar();
	        		Transferencia.escribirLinea(out, Protocolo.OK);
	        		Transferencia.escribirLinea(out,  Integer.toString(lista.size()));
	        		for (InfoFichero info :  lista) {
	        			info.escribir(out);
	        		}
	        		out.flush();
	        		
	        	} else if (comando.equals(Protocolo.PUT)) {
	        		String nombre = Transferencia.leerLinea(in);
	        		long fecha = Transferencia.leerLong(in);
	        		if (fecha < 0 ) {
	        			enviarError(out, "Fecha no válida");
	        			continue;
	        		}
	        		
	        		if (!Protocolo.esNombreValido(nombre)) {
	                    enviarError(out, "Nombre de fichero no válido");
	                    continue;
	                }
	        		
	        		InfoFichero actual = almacen.obtener(nombre);
	        		if (actual != null && actual.getFecha() >= fecha) {
	        			enviarError(out, "El servidor ya tiene una versión igual o más reciente");
	                    continue;
	        		}
	        		Transferencia.escribirLinea(out, Protocolo.OK);
	        		out.flush();
	        		
	        		File tmp = almacen.crearTemporal(nombre);
	        		boolean guardado;
	        		try {
	        			Transferencia.recibirFichero(in, tmp);
	        	        guardado = almacen.confirmarSubida(nombre, tmp, fecha);
	        	    } finally {
	        	    	if (tmp.exists() && !tmp.delete()) {
	        	    		System.err.println("No se pudo borrar el temporal " + tmp.getName());
	        	    	}
	        		}
	        		
	        		if (guardado) {
	        			System.out.println("[" + usuario + "] subió " + nombre);
			            Transferencia.escribirLinea(out, Protocolo.OK);
			            out.flush();
	        		} else {
	        			enviarError(out, "Otro cliente subió antes una versión igual o más reciente");
	                }
	        		
	        	} else if (comando.equals(Protocolo.GET)) {
	        		String nombre = Transferencia.leerLinea(in);
	        		if (!Protocolo.esNombreValido(nombre)) {
	                    enviarError(out, "Nombre de fichero no válido");
	                    continue;
	                }
	        		
	        		InfoFichero info = almacen.obtener(nombre);
	        		if (info == null) {
	        			enviarError(out, "El fichero no existe: " + nombre);
	                     continue;
	        		}
	        		
	        		Transferencia.escribirLinea(out, Protocolo.OK);
	                Transferencia.escribirLinea(out, Long.toString(info.getFecha()));
	                Transferencia.enviarFichero(almacen.getFichero(nombre), out);
	                System.out.println("[" + usuario + "] descargó " + nombre);
	        		
	        	} else if (comando.equals(Protocolo.BYE)) {
	        		seguir = false;
	        		
	        	} else {
                    enviarError(out, "Comando desconocido: " + comando);
                }
	        }
	        System.out.println("[" + usuario + "] se ha desconectado");
	        
		} catch(EOFException e) {
			System.out.println("[" + usuario + "] cerró la conexión sin bye." );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	private void enviarError(DataOutputStream out, String motivo) throws IOException {
		Transferencia.escribirLinea(out, Protocolo.ERROR);
        Transferencia.escribirLinea(out, motivo);
        out.flush();
	}
	
}
