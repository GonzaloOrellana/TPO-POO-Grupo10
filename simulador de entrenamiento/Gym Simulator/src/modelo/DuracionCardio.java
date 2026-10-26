package modelo;

public enum DuracionCardio {
    CORTO(5, 10, 10),
    MEDIO(10, 20, 20),
    LARGO(15, 30, 30);

    private final int minutos;
    private final int resistenciaGanada;
    private final int energiaGastada;

    DuracionCardio(int minutos, int resistenciaGanada, int energiaGastada) {
        this.minutos = minutos;
        this.resistenciaGanada = resistenciaGanada;
        this.energiaGastada = energiaGastada;
    }

    public int getMinutos() { return minutos; }
    public int getResistenciaGanada() { return resistenciaGanada; }
    public int getEnergiaGastada() { return energiaGastada; }
}
