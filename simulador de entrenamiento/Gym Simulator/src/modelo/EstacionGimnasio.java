package modelo;

public abstract class EstacionGimnasio {
    // TEMA: modificador protected (las subclases acceden directamente)
    protected String nombre;
    protected int x;
    protected int y;

    protected EstacionGimnasio(String nombre, int x, int y) {
        this.nombre = nombre;
        this.x = x;
        this.y = y;
    }

    // TEMA: métodos abstractos: el contrato que cada estación DEBE cumplir
    public abstract void interactuar(Personaje p);

    public abstract int getCostoTiempo();

    // Devuelve "" si la estación se puede usar; si no, el motivo para mostrarle al jugador
    public String getMotivoNoUsable(Personaje p) {
        return "";
    }

    public boolean puedeUsarse(Personaje p) {
        return getMotivoNoUsable(p).isEmpty();
    }

    public String getNombre() { return nombre; }
    public int getX() { return x; }
    public int getY() { return y; }

}
