package modelo;

public class MaquinaMuscular extends EstacionGimnasio {
    private static final int PENALIZACION_SOBRECARGA = -5;      // resta motivación por entrenar sobrecargado

    private final TipoMusculo grupoQueTrabaja;
    private final int impactoFuerza;
    private final int costoTiempo;
    private final int costoEnergia;

    // TEMA: sobrecarga de constructores + this(...): el corto usa los valores por defecto
    public MaquinaMuscular(String nombre, int x, int y, TipoMusculo grupoQueTrabaja) {
        this(nombre, x, y, grupoQueTrabaja, 20, 15, 15);
    }

    public MaquinaMuscular(String nombre, int x, int y, TipoMusculo grupoQueTrabaja,
                           int impactoFuerza, int costoTiempo, int costoEnergia) {
        super(nombre, x, y);    // TEMA: super(...) siempre primera línea
        this.grupoQueTrabaja = grupoQueTrabaja;
        this.impactoFuerza = impactoFuerza;
        this.costoTiempo = costoTiempo;
        this.costoEnergia = costoEnergia;
    }

    // TEMA: sobreescritura
    @Override
    public void interactuar(Personaje p) {
        GrupoMuscular grupo = p.getGrupoMuscular(grupoQueTrabaja);
        double efectividad = grupo.obtenerEfectividad();
        if (efectividad == 0) {
            return;// lesionado: no se entrena
        }
        // Entrenar un músculo que ya estaba sobrecargado desmotiva al personaje
        if (grupo.getEstado() == EstadoFatiga.SOBRECARGADO) {
            p.sumarMotivacion(PENALIZACION_SOBRECARGA);
        }
        p.sumarFuerza((int) (impactoFuerza * efectividad));   // TEMA: casteo primitivo
        grupo.registrarEntrenamiento();
        p.consumirEnergia(costoEnergia);
    }

    @Override
    public int getCostoTiempo() { return costoTiempo; }

    // TEMA: sobreescritura + polimorfismo: cada estación explica por qué no se puede usar
    @Override
    public String getMotivoNoUsable(Personaje p) {
        GrupoMuscular grupo = p.getGrupoMuscular(grupoQueTrabaja);
        if (grupo.getEstado() == EstadoFatiga.LESIONADO) {
            return "Tu " + grupoQueTrabaja.name().toLowerCase() + " está lesionado: vuelve a estar disponible en "
                    + grupo.getDiasLesionRestantes() + " día(s)";
        }
        return "";
    }

    public TipoMusculo getGrupoQueTrabaja() { return grupoQueTrabaja; }

}
