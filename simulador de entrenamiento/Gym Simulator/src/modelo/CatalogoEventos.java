package modelo;

import modelo.EventoAleatorio.OpcionEvento;

import java.util.ArrayList;
import java.util.List;

// TEMA: GRASP Pure Fabrication. Clase inventada para que GestorJuego no cargue con el contenido
// TEMA: clase utilitaria: constructor privado y métodos estáticos (no se instancia)
public final class CatalogoEventos {

    public static final int MAX_PENALIZACION_ENERGIA = -20;

    private CatalogoEventos() {
    }

    // Regla de diseño: ninguna opción resta 40 o más de energía (así un evento
    // nunca puede provocar un desmayo apenas arranca el día)
    public static List<EventoAleatorio> crearEventos() {
        List<EventoAleatorio> eventos = new ArrayList<>();

        eventos.add(new EventoAleatorio("Tuviste una mala noche y dormiste mal.", List.of(
                new OpcionEvento("Entrenar igual", MAX_PENALIZACION_ENERGIA, 0),
                new OpcionEvento("Ir con calma", -10, -5))));

        eventos.add(new EventoAleatorio("Un amigo te invita un batido.", List.of(
                new OpcionEvento("Aceptar el batido", 15, 10),
                new OpcionEvento("Agradecer y seguir", 0, 5))));

        eventos.add(new EventoAleatorio("Hoy el gimnasio está muy concurrido.", List.of(
                new OpcionEvento("Esperar tu turno con paciencia", -5, 0),
                new OpcionEvento("Irte de mal humor", 0, -10))));

        eventos.add(new EventoAleatorio("Sonó tu canción favorita en el gimnasio.", List.of(
                new OpcionEvento("Entrenar a ritmo", -5, 10),
                new OpcionEvento("Disfrutarla sin esfuerzo", 0, 5))));

        return eventos;
    }
}
