package vista;

import modelo.EventoAleatorio;
import modelo.EventoAleatorio.OpcionEvento;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

// TEMA: herencia: DialogoEvento ES UN JDialog modal que obliga al jugador a elegir una opción del evento
public class DialogoEvento extends JDialog {

    private static final Font FUENTE_TITULO = new Font(Font.MONOSPACED, Font.BOLD, 16);
    private static final Font FUENTE_DESCRIPCION = new Font(Font.MONOSPACED, Font.PLAIN, 14);

    private int indiceSeleccionado;

    public DialogoEvento(JFrame ventanaPadre, EventoAleatorio evento) {
        // TEMA: super(...) llama al constructor de JDialog con modal = true (bloquea la ventana principal)
        super(ventanaPadre, "Evento del Día", true);
        this.indiceSeleccionado = 0;

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        JPanel contenedor = new JPanel(new BorderLayout(0, 18));
        contenedor.setBackground(PanelEstadisticas.COLOR_FONDO_HUD);
        contenedor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PanelEstadisticas.COLOR_BORDE_CLARO, 3),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        JLabel lblTitulo = new JLabel("¡EVENTO DEL DÍA!", SwingConstants.CENTER);
        lblTitulo.setFont(FUENTE_TITULO);
        lblTitulo.setForeground(PanelEstadisticas.COLOR_TEXTO);

        // Usamos HTML simple en el JLabel para que el texto largo haga salto de línea automático
        JLabel lblDescripcion = new JLabel(
                "<html><div style='width: 340px; text-align: center;'>"
                        + evento.getDescripcion() + "</div></html>",
                SwingConstants.CENTER);
        lblDescripcion.setFont(FUENTE_DESCRIPCION);
        lblDescripcion.setForeground(PanelEstadisticas.COLOR_TEXTO);

        List<OpcionEvento> opciones = evento.getOpciones();
        JPanel panelBotones = new JPanel(new GridLayout(opciones.size(), 1, 0, 10));
        panelBotones.setOpaque(false);

        for (int i = 0; i < opciones.size(); i++) {
            OpcionEvento opcion = opciones.get(i);
            JButton boton = new JButton(formatearTextoOpcion(opcion));
            PanelEstadisticas.estilizarBotonRetro(boton);
            boton.addActionListener(new OyenteElegirOpcion(i));
            panelBotones.add(boton);
        }

        contenedor.add(lblTitulo, BorderLayout.NORTH);
        contenedor.add(lblDescripcion, BorderLayout.CENTER);
        contenedor.add(panelBotones, BorderLayout.SOUTH);

        setContentPane(contenedor);
        pack();
        setLocationRelativeTo(ventanaPadre);
    }

    public int getIndiceSeleccionado() {
        return indiceSeleccionado;
    }

    private String formatearTextoOpcion(OpcionEvento opcion) {
        StringBuilder sb = new StringBuilder(opcion.getTexto());
        sb.append("  [");
        sb.append("Energía ").append(formatearDelta(opcion.getDeltaEnergia()));
        sb.append(" | Motivación ").append(formatearDelta(opcion.getDeltaMotivacion()));
        sb.append("]");
        return sb.toString();
    }

    private String formatearDelta(int delta) {
        return (delta >= 0) ? ("+" + delta) : String.valueOf(delta);
    }

    // TEMA: clase interna (no estática) para capturar la opción elegida y cerrar el diálogo
    private class OyenteElegirOpcion implements ActionListener {

        private final int indice;

        private OyenteElegirOpcion(int indice) {
            this.indice = indice;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            indiceSeleccionado = indice;
            dispose();      // cierra el diálogo modal y devuelve el control a VentanaPrincipal
        }
    }
}
