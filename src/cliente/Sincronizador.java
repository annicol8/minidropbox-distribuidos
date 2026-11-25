package cliente;

import compartido.Escaner;
import compartido.InfoFichero;
import compartido.Protocolo;
import compartido.Transferencia;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sincronizador {
    private ClienteSync cliente;

    public Sincronizador(ClienteSync cliente){
        this.cliente = cliente;
    }


    //Compara el inventario local con el del servidor y ejecuta PUT / GET según las diferencias
    public synchronized void sincronizar() throws IOException {
        // Obtener archivos locales
        Map<String, InfoFichero> locales = Escaner.escanearCarpeta(cliente.getCarpetaLocal());

        // Obtener inventario del servidor
        List<InfoFichero> listaServidor = cliente.listar();
        Map<String, InfoFichero> remotos = new HashMap<>();
        for (int i = 0; i < listaServidor.size(); i++){
            InfoFichero info = listaServidor.get(i);
            remotos.put(info.getNombre(), info);
        }

        int subidos = 0;
        int descargados = 0;

        // Revisar ficheros locales (solo en cliente o presentes en ambos)
        for (Map.Entry<String, InfoFichero> entrada: locales.entrySet()){
            String nombre = entrada.getKey();
            InfoFichero local = entrada.getValue();
            InfoFichero remoto = remotos.get(nombre);

            if (remoto == null){ //Solo en cliente -> PUT
                cliente.subir(nombre);
                System.out.println("[SYNC] Subido (nuevo): " + nombre);
                subidos++;
            } else if (!local.getHash().equals(remoto.getHash())) { //En ambos con distinto contenido -> gana fecha más reciente
                if (local.getFecha() > remoto.getFecha()){
                    cliente.subir(nombre);
                    System.out.println("[SYNC] Subido (actualizado): " + nombre);
                    subidos++;
                } else if (remoto.getFecha() > local.getFecha()) {
                    cliente.descargar(nombre);
                    System.out.println("[SYNC] Descargado (actualizado): " + nombre);
                    descargados++;
                }
            }
        }

        // Revisar ficheros que solo están en el servidor -> GET
        for (Map.Entry<String, InfoFichero> entrada: remotos.entrySet()){
            String nombre = entrada.getKey();
            if (!locales.containsKey(nombre)){
                cliente.descargar(nombre);
                System.out.println("[SYNC] Descargado (nuevo): " + nombre);
                descargados++;
            }
        }

        if (subidos == 0 && descargados == 0){
            System.out.println("[SYNC] No hay nada que actualizar");
        }else{
            System.out.println("[SYNC] Sincronizacion completada: SUBIDOS -> " + subidos + " || DESCARGADOS -> "+ descargados);
        }
    }
}
