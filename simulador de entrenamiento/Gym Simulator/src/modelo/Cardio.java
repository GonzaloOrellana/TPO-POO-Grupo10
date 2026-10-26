package modelo;

public class Cardio extends EstacionGimnasio {
    private DuracionCardio duracionSeleccionada;

    public Cardio(String nombre, int x, int y) {
        super(nombre, x, y);
        // Valor por defecto: si quedara en null, interactuar() tiraría NullPointerException
        this.duracionSeleccionada = DuracionCardio.CORTO;
    }

    public void seleccionarDuracion(DuracionCardio duracion) {
        this.duracionSeleccionada = duracion;
    }

    @Override
    public void interactuar(Personaje p) {
        p.sumarResistencia(duracionSeleccionada.getResistenciaGanada());
        p.consumirEnergia(duracionSeleccionada.getEnergiaGastada());
    }

    @Override
    public int getCostoTiempo() { return duracionSeleccionada.getMinutos(); }

    public DuracionCardio getDuracionSeleccionada() { return duracionSeleccionada; }
}
