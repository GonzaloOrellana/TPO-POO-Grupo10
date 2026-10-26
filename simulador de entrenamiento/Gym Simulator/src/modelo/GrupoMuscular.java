package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GrupoMuscular {

    private static final int INCREMENTO_FATIGA = 25;
    private static final int FATIGA_MAXIMA = 100;
    private static final int DIAS_LESION = 2;            // ajustable (2 o 3)
    private static final int FATIGA_TRAS_LESION = 50;

    private final TipoMusculo tipo;
    private int nivelFatiga;
    private EstadoFatiga estado;
    private int diasLesionRestantes;
    private final List<Integer> historialEstimulo;

    public GrupoMuscular(TipoMusculo tipo) {
        this.tipo = tipo;
        this.nivelFatiga = 0;
        this.estado = EstadoFatiga.OPTIMO;
        this.diasLesionRestantes = 0;
        this.historialEstimulo = new ArrayList<>();
    }

    public double obtenerEfectividad() {
        return estado.getEfectividad();
    }

    public void registrarEntrenamiento() {
        nivelFatiga = Math.min(FATIGA_MAXIMA, nivelFatiga + INCREMENTO_FATIGA);
        historialEstimulo.add(nivelFatiga);
        actualizarEstado();
        // Si recién se lesionó, arranca el plazo de la lesión
        if (estado == EstadoFatiga.LESIONADO && diasLesionRestantes == 0) {
            diasLesionRestantes = DIAS_LESION;
        }
    }

    public void recuperar(int cantidad) {
        if (estado == EstadoFatiga.LESIONADO) {
            return;                    // la lesión se cura con el paso de los días, no descansando
        }
        nivelFatiga = Math.max(0, nivelFatiga - cantidad);
        actualizarEstado();
    }

    // TEMA: visibilidad default. Solo Personaje (mismo paquete) maneja el paso del tiempo
    void pasarDia() {
        if (diasLesionRestantes == 0) {
            return;
        }
        diasLesionRestantes--;
        if (diasLesionRestantes == 0) {
            nivelFatiga = FATIGA_TRAS_LESION;
            actualizarEstado();
        }
    }

    void reiniciar() {                 // al subir de nivel: fatiga 0 y lesión curada
        nivelFatiga = 0;
        diasLesionRestantes = 0;
        actualizarEstado();
    }

    void actualizarEstado() {
        if (diasLesionRestantes > 0) {
            estado = EstadoFatiga.LESIONADO;
            return;
        }
        if (nivelFatiga < 40) {
            estado = EstadoFatiga.OPTIMO;
        } else if (nivelFatiga < 70) {
            estado = EstadoFatiga.FATIGADO;
        } else if (nivelFatiga < 90) {
            estado = EstadoFatiga.SOBRECARGADO;
        } else {
            estado = EstadoFatiga.LESIONADO;
        }
    }

    public TipoMusculo getTipo() { return tipo; }
    public int getNivelFatiga() { return nivelFatiga; }
    public EstadoFatiga getEstado() { return estado; }
    public int getDiasLesionRestantes() { return diasLesionRestantes; }

    public List<Integer> getHistorialEstimulo() {
        return Collections.unmodifiableList(historialEstimulo);
    }

    @Override
    public String toString() {
        return tipo + " [fatiga=" + nivelFatiga + ", estado=" + estado + "]";
    }
}