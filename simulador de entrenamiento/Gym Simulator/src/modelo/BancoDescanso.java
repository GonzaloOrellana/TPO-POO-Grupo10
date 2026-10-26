package modelo;

public class BancoDescanso extends EstacionGimnasio {
    private DuracionDescanso duracionSeleccionada;

    public BancoDescanso(String nombre, int x, int y) {
        super(nombre, x, y);
        this.duracionSeleccionada = DuracionDescanso.CORTO;
    }

    public void seleccionarDuracion(DuracionDescanso duracion) {
        this.duracionSeleccionada = duracion;
    }

    @Override
    public void interactuar(Personaje p) {
        for (GrupoMuscular grupo : p.getGruposMusculares().values()) {
            grupo.recuperar(duracionSeleccionada.getReduccionFatiga());
        }
        p.sumarEnergia(duracionSeleccionada.getEnergiaRecuperada());
        p.sumarMotivacion(duracionSeleccionada.getRecuperacionMotivacion());
    }

    @Override
    public int getCostoTiempo() { return duracionSeleccionada.getMinutos(); }

    public DuracionDescanso getDuracionSeleccionada() { return duracionSeleccionada; }
}
