package app;

import controlador.GestorJuego;
import excepciones.DatosInvalidosException;
import modelo.CatalogoEventos;
import modelo.EstacionGimnasio;
import modelo.MaquinaMuscular;
import modelo.Nivel;
import modelo.Objetivo;
import modelo.Personaje;
import modelo.TipoMusculo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Prueba de consola de la fase 5 (eventos aleatorios). Se conserva como referencia;
// el punto de entrada del juego ahora es Main (Swing).
public class PruebaConsola {
    public static void main(String[] args) {
        try {
            Personaje alex = new Personaje("Alex", 25, Objetivo.FUERZA);
            MaquinaMuscular press = new MaquinaMuscular("Press de pecho", 100, 120, TipoMusculo.PECHO);

            List<EstacionGimnasio> estaciones = new ArrayList<>();
            estaciones.add(press);

            List<Nivel> niveles = new ArrayList<>();
            niveles.add(new Nivel(1, 1000, 0, 5));      // objetivo inalcanzable: nos quedamos en el nivel 1

            // TEMA: clase interna anónima: un Random "truco" que fuerza el resultado en las pruebas
            Random siempreEvento = new Random() {
                @Override
                public double nextDouble() { return 0.0; }      // 0.0 < 0.30: siempre sortea un evento
                @Override
                public int nextInt(int limite) { return 0; }    // siempre el primero del catálogo
            };
            Random nuncaEvento = new Random() {
                @Override
                public double nextDouble() { return 0.99; }     // nunca sortea
            };

            GestorJuego gestor = new GestorJuego(alex, estaciones, niveles,
                    CatalogoEventos.crearEventos(), siempreEvento);

            System.out.println("Día 1 -> ¿evento pendiente? " + gestor.hayEventoPendiente());

            gestor.cerrarDia();
            System.out.println("Día 2 -> ¿evento pendiente? " + gestor.hayEventoPendiente());
            System.out.println(gestor.getEventoPendiente().getDescripcion());

            // Con el evento sin resolver no se puede jugar ni cerrar el día
            try {
                gestor.validarAccion(press);
            } catch (IllegalStateException e) {
                System.out.println("Cartel: " + e.getMessage());
            }
            try {
                gestor.cerrarDia();
            } catch (IllegalStateException e) {
                System.out.println("Cartel: " + e.getMessage());
            }

            gestor.resolverEvento(1);                    // "Ir con calma"
            System.out.println(alex);
            System.out.println("¿Evento pendiente? " + gestor.hayEventoPendiente());

            gestor.procesarAccion(press);                // ya se puede jugar
            System.out.println(alex);

            // Azar adverso: no se sortea ningún evento
            Personaje bruno = new Personaje("Bruno", 30, Objetivo.FUERZA);
            GestorJuego sinSuerte = new GestorJuego(bruno, estaciones, niveles,
                    CatalogoEventos.crearEventos(), nuncaEvento);
            sinSuerte.cerrarDia();
            System.out.println("Azar adverso -> ¿evento pendiente? " + sinSuerte.hayEventoPendiente());
            try {
                sinSuerte.resolverEvento(0);
            } catch (IllegalStateException e) {
                System.out.println("Error esperado: " + e.getMessage());
            }

            // Constructor corto: sin catálogo de eventos
            Personaje carla = new Personaje("Carla", 28, Objetivo.RESISTENCIA);
            GestorJuego sinCatalogo = new GestorJuego(carla, estaciones, niveles);
            sinCatalogo.cerrarDia();
            System.out.println("Sin catálogo -> ¿evento pendiente? " + sinCatalogo.hayEventoPendiente());

        } catch (DatosInvalidosException e) {
            System.out.println("No se esperaba este error: " + e.getMessage());
        }
    }
}
