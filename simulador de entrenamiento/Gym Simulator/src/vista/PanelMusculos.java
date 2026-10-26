package vista;

import controlador.GestorJuego;
import modelo.EstadoFatiga;
import modelo.GrupoMuscular;
import modelo.Personaje;
import modelo.TipoMusculo;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// TEMA: herencia: PanelMusculos ES UN JPanel que muestra el estado de los 5 grupos musculares
public class PanelMusculos extends JPanel {

    private static final Color COLOR_OPTIMO = new Color(76, 201, 91);        // #4CC95B
    private static final Color COLOR_FATIGADO = new Color(242, 201, 56);     // #F2C938
    private static final Color COLOR_SOBRECARGADO = new Color(240, 138, 60); // #F08A3C
    private static final Color COLOR_LESIONADO = new Color(224, 70, 70);     // #E04646

    private static final Font FUENTE_TITULO_SECCION = new Font(Font.MONOSPACED, Font.BOLD, 12);
    private static final Font FUENTE_MUSCULO = new Font(Font.MONOSPACED, Font.BOLD, 12);
    private static final Font FUENTE_ESTADO = new Font(Font.MONOSPACED, Font.PLAIN, 11);

    private final GestorJuego controlador;

    // TEMA: arrays: 5 posiciones fijas que se reordenan de mayor a menor fatiga en cada actualización
    private final JLabel[] lblNombres;
    private final JProgressBar[] barrasFatiga;
    private final JLabel[] lblEstados;

    public PanelMusculos(GestorJuego controlador) {
        this.controlador = controlador;

        int cantidadGrupos = TipoMusculo.values().length;
        this.lblNombres = new JLabel[cantidadGrupos];
        this.barrasFatiga = new JProgressBar[cantidadGrupos];
        this.lblEstados = new JLabel[cantidadGrupos];

        setLayout(new BorderLayout(0, 6));
        setBackground(PanelEstadisticas.COLOR_FONDO_HUD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, PanelEstadisticas.COLOR_BORDE_OSCURO),
                BorderFactory.createEmptyBorder(8, 16, 10, 16)
        ));

        JLabel titulo = new JLabel("FATIGA MUSCULAR (ORDENADA DE MAYOR A MENOR RIESGO):");
        titulo.setForeground(PanelEstadisticas.COLOR_BORDE_CLARO);
        titulo.setFont(FUENTE_TITULO_SECCION);

        JPanel grilla = new JPanel(new GridLayout(1, cantidadGrupos, 12, 0));
        grilla.setOpaque(false);

        for (int i = 0; i < cantidadGrupos; i++) {
            grilla.add(crearTarjetaMusculo(i));
        }

        add(titulo, BorderLayout.NORTH);
        add(grilla, BorderLayout.CENTER);

        actualizar();
    }

    private JPanel crearTarjetaMusculo(int indice) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 3));
        tarjeta.setBackground(PanelEstadisticas.COLOR_BORDE_OSCURO);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PanelEstadisticas.COLOR_BORDE_CLARO, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        JLabel lblNombre = new JLabel("", SwingConstants.CENTER);
        lblNombre.setForeground(PanelEstadisticas.COLOR_TEXTO);
        lblNombre.setFont(FUENTE_MUSCULO);

        JProgressBar barra = new JProgressBar(0, 100);
        barra.setStringPainted(true);
        barra.setFont(FUENTE_ESTADO);
        barra.setBackground(new Color(18, 22, 30));
        barra.setBorder(BorderFactory.createLineBorder(PanelEstadisticas.COLOR_BORDE_CLARO, 1));
        barra.setPreferredSize(new Dimension(130, 18));

        JLabel lblEstado = new JLabel("", SwingConstants.CENTER);
        lblEstado.setFont(FUENTE_ESTADO);

        lblNombres[indice] = lblNombre;
        barrasFatiga[indice] = barra;
        lblEstados[indice] = lblEstado;

        tarjeta.add(lblNombre, BorderLayout.NORTH);
        tarjeta.add(barra, BorderLayout.CENTER);
        tarjeta.add(lblEstado, BorderLayout.SOUTH);
        return tarjeta;
    }

    public void actualizar() {
        Personaje p = controlador.getPersonaje();

        // TEMA: colecciones genéricas + ordenamiento personalizado con Comparator
        // Copiamos los 5 grupos a una lista para poder ordenarlos sin alterar el Map de Personaje
        List<GrupoMuscular> listaOrdenada = new ArrayList<>(p.getGruposMusculares().values());
        Collections.sort(listaOrdenada, new ComparadorPorFatiga());

        for (int i = 0; i < listaOrdenada.size(); i++) {
            GrupoMuscular grupo = listaOrdenada.get(i);
            EstadoFatiga estado = grupo.getEstado();
            Color colorEstado = obtenerColorEstado(estado);

            lblNombres[i].setText(grupo.getTipo().name());

            barrasFatiga[i].setValue(grupo.getNivelFatiga());
            barrasFatiga[i].setForeground(colorEstado);
            barrasFatiga[i].setString("Fatiga: " + grupo.getNivelFatiga() + "%");

            lblEstados[i].setForeground(colorEstado);
            if (estado == EstadoFatiga.LESIONADO) {
                lblEstados[i].setText("LESIONADO (" + grupo.getDiasLesionRestantes() + "d)");
            } else {
                int efectividadPorc = (int) (grupo.obtenerEfectividad() * 100);
                lblEstados[i].setText(estado.name() + " (Efec. " + efectividadPorc + "%)");
            }
        }
    }

    private Color obtenerColorEstado(EstadoFatiga estado) {
        if (estado == EstadoFatiga.OPTIMO) {
            return COLOR_OPTIMO;
        } else if (estado == EstadoFatiga.FATIGADO) {
            return COLOR_FATIGADO;
        } else if (estado == EstadoFatiga.SOBRECARGADO) {
            return COLOR_SOBRECARGADO;
        } else {
            return COLOR_LESIONADO;
        }
    }

    // TEMA: ordenamiento personalizado (Comparator) + clase interna estática
    // Ordena de mayor a menor fatiga (el músculo más comprometido aparece primero a la izquierda);
    // a igual fatiga, desempata por el orden del enum TipoMusculo.
    private static class ComparadorPorFatiga implements Comparator<GrupoMuscular> {
        @Override
        public int compare(GrupoMuscular g1, GrupoMuscular g2) {
            int porFatigaDesc = Integer.compare(g2.getNivelFatiga(), g1.getNivelFatiga());
            if (porFatigaDesc != 0) {
                return porFatigaDesc;
            }
            return g1.getTipo().compareTo(g2.getTipo());
        }
    }
}
