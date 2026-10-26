package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventoAleatorio {

    private final String descripcion;
    private final List<OpcionEvento> opciones;

    public EventoAleatorio(String descripcion, List<OpcionEvento> opciones) {
        if (opciones.size() < 2) {
            throw new IllegalArgumentException("Un evento necesita al menos 2 opciones");
        }
        this.descripcion = descripcion;
        this.opciones = new ArrayList<>(opciones);        // copia defensiva
    }

    // TEMA: sobrecarga de métodos (mismo nombre, distintos parámetros)
    public void aplicar(Personaje p, OpcionEvento elegida) {
        if (!opciones.contains(elegida)) {
            throw new IllegalArgumentException("La opción no pertenece a este evento");
        }
        int deltaEnergia = elegida.getDeltaEnergia();
        if (deltaEnergia < 0) {
            p.consumirEnergia(-deltaEnergia);             // así se dispara el desmayo si llega a 0
        } else {
            p.sumarEnergia(deltaEnergia);
        }
        p.sumarMotivacion(elegida.getDeltaMotivacion());
    }

    // Versión para la vista: un diálogo suele devolver el índice de la opción elegida
    public void aplicar(Personaje p, int indiceOpcion) {
        aplicar(p, opciones.get(indiceOpcion));
    }

    public String getDescripcion() { return descripcion; }

    public List<OpcionEvento> getOpciones() {
        return Collections.unmodifiableList(opciones);
    }

    // TEMA: clase interna estática + clase inmutable
    public static final class OpcionEvento {

        private final String texto;
        private final int deltaEnergia;
        private final int deltaMotivacion;

        public OpcionEvento(String texto, int deltaEnergia, int deltaMotivacion) {
            this.texto = texto;
            this.deltaEnergia = deltaEnergia;
            this.deltaMotivacion = deltaMotivacion;
        }

        public String getTexto() { return texto; }
        public int getDeltaEnergia() { return deltaEnergia; }
        public int getDeltaMotivacion() { return deltaMotivacion; }
    }
}
