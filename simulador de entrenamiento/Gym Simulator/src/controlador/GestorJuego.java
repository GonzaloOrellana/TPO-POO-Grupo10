package controlador;

import excepciones.TiempoInsuficienteException;
import modelo.EstacionGimnasio;
import modelo.EventoAleatorio;
import modelo.Nivel;
import modelo.Personaje;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// TEMA: GRASP Controller. Es el único que coordina el modelo; la vista le pide todo a él
public class GestorJuego {

    private static final int TIEMPO_POR_DIA = 90;               // TEMA: static final
    private static final double PROBABILIDAD_EVENTO = 0.30;

    private final Personaje personaje;
    private final List<EstacionGimnasio> estaciones;
    private final List<Nivel> niveles;
    private final List<EventoAleatorio> eventos;
    private final Random azar;
    private int indiceNivelActual;
    private int diaActual;
    private int tiempoRestante;
    private EventoAleatorio eventoPendiente;                    // null mientras no haya un evento sin resolver
    private final List<RegistroDia> historial;

    // TEMA: sobrecarga de constructores + this(...): sin catálogo de eventos
    public GestorJuego(Personaje personaje, List<EstacionGimnasio> estaciones, List<Nivel> niveles) {
        this(personaje, estaciones, niveles, new ArrayList<>(), new Random());
    }

    // Los eventos y el Random llegan de afuera (inyección de dependencias): se puede probar con azar fijo
    public GestorJuego(Personaje personaje, List<EstacionGimnasio> estaciones, List<Nivel> niveles,
                       List<EventoAleatorio> eventos, Random azar) {
        if (niveles.isEmpty()) {
            throw new IllegalArgumentException("Tiene que haber al menos un nivel");
        }
        this.personaje = personaje;
        this.estaciones = new ArrayList<>(estaciones);   // copias defensivas
        this.niveles = new ArrayList<>(niveles);
        this.eventos = new ArrayList<>(eventos);
        this.azar = azar;
        this.indiceNivelActual = 0;
        this.diaActual = 1;
        this.tiempoRestante = TIEMPO_POR_DIA;
        this.eventoPendiente = null;                     // el día 1 NO tiene evento
        this.historial = new ArrayList<>();
    }

    // La vista lo llama ANTES de mover al personaje: si algo falla, muestra el cartel con el mensaje
    public void validarAccion(EstacionGimnasio estacion) {
        verificarSinEventoPendiente();
        // TEMA: polimorfismo: no sabemos qué estación es, cada una explica su motivo
        String motivo = estacion.getMotivoNoUsable(personaje);
        if (!motivo.isEmpty()) {
            throw new IllegalStateException(motivo);
        }
        // TEMA: throw de excepción propia (unchecked)
        if (estacion.getCostoTiempo() > tiempoRestante) {
            throw new TiempoInsuficienteException("No alcanza el tiempo para " + estacion.getNombre());
        }
    }

    public void procesarAccion(EstacionGimnasio estacion) {
        validarAccion(estacion);
        estacion.interactuar(personaje);
        tiempoRestante -= estacion.getCostoTiempo();
    }

    public boolean verificarVictoria() {
        return !hayNivelSiguiente() && getNivelActual().objetivoCumplido(personaje);
    }

    public boolean verificarDerrota() {
        return perdioPorBurnout() || perdioPorTiempo();
    }

    // Final "abandonaste la copa": el personaje se quedó sin motivación
    public boolean perdioPorBurnout() {
        return personaje.getMotivacion() == 0;
    }

    // Final "no llegaste a tiempo": pasaron los días del nivel sin cumplir su objetivo
    public boolean perdioPorTiempo() {
        return diaActual > getNivelActual().getDiasDisponibles()
                && !getNivelActual().objetivoCumplido(personaje);
    }

    // También es lo que ejecuta el botón "Irse a casa".
    // Devuelve true si el jugador completó el nivel y pasó al siguiente (la vista muestra el cartel)
    public boolean cerrarDia() {
        verificarSinEventoPendiente();

        historial.add(new RegistroDia(getNivelActual().getNumero(), diaActual,
                personaje.getEnergia(), personaje.getMotivacion(),
                personaje.getFuerza(), personaje.getResistencia()));

        personaje.pasarDia();                             // avanza el contador de las lesiones

        boolean subioDeNivel = false;
        if (getNivelActual().objetivoCumplido(personaje) && hayNivelSiguiente()) {
            avanzarNivel();
            subioDeNivel = true;
        } else {
            diaActual++;
        }

        tiempoRestante = TIEMPO_POR_DIA;
        personaje.recuperarEnergiaDiaria();               // primero se recupera la energía...
        sortearEventoDelDia();                            // ...y recién después se sortea el evento
        return subioDeNivel;
    }

    // ----- Eventos aleatorios -----

    public boolean hayEventoPendiente() {
        return eventoPendiente != null;
    }

    public EventoAleatorio getEventoPendiente() {
        if (eventoPendiente == null) {
            throw new IllegalStateException("No hay un evento pendiente");
        }
        return eventoPendiente;
    }

    // La vista le informa qué opción eligió el jugador (índice dentro de las opciones del evento)
    public void resolverEvento(int indiceOpcion) {
        getEventoPendiente().aplicar(personaje, indiceOpcion);   // si el índice es inválido, falla ANTES de limpiar
        eventoPendiente = null;
    }

    private void sortearEventoDelDia() {
        if (eventos.isEmpty()) {
            return;
        }
        if (azar.nextDouble() < PROBABILIDAD_EVENTO) {
            eventoPendiente = eventos.get(azar.nextInt(eventos.size()));
        }
    }

    private void verificarSinEventoPendiente() {
        if (eventoPendiente != null) {
            throw new IllegalStateException("Resolvé el evento antes de continuar");
        }
    }

    // ----- Niveles -----

    private boolean hayNivelSiguiente() {
        return indiceNivelActual < niveles.size() - 1;
    }

    private void avanzarNivel() {
        indiceNivelActual++;
        diaActual = 1;
        personaje.reiniciarFatigaGrupos();                // cura fatiga y lesiones
    }

    public Personaje getPersonaje() { return personaje; }
    public Nivel getNivelActual() { return niveles.get(indiceNivelActual); }
    public int getDiaActual() { return diaActual; }
    public int getTiempoRestante() { return tiempoRestante; }

    public List<EstacionGimnasio> getEstaciones() {
        return Collections.unmodifiableList(estaciones);
    }

    public List<RegistroDia> getHistorial() {
        return Collections.unmodifiableList(historial);
    }

    // TEMA: clase interna estática (no necesita acceso a una instancia de GestorJuego)
    // TEMA: clase inmutable (todos los campos final, sin setters) + Comparable
    public static final class RegistroDia implements Comparable<RegistroDia> {

        private final int numeroNivel;
        private final int numeroDia;
        private final int energiaFinal;
        private final int motivacionFinal;
        private final int fuerzaFinal;
        private final int resistenciaFinal;

        private RegistroDia(int numeroNivel, int numeroDia, int energiaFinal,
                            int motivacionFinal, int fuerzaFinal, int resistenciaFinal) {
            this.numeroNivel = numeroNivel;
            this.numeroDia = numeroDia;
            this.energiaFinal = energiaFinal;
            this.motivacionFinal = motivacionFinal;
            this.fuerzaFinal = fuerzaFinal;
            this.resistenciaFinal = resistenciaFinal;
        }

        // TEMA: orden natural (Comparable): cronológico, primero por nivel y luego por día
        @Override
        public int compareTo(RegistroDia otro) {
            int porNivel = Integer.compare(this.numeroNivel, otro.numeroNivel);
            if (porNivel != 0) {
                return porNivel;
            }
            return Integer.compare(this.numeroDia, otro.numeroDia);
        }

        public int getNumeroNivel() { return numeroNivel; }
        public int getNumeroDia() { return numeroDia; }
        public int getEnergiaFinal() { return energiaFinal; }
        public int getMotivacionFinal() { return motivacionFinal; }
        public int getFuerzaFinal() { return fuerzaFinal; }
        public int getResistenciaFinal() { return resistenciaFinal; }

        @Override
        public String toString() {
            return "Nivel " + numeroNivel + " / Día " + numeroDia + " [energia=" + energiaFinal
                    + ", motivacion=" + motivacionFinal + ", fuerza=" + fuerzaFinal
                    + ", resistencia=" + resistenciaFinal + "]";
        }
    }
}
