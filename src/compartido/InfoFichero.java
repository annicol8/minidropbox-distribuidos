package compartido;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InfoFichero {

	private final String nombre;
	private final long tamano;
	private final long fecha;  //ultima Modificación en ms
	private final String hash; //sha-256 en hexadecimal
	
	public InfoFichero(String nombre, long tam, long fecha, String hash) {
		this.nombre = nombre;
		this.tamano = tam;
		this.fecha = fecha;
		this.hash = hash;
	}
	
	public String getNOmbre() { return nombre; }
	public long getTamano() { return tamano; }
	public long getFecha()    { return fecha; }
    public String getHash()   { return hash; }
    
    public void escribir(DataOutputStream out) throws IOException {
    	out.writeUTF(nombre);
        out.writeLong(tamano);
        out.writeLong(fecha);
        out.writeUTF(hash);
    }
    
    public static InfoFichero leer(DataInputStream in) throws IOException {
    	String nombre = in.readUTF();
        long tamano = in.readLong();
        long fecha = in.readLong();
        String hash = in.readUTF();
        return new InfoFichero(nombre, tamano, fecha, hash);
    }
    
    @Override
    public String toString() {
    	String fechaMostrada = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(fecha)); 
    	return nombre + " | " + tamano + " bytes | " + fechaMostrada + " | " + hash.substring(0,8) + "..." ;
    }
    
    
}
