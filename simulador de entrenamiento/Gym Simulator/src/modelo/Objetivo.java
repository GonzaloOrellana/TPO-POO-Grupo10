package modelo;

public enum Objetivo {
    // Cada categoría de la Copa del Barrio da un bonus a su especialidad
    HIPERTROFIA(1.10, 1.10),
    FUERZA(1.25, 1.0),
    RESISTENCIA(1.0, 1.25);

    private final double multiplicadorFuerza;
    private final double multiplicadorResistencia;

    Objetivo(double multiplicadorFuerza, double multiplicadorResistencia) {
        this.multiplicadorFuerza = multiplicadorFuerza;
        this.multiplicadorResistencia = multiplicadorResistencia;
    }

    public double getMultiplicadorFuerza() { return multiplicadorFuerza; }
    public double getMultiplicadorResistencia() { return multiplicadorResistencia; }
}
