package modelo;

public class Heladera extends EstacionGimnasio {
    private final int recuperacionEnergia;
    private final int costoTiempo;

    public Heladera(String nombre, int x, int y) {
        super(nombre, x, y);
        this.recuperacionEnergia = 15;
        this.costoTiempo = 1;
    }

    @Override
    public void interactuar(Personaje p) {
        p.sumarEnergia(recuperacionEnergia);
    }

    @Override
    public int getCostoTiempo() { return costoTiempo; }
}
