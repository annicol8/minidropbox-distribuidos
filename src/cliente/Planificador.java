package cliente;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Planificador {
    private final Sincronizador sincronizador;
    private final int periodoSegundos;
    private final ScheduledExecutorService scheduler;

    public Planificador(Sincronizador sincronizador, int periodoSegundos) {
        this.sincronizador = sincronizador;
        this.periodoSegundos = periodoSegundos;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void iniciar(){
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    sincronizador.sincronizar();
                } catch (IOException e) {
                    if (e instanceof EOFException || e instanceof SocketException){
                        System.err.println("[PLANIFICADOR] Conexión perdida. Sincronización automática detenida. Escribe cualquier comando para salir");
                        scheduler.shutdown();
                    }
                    System.err.println("[PLANIFICADOR] Error en la sincronización automática: " + e.getMessage());
                }

            }
        },periodoSegundos,periodoSegundos, TimeUnit.SECONDS);
    }

    public void detener(){
        scheduler.shutdown();
    }
}
