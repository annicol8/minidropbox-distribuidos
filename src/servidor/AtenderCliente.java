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
			DataInputStream in = new DataInputStream(s.getInputStream());
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
	        		}
	        		usuario = nombre;
	        		System.out.println("Sesion iniciada: " + usuario);
	        		Transferencia.escribirLinea(out, Protocolo.OK);
	                out.flush();
	                
	        	} else if (comando.equals(Protocolo.LIST)) {
	        		
	        		List<InfoFichero> lista = almacen.listar();
	        		Transferencia.escribirLinea(out, Protocolo.OK);
	        		Transferencia.escribirLinea(out,  Integer.toString(lista.size()));
	        		for (InfoFichero info :  lista) {
	        			info.escribir(out);
	        		}
	        		out.flush();
	        		
	        	} else if (comando.equals(Protocolo.BYE)) {
	        		seguir = false;
	        		
	        	} else {
                    enviarError(out, "Comando desconocido: " + comando);
                }
	        }
	        System.out.println("[" + usuario + "] se ha desconectado");
	        
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
