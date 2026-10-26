package modelo;

public class Nivel {

    private final int numero;
    private final String nombre;
    private final int fuerzaObjetivo;
    private final int resistenciaObjetivo;
    private final int diasDisponibles;

    // TEMA: sobrecarga de constructores + this(...): si no pasan nombre, usa "Nivel X"
    public Nivel(int numero, int fuerzaObjetivo, int resistenciaObjetivo, int diasDisponibles) {
        this(numero, "Nivel " + numero, fuerzaObjetivo, resistenciaObjetivo, diasDisponibles);
    }

    public Nivel(int numero, String nombre, int fuerzaObjetivo, int resistenciaObjetivo, int diasDisponibles) {
        this.numero = numero;
        this.nombre = nombre;
        this.fuerzaObjetivo = fuerzaObjetivo;
        this.resistenciaObjetivo = resistenciaObjetivo;
        this.diasDisponibles = diasDisponibles;
    }

    public boolean objetivoCumplido(Personaje p) {
        return p.getFuerza() >= fuerzaObjetivo && p.getResistencia() >= resistenciaObjetivo;
    }

    public int getNumero() { return numero; }
    public String getNombre() { return nombre; }
    public int getFuerzaObjetivo() { return fuerzaObjetivo; }
    public int getResistenciaObjetivo() { return resistenciaObjetivo; }
    public int getDiasDisponibles() { return diasDisponibles; }

    @Override
    public String toString() {
        return nombre + " (Nivel " + numero + ") [fuerza>=" + fuerzaObjetivo + ", resistencia>=" + resistenciaObjetivo
                + ", dias=" + diasDisponibles + "]";
    }
}
