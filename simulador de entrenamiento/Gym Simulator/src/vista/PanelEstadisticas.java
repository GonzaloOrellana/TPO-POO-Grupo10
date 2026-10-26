package vista;

import controlador.GestorJuego;
import modelo.Nivel;
import modelo.Personaje;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

// TEMA: herencia: PanelEstadisticas ES UN JPanel (reutiliza el comportamiento de un contenedor Swing)
public class PanelEstadisticas extends JPanel {

    // Paleta retro pizarra extraída de referencia_gimnasio.png (TEMAS: static final)
    public static final Color COLOR_FONDO_HUD = new Color(44, 53, 69);       // #2C3545
    public static final Color COLOR_BORDE_CLARO = new Color(140, 160, 186);  // #8CA0BA
    public static final Color COLOR_BORDE_OSCURO = new Color(24, 30, 41);    // #181E29
    public static final Color COLOR_TEXTO = new Color(244, 241, 232);        // #F4F1E8

    private static final Color COLOR_FUERZA = new Color(224, 70, 70);        // #E04646
    private static final Color COLOR_RESISTENCIA = new Color(76, 201, 91);   // #4CC95B
    private static final Color COLOR_ENERGIA = new Color(242, 201, 56);      // #F2C938
    private static final Color COLOR_MOTIVACION = new Color(54, 163, 247);   // #36A3F7

    private static final Font FUENTE_TITULO = new Font(Font.MONOSPACED, Font.BOLD, 16);
    private static final Font FUENTE_BARRA = new Font(Font.MONOSPACED, Font.BOLD, 12);

    private final GestorJuego controlador;

    private final JLabel lblPersonajeNivel;
    private final JLabel lblDia;
    private final JLabel lblTiempo;

    private final JProgressBar barraFuerza;
    private final JProgressBar barraResistencia;
    private final JProgressBar barraEnergia;
    private final JProgressBar barraMotivacion;

    private final JButton btnCerrarDia;

    public PanelEstadisticas(GestorJuego controlador, ActionListener oyenteCerrarDia) {
        this.controlador = controlador;

        setLayout(new BorderLayout(12, 8));
        setBackground(COLOR_FONDO_HUD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, COLOR_BORDE_OSCURO),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        // 1. Fila superior: Nombre + Nivel | Día | Tiempo restante + Botón Irse a casa
        JPanel filaSuperior = new JPanel(new BorderLayout());
        filaSuperior.setOpaque(false);

        lblPersonajeNivel = crearEtiqueta("");
        lblDia = crearEtiqueta("");
        lblDia.setHorizontalAlignment(JLabel.CENTER);
        lblTiempo = crearEtiqueta("");

        btnCerrarDia = new JButton("IRSE A CASA");
        estilizarBotonRetro(btnCerrarDia);
        btnCerrarDia.addActionListener(oyenteCerrarDia);

        JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        panelDerecho.setOpaque(false);
        panelDerecho.add(lblTiempo);
        panelDerecho.add(btnCerrarDia);

        filaSuperior.add(lblPersonajeNivel, BorderLayout.WEST);
        filaSuperior.add(lblDia, BorderLayout.CENTER);
        filaSuperior.add(panelDerecho, BorderLayout.EAST);

        // 2. Fila inferior: las 4 barras de estadísticas (Fuerza, Resistencia, Energía, Motivación)
        JPanel filaBarras = new JPanel(new GridLayout(1, 4, 14, 0));
        filaBarras.setOpaque(false);

        barraFuerza = crearBarra(COLOR_FUERZA);
        barraResistencia = crearBarra(COLOR_RESISTENCIA);
        barraEnergia = crearBarra(COLOR_ENERGIA);
        barraMotivacion = crearBarra(COLOR_MOTIVACION);

        filaBarras.add(armarCajaBarra("FUERZA", barraFuerza));
        filaBarras.add(armarCajaBarra("RESISTENCIA", barraResistencia));
        filaBarras.add(armarCajaBarra("ENERGIA", barraEnergia));
        filaBarras.add(armarCajaBarra("MOTIVACION", barraMotivacion));

        add(filaSuperior, BorderLayout.NORTH);
        add(filaBarras, BorderLayout.CENTER);

        actualizar();
    }

    // Refresca todos los textos y barras leyendo el estado actual del controlador y del personaje
    public void actualizar() {
        Personaje p = controlador.getPersonaje();
        Nivel nivel = controlador.getNivelActual();

        lblPersonajeNivel.setText(p.getNombre().toUpperCase()
                + " [" + p.getObjetivo() + "] - " + nivel.getNombre().toUpperCase());
        lblDia.setText("DIA " + controlador.getDiaActual() + " / " + nivel.getDiasDisponibles());
        lblTiempo.setText("TIEMPO: " + controlador.getTiempoRestante() + " MIN");

        // Las barras de fuerza y resistencia muestran el % hacia el objetivo del nivel actual
        int porcFuerza = calcularPorcentaje(p.getFuerza(), nivel.getFuerzaObjetivo());
        barraFuerza.setValue(porcFuerza);
        barraFuerza.setString(p.getFuerza() + " / " + nivel.getFuerzaObjetivo() + " (" + porcFuerza + "%)");

        int porcResistencia = calcularPorcentaje(p.getResistencia(), nivel.getResistenciaObjetivo());
        barraResistencia.setValue(porcResistencia);
        barraResistencia.setString(p.getResistencia() + " / " + nivel.getResistenciaObjetivo()
                + " (" + porcResistencia + "%)");

        barraEnergia.setValue(p.getEnergia());
        barraEnergia.setString(p.getEnergia() + " / 100");

        barraMotivacion.setValue(p.getMotivacion());
        barraMotivacion.setString(p.getMotivacion() + " / 100");
    }

    private int calcularPorcentaje(int actual, int objetivo) {
        if (objetivo <= 0) {
            return 100;
        }
        return Math.min(100, (actual * 100) / objetivo);
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(COLOR_TEXTO);
        etiqueta.setFont(FUENTE_TITULO);
        return etiqueta;
    }

    private JProgressBar crearBarra(Color colorRelleno) {
        JProgressBar barra = new JProgressBar(0, 100);
        barra.setStringPainted(true);
        barra.setFont(FUENTE_BARRA);
        barra.setForeground(colorRelleno);
        barra.setBackground(COLOR_BORDE_OSCURO);
        barra.setBorder(BorderFactory.createLineBorder(COLOR_BORDE_CLARO, 2));
        barra.setPreferredSize(new Dimension(160, 22));
        return barra;
    }

    private JPanel armarCajaBarra(String titulo, JProgressBar barra) {
        JPanel caja = new JPanel(new BorderLayout(0, 3));
        caja.setOpaque(false);

        JLabel lbl = new JLabel(titulo);
        lbl.setForeground(COLOR_BORDE_CLARO);
        lbl.setFont(FUENTE_BARRA);

        caja.add(lbl, BorderLayout.NORTH);
        caja.add(barra, BorderLayout.CENTER);
        return caja;
    }

    public static void estilizarBotonRetro(JButton boton) {
        boton.setFont(FUENTE_BARRA);
        boton.setBackground(new Color(58, 74, 98));
        boton.setForeground(COLOR_TEXTO);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE_CLARO, 2),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
    }
}
