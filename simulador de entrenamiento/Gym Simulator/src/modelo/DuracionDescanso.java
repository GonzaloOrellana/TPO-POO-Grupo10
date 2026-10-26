package modelo;

public enum DuracionDescanso {
    CORTO(1, 10, 5, 5),
    MEDIO(2, 20, 10, 10),
    LARGO(3, 30, 15, 15);

    private final int minutos;
    private final int energiaRecuperada;
    private final int reduccionFatiga;
    private final int recuperacionMotivacion;

    DuracionDescanso(int minutos, int energiaRecuperada, int reduccionFatiga, int recuperacionMotivacion) {
        this.minutos = minutos;
        this.energiaRecuperada = energiaRecuperada;
        this.reduccionFatiga = reduccionFatiga;
        this.recuperacionMotivacion = recuperacionMotivacion;
    }

    public int getMinutos() { return minutos; }
    public int getEnergiaRecuperada() { return energiaRecuperada; }
    public int getReduccionFatiga() { return reduccionFatiga; }
    public int getRecuperacionMotivacion() { return recuperacionMotivacion; }
}
