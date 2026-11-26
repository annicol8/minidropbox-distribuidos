package compartido;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InfoFichero {

	private String nombre;
	private long tamano;
	private long fecha;  //ultima Modificación en ms
	private String hash; //sha-256 en hexadecimal
	
	public InfoFichero () { }
	
	public InfoFichero(String nombre, long tam, long fecha, String hash) {
		this.nombre = nombre;
		this.tamano = tam;
		this.fecha = fecha;
		this.hash = hash;
	}
	
	public String getNombre() { return nombre; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	
	public long getTamano() { return tamano; }
	public void setTamano(long tamano) { this.tamano = tamano; }

    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    
    public void escribir(DataOutputStream out) throws IOException {
    	Transferencia.escribirLinea(out, nombre);
        Transferencia.escribirLinea(out, Long.toString(tamano));
        Transferencia.escribirLinea(out, Long.toString(fecha));
        Transferencia.escribirLinea(out, hash);
    }
    
    public static InfoFichero leer(DataInputStream in) throws IOException {
    	String nombre = Transferencia.leerLinea(in);
        long tamano = Transferencia.leerLong(in);
        long fecha = Transferencia.leerLong(in);
        String hash = Transferencia.leerLinea(in);
        return new InfoFichero(nombre, tamano, fecha, hash);
    }
    
    @Override
    public String toString() {
    	String fechaMostrada = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(fecha)); 
    	String hashCorto = (hash != null && hash.length() >= 8) ? hash.substring(0, 8) + "..." : hash;
    	return nombre + " | " + tamano + " bytes | " + fechaMostrada + " | " + hashCorto ;
    }
    
    
}