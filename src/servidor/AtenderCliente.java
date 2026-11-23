package servidor;

import compartido.*;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class AtenderCliente implements Runnable{
	private final Socket socket;
	private final AlmacenServidor almacen;
	
	public AtenderCliente(Socket s, AlmacenServidor a) {
		this.socket = s;
		this.almacen = a;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try (Socket s = socket;
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
	        PrintWriter out = new PrintWriter(s.getOutputStream(), true)) 
		{
			String usuario = "?";
			String comando;
			
			while ((comando = in.readLine()) != null)  {
				comando = comando.trim();
				if (comando.isEmpty()) {
					continue;
				}
				
				switch(comando) {
					case Protocolo.HELLO: 
						String nomUsuario = in.readLine();
						if (nomUsuario == null || nomUsuario.trim().isEmpty()) {
							enviarError(out, "Falta el nombre del usuario");
						} else {
							usuario = nomUsuario.trim();
							System.out.println("Conectado: " + usuario);
							out.println(Protocolo.OK);
						}
						break;
					
					case Protocolo.LIST:
                        List<InfoFichero> lista = almacen.listar();
                        out.println(Protocolo.OK);
                        out.println(Integer.toString(lista.size()));
                        for (InfoFichero f : lista) {
                            f.escribir(out);
                        }
                        break;
					
					case Protocolo.BYE:
						System.out.println("Desconectado: "+ usuario);
						return;
					
					default:
						enviarError(out, "Comando desconocido: " + comando);
						break;
				}
			}
			
			System.out.println("Cliente desconectado: "+ usuario);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error con el cliente: "+ e.getMessage());
		}		
	
	}
	
	private void enviarLinea(BufferedWriter out, String texto) throws IOException {
		out.write(texto);
		out.newLine();
		out.flush();
	}
	
	private void enviarError(PrintWriter out, String motivo) {
        out.println(Protocolo.ERROR);
        out.println(motivo);
    }
	
	
}
