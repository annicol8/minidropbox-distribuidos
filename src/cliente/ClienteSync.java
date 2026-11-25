package cliente;

import compartido.InfoFichero;
import compartido.Protocolo;
import compartido.Transferencia;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class ClienteSync {
	private final File carpetaLocal;
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;

    public ClienteSync(String host, String usuario,File carpetaLocal) throws IOException {
        this.carpetaLocal = carpetaLocal;
        if (!carpetaLocal.exists() && !carpetaLocal.mkdirs()){
			throw new IOException("No se pudo crear la carpeta local: " + carpetaLocal.getAbsolutePath());
		}
		if (!carpetaLocal.isDirectory()){
			throw new IOException("La ruta indicada no es una carpeta: " + carpetaLocal.getAbsolutePath());
		}
		this.socket = new Socket(host, Protocolo.PUERTO);
		this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		try {
			saludar(usuario);
		} catch (IOException e) {
			try { socket.close(); } catch (IOException ignored) { }
			throw e;
		}
	}
	public File getCarpetaLocal(){
		return carpetaLocal;
	}

	//------------------ APOYO DE PROTOCOLO ------------------
	private void esperarOK() throws IOException {
		String respuesta = Transferencia.leerLinea(in);
		if (Protocolo.OK.equals(respuesta)){
			return;
		}
		if (Protocolo.ERROR.equals(respuesta)){
			throw new IOException(Transferencia.leerLinea(in));
		}
		throw new IOException("Respuesta inesperada del servidor: " + respuesta);
	}
	public static String pedirDato(BufferedReader teclado, String mensaje) throws IOException {
		String dato;
		do{
			System.out.println(mensaje);
			dato = teclado.readLine();
			if (dato == null){
				throw new IOException("Entrada cerrada");
			}
			dato = dato.trim();
		}while (dato.isEmpty());
		return dato;
	}

	//------------------ OPERACIONES DEL PROTOCOLO ------------------
	private void saludar(String usuario) throws IOException {
		Transferencia.escribirLinea(out, Protocolo.HELLO);
		Transferencia.escribirLinea(out, usuario);
		out.flush();
		esperarOK();
	}
	public synchronized List<InfoFichero> listar() throws IOException {
		Transferencia.escribirLinea(out, Protocolo.LIST);
		out.flush();
		esperarOK();

		int n = (int)Transferencia.leerLong(in);
		List<InfoFichero> lista = new ArrayList<>(n);
		for (int i = 0; i < n; i++){
			lista.add(InfoFichero.leer(in));
		}
		return lista;
	}

	public synchronized void subir(String nombre) throws IOException {
		if (!Protocolo.esNombreValido(nombre)){
			throw new IOException("Nombre de fichero no válido: " + nombre);
		}
		File ficheroLocal = new File(carpetaLocal, nombre);
		if (!ficheroLocal.isFile()){
			throw new IOException("No existe el fichero local: " + nombre);
		}

		// Petición PUT + nombre + fecha y validación
		Transferencia.escribirLinea(out, Protocolo.PUT);
		Transferencia.escribirLinea(out, nombre);
		Transferencia.escribirLinea(out, Long.toString(ficheroLocal.lastModified()));
		out.flush();
		esperarOK();
		// Transferencia del fichero y confirmación
		Transferencia.enviarFichero(ficheroLocal, out);
		esperarOK();
	}

	public synchronized void descargar(String nombre) throws IOException {
		if (!Protocolo.esNombreValido(nombre)){
			throw new IOException("Nombre del fichero no válido: " + nombre);
		}

		//Petición GET + nombre y validación
		Transferencia.escribirLinea(out, Protocolo.GET);
		Transferencia.escribirLinea(out, nombre);
		out.flush();
		esperarOK();

		//Lectura de fecha y recepción del fichero temporal .tmp
		long fechaServidor = Transferencia.leerLong(in);
		File temporal = new File(carpetaLocal, nombre + ".tmp");
		File destino = new File(carpetaLocal, nombre);
		try {
            Transferencia.recibirFichero(in, temporal);
			temporal.setLastModified(fechaServidor);
			Files.move(temporal.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
			destino.setLastModified(fechaServidor);
        }finally {
            if (temporal.exists()){
				temporal.delete();
			}
        }
	}

	public synchronized void cerrar(){
		try {
            Transferencia.escribirLinea(out, Protocolo.BYE);
			out.flush();
        } catch (IOException e) {
            //Si servidor ha cerrado la conexión, continuamos cerrando el socket
        }
		try {
            socket.close();
        } catch (IOException e) {
        }
    }

	//------------------ CONSOLA ------------------

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ClienteSync cliente = null;
		try (BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in))) {
            String host = pedirDato(teclado, "Servidor (ej. localhost): ");
			String usuario = pedirDato(teclado, "Usuario: ");
			String carpeta = pedirDato(teclado, "Carpeta local: ");

			cliente = new ClienteSync(host, usuario, new File(carpeta));
			System.out.println("Conectado. Comandos: list | put <fichero> | get <fichero> | sync | salir");

			String linea;
			while ((linea = teclado.readLine()) != null){
				linea = linea.trim();
				if (linea.isEmpty()){
					continue;
				}

				String[] partes = linea.split("\\s+", 2);
				String comando = partes[0].toLowerCase();

				try {
                    switch (comando){
						case "list":
							List<InfoFichero> lista = cliente.listar();
							if (lista.isEmpty()){
								System.out.println("No hay ficheros en el servidor");
							}else{
								System.out.println("---LISTA DE FICHEROS---");
								for (int i = 0; i < lista.size(); i++){
									System.out.println(lista.get(i));
								}
							}
							break;

						case "put":
							if (partes.length <2){
								System.out.println("El uso de put es: put <fichero>");
							}else{
								String nombrePut = partes[1].trim();
								cliente.subir(nombrePut);
								System.out.println("ÉXITO. Se ha subido el fichero: " + nombrePut);
							}
							break;

						case "get":
							if (partes.length <2){
								System.out.println("El uso de get es: get <fichero>");
							}else{
								String nombreGet = partes[1].trim();
								cliente.descargar(nombreGet);
								System.out.println("ÉXITO. Se ha descargado el fichero: " + nombreGet);
							}
							break;
						case "sync":
							System.out.println();
							break;
						case "salir":
							return;
                        default:
							System.out.println("Comando no reconocido");
					}
                } catch (IOException e) {
					System.out.println("Error: " + e.getMessage());
					if (cliente.socket.isClosed()) {
						break;
					}
                }
            }
        } catch (IOException e) {
			System.out.println("Error de conexión: " + e.getMessage());
		}finally {
			if (cliente != null) {
				cliente.cerrar();
				System.out.println("Desconectado.");
			}
        }

    }

}
