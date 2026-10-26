package app;

import vista.VentanaPrincipal;

import javax.swing.SwingUtilities;

// Punto de entrada del juego. La prueba de consola de la fase 5 quedó en PruebaConsola.
public class Main {
    public static void main(String[] args) {
        // TEMA: hilos: Swing exige crear y modificar la interfaz en su propio hilo
        // (Event Dispatch Thread). invokeLater encola esta tarea para que la ejecute ese hilo.
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}
