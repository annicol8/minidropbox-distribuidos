package cliente;

import compartido.InfoFichero;
import compartido.Protocolo;
import compartido.Transferencia;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
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
		saludar(usuario);
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


	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
