package modelo;

public enum EstadoFatiga {
    OPTIMO(1.0),
    FATIGADO(0.6),
    SOBRECARGADO(0.25),
    LESIONADO(0.0);

    private final double efectividad;

    EstadoFatiga(double efectividad) {   // los constructores de enum son siempre privados
        this.efectividad = efectividad;
    }

    public double getEfectividad() {
        return efectividad;
    }
}
