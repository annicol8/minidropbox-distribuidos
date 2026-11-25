package cliente;

import java.io.IOException;
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
                    System.err.println("[PLANIFICADOR] Error en la sincronización automática: " + e.getMessage());
                }

            }
        },periodoSegundos,periodoSegundos, TimeUnit.SECONDS);
    }

    public void detener(){
        scheduler.shutdown();
    }
}