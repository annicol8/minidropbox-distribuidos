package compartido;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Transferencia {
    private static final int TAM_BUFFER = 8192;

    private Transferencia() { }

    public static void enviarFichero(File origen, DataOutputStream out) throws IOException {
        long tamano = origen.length();
        out.writeLong(tamano);

        try (InputStream in = new FileInputStream(origen)) {
            byte[] buffer = new byte[TAM_BUFFER];
            long restantes = tamano;

            while (restantes > 0) {
                int aLeer = (int) Math.min(buffer.length, restantes);
                int leidos = in.read(buffer, 0, aLeer);

                if (leidos == -1) {
                    throw new EOFException("Fin de fichero inesperado al leer " + origen.getName());
                }

                out.write(buffer, 0, leidos);
                restantes -= leidos;
            }
        }
        out.flush();
    }

    public static void recibirFichero(DataInputStream in, File destino, long tamano) throws IOException {
        if (tamano < 0) {
            throw new IOException("Tamaño negativo");
        }
        try (OutputStream out = new FileOutputStream(destino)) {
            byte[] buffer = new byte[TAM_BUFFER];
            long restantes = tamano;

            while (restantes > 0) {
                int aLeer = (int) Math.min(buffer.length, restantes);
                int leidos = in.read(buffer, 0, aLeer);

                if (leidos == -1) {
                    throw new EOFException("Conexión cerrada antes de recibir todos los datos de " + destino.getName());
                }

                out.write(buffer, 0, leidos);
                restantes -= leidos;
            }
        }
    }
}
