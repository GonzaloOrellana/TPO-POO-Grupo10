package modelo;

import excepciones.DatosInvalidosException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Personaje {
    private static final int EDAD_POR_DEFECTO = 25;
    private static final int ENERGIA_MAXIMA = 100;
    private static final int MOTIVACION_MAXIMA = 100;
    private static final int ENERGIA_DIARIA = 40;
    private static final int PORCENTAJE_PERDIDA_DESMAYO = 10;
    private static final int ENERGIA_AL_DESPERTAR = 10;         // 10% de la energía máxima

    private final String nombre;
    private final int edad;
    private final Objetivo objetivo;
    private int fuerza;
    private int resistencia;
    private int energia;
    private int motivacion;
    private final Map<TipoMusculo, GrupoMuscular> gruposMusculares;

    // TEMA: sobrecarga de constructores + this(...)
    public Personaje(String nombre, Objetivo objetivo) throws DatosInvalidosException {
        this(nombre, EDAD_POR_DEFECTO, objetivo);
    }

    public Personaje(String nombre, int edad, Objetivo objetivo) throws DatosInvalidosException {
        this(nombre, edad, objetivo, 0, 0, ENERGIA_MAXIMA, MOTIVACION_MAXIMA);
    }

    public Personaje(String nombre, int edad, Objetivo objetivo,
                     int fuerza, int resistencia, int energia, int motivacion)
            throws DatosInvalidosException {
        // TEMA: throw de excepción propia (checked)
        if (nombre == null || nombre.isBlank()) {
            throw new DatosInvalidosException("El nombre no puede estar vacío");
        }
        if (edad <= 0) {
            throw new DatosInvalidosException("La edad debe ser mayor a cero");
        }
        this.nombre = nombre.trim();
        this.edad = edad;
        this.objetivo = objetivo;
        this.fuerza = fuerza;
        this.resistencia = resistencia;
        this.energia = energia;
        this.motivacion = motivacion;

        // TEMA: arrays (values() devuelve un TipoMusculo[]) + colecciones (Map)
        this.gruposMusculares = new LinkedHashMap<>();
        for (TipoMusculo tipo : TipoMusculo.values()) {
            gruposMusculares.put(tipo, new GrupoMuscular(tipo));
        }
    }

    public void consumirEnergia(int cantidad) {
        this.energia = Math.max(0, energia - cantidad);
        if (this.energia == 0) {
            desmayarse();
        }
    }

    // Comportamiento normal del juego (no una excepción)
    public void desmayarse() {
        fuerza -= fuerza * PORCENTAJE_PERDIDA_DESMAYO / 100;
        resistencia -= resistencia * PORCENTAJE_PERDIDA_DESMAYO / 100;
        // Se despierta con poca energía: si quedara en 0, cada acción lo volvería a desmayar
        energia = ENERGIA_AL_DESPERTAR;
    }

    public void sumarFuerza(int cantidad) {
        this.fuerza += (int) (cantidad * objetivo.getMultiplicadorFuerza());
    }

    public void sumarResistencia(int cantidad) {
        this.resistencia += (int) (cantidad * objetivo.getMultiplicadorResistencia());
    }

    public void sumarEnergia(int cantidad) {
        this.energia = Math.min(ENERGIA_MAXIMA, energia + cantidad);
    }

    public void sumarMotivacion(int cantidad) {   // acepta negativos (eventos, sobreentrenar)
        this.motivacion = Math.max(0, Math.min(MOTIVACION_MAXIMA, motivacion + cantidad));
    }

    public void recuperarEnergiaDiaria() {
        sumarEnergia(ENERGIA_DIARIA);
    }

    public void reiniciarFatigaGrupos() {
        for (GrupoMuscular grupo : gruposMusculares.values()) {
            grupo.reiniciar();
        }
    }

    public void pasarDia() {       // lo llamará GestorJuego al cerrar el día
        for (GrupoMuscular grupo : gruposMusculares.values()) {
            grupo.pasarDia();
        }
    }

    public GrupoMuscular getGrupoMuscular(TipoMusculo tipo) {
        return gruposMusculares.get(tipo);
    }

    public Map<TipoMusculo, GrupoMuscular> getGruposMusculares() {
        return Collections.unmodifiableMap(gruposMusculares);
    }

    public String getNombre() { return nombre; }
    public int getEdad() { return edad; }
    public Objetivo getObjetivo() { return objetivo; }
    public int getFuerza() { return fuerza; }
    public int getResistencia() { return resistencia; }
    public int getEnergia() { return energia; }
    public int getMotivacion() { return motivacion; }

    @Override
    public String toString() {
        return nombre + " [fuerza=" + fuerza + ", resistencia=" + resistencia
                + ", energia=" + energia + ", motivacion=" + motivacion + "]";
    }
}
