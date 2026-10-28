package vista;

import controlador.GestorJuego;
import excepciones.DatosInvalidosException;
import excepciones.TiempoInsuficienteException;
import modelo.BancoDescanso;
import modelo.Cardio;
import modelo.CatalogoEventos;
import modelo.DuracionCardio;
import modelo.DuracionDescanso;
import modelo.EstacionGimnasio;
import modelo.EventoAleatorio;
import modelo.Heladera;
import modelo.MaquinaMuscular;
import modelo.Nivel;
import modelo.Objetivo;
import modelo.Personaje;
import modelo.TipoMusculo;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// TEMA: herencia: VentanaPrincipal ES UN JFrame (reutiliza todo lo que ya sabe hacer una ventana)
public class VentanaPrincipal extends JFrame {

    private static final int ANCHO = 960;
    private static final int ALTO = 740;

    // Color de fondo pizarra de referencia_gimnasio.png (#495466)
    public static final Color COLOR_FONDO_PIZARRA = new Color(73, 84, 102);

    // TEMA: asociación unidireccional Vista -> Controlador (el controlador no conoce a la vista)
    private GestorJuego controlador;        // null hasta que se inicia la partida
    private PanelEstadisticas panelEstadisticas;
    private PanelGimnasio panelGimnasio;
    private PanelMusculos panelMusculos;

    private final JTextField txtNombre;
    private final JComboBox<Objetivo> cbObjetivo;
    private final JButton btnIniciarPartida;

    public VentanaPrincipal() {
        super("Simulador de Entrenamiento — La Copa del Barrio"); // TEMA: super(...): título del JFrame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(ANCHO, ALTO);
        setLocationRelativeTo(null);              // centra la ventana en la pantalla
        setResizable(false);

        txtNombre = new JTextField(12);
        // TEMA: arrays: Objetivo.values() devuelve un Objetivo[] con las 3 opciones del combo
        cbObjetivo = new JComboBox<>(Objetivo.values());

        btnIniciarPartida = new JButton("JUGAR");
        estilizarBotonGrandeMenu(btnIniciarPartida);
        // TEMA: manejo de eventos: el botón avisa a su oyente cada vez que lo clickean
        btnIniciarPartida.addActionListener(new OyenteIniciarPartida());

        mostrarPantallaInicio();
    }

    private void mostrarPantallaInicio() {
        Font fuenteEtiqueta = new Font(Font.MONOSPACED, Font.BOLD, 13);
        Font fuenteCampo = new Font(Font.MONOSPACED, Font.BOLD, 14);

        // 1. Cartel superior estilo placa 3D pixel-art con el título del juego
        PanelPlacaPixelArt placaTitulo = new PanelPlacaPixelArt(new BorderLayout(0, 4));
        placaTitulo.setBorder(BorderFactory.createEmptyBorder(12, 24, 16, 24));
        placaTitulo.setMaximumSize(new Dimension(560, 96));
        placaTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel("LA COPA DEL BARRIO", SwingConstants.CENTER);
        lblTitulo.setFont(new Font(Font.MONOSPACED, Font.BOLD, 26));
        lblTitulo.setForeground(PanelEstadisticas.COLOR_BOTON_TEXTO);

        JLabel lblSubtitulo = new JLabel("SIMULADOR DE ENTRENAMIENTO · INGRESÁ TUS DATOS", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        lblSubtitulo.setForeground(PanelEstadisticas.COLOR_BOTON_BISEL);

        placaTitulo.add(lblTitulo, BorderLayout.NORTH);
        placaTitulo.add(lblSubtitulo, BorderLayout.SOUTH);

        // 2. Placa central 3D con la ficha de inscripción del atleta (solo Nombre y Categoría) y retrato
        estilizarCampoPixelArt(txtNombre, fuenteCampo);
        cbObjetivo.setFont(fuenteEtiqueta);
        cbObjetivo.setBackground(PanelEstadisticas.COLOR_BOTON_BRILLO);
        cbObjetivo.setForeground(PanelEstadisticas.COLOR_BOTON_CONTORNO);
        cbObjetivo.setBorder(BorderFactory.createLineBorder(PanelEstadisticas.COLOR_BOTON_CONTORNO, 2));

        JPanel grillaCampos = new JPanel(new GridLayout(2, 2, 10, 14));
        grillaCampos.setOpaque(false);

        JLabel lblNombre = crearEtiquetaPlaca("NOMBRE:");
        JLabel lblObjetivo = crearEtiquetaPlaca("CATEGORÍA:");

        grillaCampos.add(lblNombre);
        grillaCampos.add(txtNombre);
        grillaCampos.add(lblObjetivo);
        grillaCampos.add(cbObjetivo);

        JLabel lblBonusObjetivo = new JLabel("", SwingConstants.CENTER);
        lblBonusObjetivo.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
        lblBonusObjetivo.setForeground(PanelEstadisticas.COLOR_BOTON_CONTORNO);
        actualizarTextoBonus(lblBonusObjetivo, (Objetivo) cbObjetivo.getSelectedItem());
        cbObjetivo.addActionListener(e ->
                actualizarTextoBonus(lblBonusObjetivo, (Objetivo) cbObjetivo.getSelectedItem()));

        JPanel derechaFicha = new JPanel(new BorderLayout(0, 12));
        derechaFicha.setOpaque(false);
        derechaFicha.add(grillaCampos, BorderLayout.CENTER);
        derechaFicha.add(lblBonusObjetivo, BorderLayout.SOUTH);

        PanelPlacaPixelArt placaFicha = new PanelPlacaPixelArt(new BorderLayout(16, 0));
        placaFicha.setBorder(BorderFactory.createEmptyBorder(16, 22, 20, 22));
        placaFicha.setMaximumSize(new Dimension(560, 165));
        placaFicha.setAlignmentX(Component.CENTER_ALIGNMENT);
        placaFicha.add(crearMiniRetratoCompetidor(), BorderLayout.WEST);
        placaFicha.add(derechaFicha, BorderLayout.CENTER);

        // 3. Columna central de 3 grandes botones 3D Pixel-Art (como PLAY / PAUSE / EXIT en la referencia)
        btnIniciarPartida.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnComoJugar = new JButton("REGLAS");
        estilizarBotonGrandeMenu(btnComoJugar);
        btnComoJugar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnComoJugar.addActionListener(e -> mostrarDialogoComoJugar());

        JButton btnSalir = new JButton("SALIR");
        estilizarBotonGrandeMenu(btnSalir);
        btnSalir.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSalir.addActionListener(e -> dispose());

        // Armado de la columna central vertical
        JPanel columnaCentral = new JPanel();
        columnaCentral.setLayout(new BoxLayout(columnaCentral, BoxLayout.Y_AXIS));
        columnaCentral.setOpaque(false);
        columnaCentral.add(placaTitulo);
        columnaCentral.add(Box.createVerticalStrut(14));
        columnaCentral.add(placaFicha);
        columnaCentral.add(Box.createVerticalStrut(18));
        columnaCentral.add(btnIniciarPartida);
        columnaCentral.add(Box.createVerticalStrut(12));
        columnaCentral.add(btnComoJugar);
        columnaCentral.add(Box.createVerticalStrut(12));
        columnaCentral.add(btnSalir);

        // 4. Botones cuadrados 3D en las esquinas inferiores (como los iconos inferiores de la referencia)
        JButton btnHistoria = crearBotonCuadradoIcono("COPA", "Ver historia de La Copa del Barrio");
        btnHistoria.addActionListener(e -> mostrarDialogoHistoria());

        JButton btnEtapas = crearBotonCuadradoIcono("METAS", "Ver objetivos de las 3 etapas");
        btnEtapas.addActionListener(e -> mostrarDialogoMetas());

        JButton btnAyuda = crearBotonCuadradoIcono("?", "Cómo jugar");
        btnAyuda.addActionListener(e -> mostrarDialogoComoJugar());

        JPanel esquinaIzq = new JPanel(new GridLayout(1, 1, 10, 0));
        esquinaIzq.setOpaque(false);
        esquinaIzq.add(btnHistoria);

        JPanel esquinaDer = new JPanel(new GridLayout(1, 2, 10, 0));
        esquinaDer.setOpaque(false);
        esquinaDer.add(btnEtapas);
        esquinaDer.add(btnAyuda);

        JPanel barraInferior = new JPanel(new BorderLayout());
        barraInferior.setOpaque(false);
        barraInferior.setBorder(BorderFactory.createEmptyBorder(0, 36, 28, 36));
        barraInferior.add(esquinaIzq, BorderLayout.WEST);
        barraInferior.add(esquinaDer, BorderLayout.EAST);

        JPanel envoltorioCentro = new JPanel(new GridBagLayout());
        envoltorioCentro.setOpaque(false);
        envoltorioCentro.add(columnaCentral);

        PanelFondoMenuPixelArt fondo = new PanelFondoMenuPixelArt();
        fondo.setLayout(new BorderLayout());
        fondo.add(envoltorioCentro, BorderLayout.CENTER);
        fondo.add(barraInferior, BorderLayout.SOUTH);

        setContentPane(fondo);
        revalidate();
        repaint();
    }

    private JLabel crearEtiquetaPlaca(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        lbl.setForeground(PanelEstadisticas.COLOR_BOTON_TEXTO);
        return lbl;
    }

    private JPanel crearMiniRetratoCompetidor() {
        JPanel recuadro = new JPanel(new BorderLayout());
        recuadro.setBackground(PanelEstadisticas.COLOR_BOTON_BRILLO);
        recuadro.setPreferredSize(new Dimension(96, 110));
        recuadro.setBorder(BorderFactory.createLineBorder(PanelEstadisticas.COLOR_BOTON_CONTORNO, 3));

        JLabel lblSprite = new JLabel("", SwingConstants.CENTER);
        Image imgPersonaje = cargarImagenRecurso("/recursos/sprites/personaje_1.png");
        if (imgPersonaje != null) {
            Image escalada = imgPersonaje.getScaledInstance(56, 88, Image.SCALE_SMOOTH);
            lblSprite.setIcon(new ImageIcon(escalada));
        }
        recuadro.add(lblSprite, BorderLayout.CENTER);
        return recuadro;
    }

    private void estilizarBotonGrandeMenu(JButton boton) {
        Font fuenteGrande = new Font(Font.MONOSPACED, Font.BOLD, 24);
        Dimension tamano = new Dimension(270, 62);
        boton.setPreferredSize(tamano);
        boton.setMinimumSize(tamano);
        boton.setMaximumSize(tamano);
        PanelEstadisticas.estilizarBotonPixelArt(boton, fuenteGrande, new Insets(10, 28, 16, 28));
    }

    private JButton crearBotonCuadradoIcono(String textoCorto, String tooltip) {
        JButton boton = new JButton(textoCorto);
        boton.setToolTipText(tooltip);
        Dimension tam = new Dimension(74, 58);
        boton.setPreferredSize(tam);
        boton.setMinimumSize(tam);
        boton.setMaximumSize(tam);
        PanelEstadisticas.estilizarBotonPixelArt(
                boton, new Font(Font.MONOSPACED, Font.BOLD, 14), new Insets(8, 8, 12, 8));
        return boton;
    }

    private void actualizarTextoBonus(JLabel etiqueta, Objetivo objetivo) {
        if (objetivo == null) {
            return;
        }
        switch (objetivo) {
            case HIPERTROFIA:
                etiqueta.setText("★ BONO: +10% Fuerza y +10% Resistencia (Equilibrado)");
                break;
            case FUERZA:
                etiqueta.setText("★ BONO: +25% Fuerza al entrenar en máquinas");
                break;
            case RESISTENCIA:
                etiqueta.setText("★ BONO: +25% Resistencia en sesiones de cinta");
                break;
            default:
                etiqueta.setText("");
                break;
        }
    }

    private void estilizarCampoPixelArt(JTextField campo, Font fuente) {
        campo.setFont(fuente);
        campo.setBackground(PanelEstadisticas.COLOR_BOTON_BRILLO);
        campo.setForeground(PanelEstadisticas.COLOR_BOTON_CONTORNO);
        campo.setCaretColor(PanelEstadisticas.COLOR_BOTON_CONTORNO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PanelEstadisticas.COLOR_BOTON_CONTORNO, 2),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
    }

    private void mostrarDialogoHistoria() {
        String historia = "<html><div style='width: 360px; font-family: monospace;'>"
                + "<b>LA COPA DEL BARRIO</b><br><br>"
                + "Hace años que dejaste de entrenar. Pasás por la puerta del gimnasio del barrio y ves el cartel:<br><br>"
                + "<b>«COPA DEL BARRIO — Inscripciones abiertas»</b><br><br>"
                + "Te anotás sin pensarlo. Tenés pocos días para superar la Clasificatoria, la Semifinal y la Final… "
                + "¡y no te podés rendir!"
                + "</div></html>";
        JOptionPane.showMessageDialog(this, historia, "Historia — La Copa del Barrio",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarDialogoMetas() {
        String metas = "<html><div style='width: 360px; font-family: monospace;'>"
                + "<b>ETAPAS DEL TORNEO:</b><br><br>"
                + "• <b>Nivel 1 — Clasificatoria:</b> 100 Fuerza · 60 Resistencia (5 días)<br>"
                + "• <b>Nivel 2 — Semifinal:</b> 250 Fuerza · 150 Resistencia (4 días)<br>"
                + "• <b>Nivel 3 — Final:</b> 450 Fuerza · 270 Resistencia (3 días)<br><br>"
                + "Al superar una etapa, al cerrar el día tu fatiga y lesiones se reinician."
                + "</div></html>";
        JOptionPane.showMessageDialog(this, metas, "Metas del Torneo",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarDialogoComoJugar() {
        String guia = "<html><div style='width: 380px; font-family: monospace;'>"
                + "<b>1. PRESUPUESTO DIARIO (90 MIN):</b><br>"
                + "Hacé click sobre cualquier máquina del gimnasio para caminar hacia ella y entrenar. "
                + "Cada máquina muscular cuesta 15 min, la cinta 5/10/15 min, el banco 1/2/3 min y la heladera 1 min.<br><br>"
                + "<b>2. FATIGA Y LESIONES:</b><br>"
                + "Entrenar un músculo sube su fatiga (+25%). Si está SOBRECARGADO rinde menos y resta motivación. "
                + "Si llega a 100% queda <b>LESIONADO por 2 días</b>. ¡Usá el <b>Banco de Descanso</b> para bajar la fatiga!<br><br>"
                + "<b>3. ENERGÍA Y DESMAYO:</b><br>"
                + "Si tu energía llega a 0 te desmayás y perdés 10% de Fuerza y Resistencia. "
                + "Tomá agua en la <b>Heladera</b> o descansá para recuperar energía.<br><br>"
                + "<b>4. AVANZAR DE ETAPA:</b><br>"
                + "Alcanzá la meta de Fuerza y Resistencia antes de quedarte sin días y hacé click en "
                + "<b>IRSE A CASA</b> para cerrar el día."
                + "</div></html>";
        JOptionPane.showMessageDialog(this, guia, "¿Cómo jugar? — Reglas de la Copa",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private Image cargarImagenRecurso(String ruta) {
        try {
            URL url = getClass().getResource(ruta);
            if (url != null) {
                return ImageIO.read(url);
            }
        } catch (IOException ex) {
            // Si no está disponible la imagen, se continúa sin ícono decorativo
        }
        return null;
    }

    private void iniciarPartida() {
        // TEMA: try / catch: un error del usuario se muestra en un cartel y el programa sigue andando
        try {
            String nombre = txtNombre.getText();
            Objetivo objetivo = (Objetivo) cbObjetivo.getSelectedItem();    // TEMA: casteo de referencia (Object -> Objetivo)
            Personaje personaje = new Personaje(nombre, objetivo);          // puede lanzar DatosInvalidosException

            controlador = new GestorJuego(personaje, crearEstaciones(), crearNiveles(),
                    CatalogoEventos.crearEventos(), new Random());
            mostrarPantallaJuego();
        } catch (DatosInvalidosException ex) {
            // TEMA: captura de excepción propia (checked)
            mostrarCartel(ex.getMessage());
        }
    }

    private void mostrarPantallaJuego() {
        panelEstadisticas = new PanelEstadisticas(controlador, new OyenteCerrarDia());
        panelGimnasio = new PanelGimnasio(controlador, new OyenteClickEstacion());
        panelMusculos = new PanelMusculos(controlador);

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setBackground(COLOR_FONDO_PIZARRA);
        contenido.add(panelEstadisticas, BorderLayout.NORTH);
        contenido.add(panelGimnasio, BorderLayout.CENTER);
        contenido.add(panelMusculos, BorderLayout.SOUTH);

        setContentPane(contenido);
        revalidate();   // recalcula la distribución de los componentes nuevos
        repaint();      // y los vuelve a dibujar

        // El Día 1 comienza con la animación de entrada desde la puerta hacia el centro del gimnasio
        SwingUtilities.invokeLater(() -> {
            if (panelGimnasio != null) {
                panelGimnasio.animarEntradaGimnasio(() -> actualizarVista());
            }
        });
    }

    // Se invoca al hacer click sobre una estación en PanelGimnasio (DISENO_CLASES sección 6)
    private void manejarClickEstacion(EstacionGimnasio estacion) {
        if (panelGimnasio.estaEnMovimiento()) {
            return;     // evita superponer otra acción mientras el hilo de movimiento está activo
        }

        // Si la estación requiere elegir duración (Cardio o BancoDescanso), preguntamos antes de validar
        if (!elegirDuracionSiCorresponde(estacion)) {
            return;     // el jugador canceló el cuadro de selección
        }

        // TEMA: try / catch / finally + multi-catch con excepción propia (TiempoInsuficienteException)
        try {
            controlador.validarAccion(estacion);                    // si falla, el personaje NO se mueve
            panelGimnasio.moverPersonajeHacia(estacion.getX(), estacion.getY(), () -> {
                ejecutarAccionAlLlegar(estacion);
            });
        } catch (IllegalStateException | TiempoInsuficienteException ex) {
            mostrarCartel(ex.getMessage());                         // cartel con el motivo exacto
        } finally {
            actualizarVista();
        }
    }

    // Se ejecuta en el Event Dispatch Thread cuando el hilo MovimientoPersonaje llega a la estación
    private void ejecutarAccionAlLlegar(EstacionGimnasio estacion) {
        try {
            controlador.procesarAccion(estacion);
            verificarFinDePartida();
        } catch (IllegalStateException | TiempoInsuficienteException ex) {
            mostrarCartel(ex.getMessage());
        } finally {
            actualizarVista();
        }
    }

    private boolean elegirDuracionSiCorresponde(EstacionGimnasio estacion) {
        if (estacion instanceof Cardio) {
            Cardio cardio = (Cardio) estacion;
            DuracionCardio[] opciones = DuracionCardio.values();
            String[] etiquetas = new String[opciones.length];
            for (int i = 0; i < opciones.length; i++) {
                DuracionCardio d = opciones[i];
                etiquetas[i] = d.name() + " (" + d.getMinutos() + " min | +"
                        + d.getResistenciaGanada() + " res | -" + d.getEnergiaGastada() + " ene)";
            }
            int eleccion = JOptionPane.showOptionDialog(this,
                    "Elegí la duración en la cinta:", estacion.getNombre(),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, etiquetas, etiquetas[0]);
            if (eleccion < 0) {
                return false;
            }
            cardio.seleccionarDuracion(opciones[eleccion]);
        } else if (estacion instanceof BancoDescanso) {
            BancoDescanso banco = (BancoDescanso) estacion;
            DuracionDescanso[] opciones = DuracionDescanso.values();
            String[] etiquetas = new String[opciones.length];
            for (int i = 0; i < opciones.length; i++) {
                DuracionDescanso d = opciones[i];
                etiquetas[i] = d.name() + " (" + d.getMinutos() + " min | -"
                        + d.getReduccionFatiga() + " fatiga | +" + d.getEnergiaRecuperada() + " ene)";
            }
            int eleccion = JOptionPane.showOptionDialog(this,
                    "Elegí la duración del descanso:", estacion.getNombre(),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, etiquetas, etiquetas[0]);
            if (eleccion < 0) {
                return false;
            }
            banco.seleccionarDuracion(opciones[eleccion]);
        }
        return true;
    }

    private void cerrarDiaActual() {
        if (panelGimnasio == null || panelGimnasio.estaEnMovimiento()) {
            return;     // espera a que termine de caminar antes de cerrar el día
        }
        if (controlador.hayEventoPendiente()) {
            mostrarCartel("Resolvé el evento antes de continuar");
            return;
        }

        // 1. El personaje vuelve primero al centro y luego camina hacia la puerta oscureciendo la pantalla
        panelGimnasio.animarSalidaGimnasio(() -> {
            // 2. Al llegar a la salida con la pantalla oscurecida, cerramos el día en el controlador
            // TEMA: try / catch / finally: manejo de IllegalStateException si no se puede cerrar el día
            try {
                boolean subioDeNivel = controlador.cerrarDia();
                actualizarVista();

                if (subioDeNivel) {
                    String proximaEtapa = controlador.getNivelActual().getNombre();
                    JOptionPane.showMessageDialog(this,
                            "¡Superaste la etapa! Ahora te toca prepararte para la " + proximaEtapa + ".",
                            "Progreso", JOptionPane.INFORMATION_MESSAGE);
                }

                verificarFinDePartida();
                if (panelGimnasio == null) {
                    return;     // la partida terminó y se reemplazó por la pantalla final
                }

                // 3. Nuevo día: la pantalla se aclara mientras el personaje camina desde la entrada al centro
                panelGimnasio.animarEntradaGimnasio(() -> {
                    if (controlador.hayEventoPendiente()) {
                        mostrarDialogoEvento();
                    }
                    verificarFinDePartida();
                    actualizarVista();
                });
            } catch (IllegalStateException ex) {
                mostrarCartel(ex.getMessage());
            } finally {
                actualizarVista();
            }
        });
    }

    private void mostrarDialogoEvento() {
        EventoAleatorio evento = controlador.getEventoPendiente();
        DialogoEvento dialogo = new DialogoEvento(this, evento);
        dialogo.setVisible(true);   // como es modal, la ejecución se detiene acá hasta que el jugador elige
        controlador.resolverEvento(dialogo.getIndiceSeleccionado());
    }

    public void actualizarVista() {
        if (panelEstadisticas != null) {
            panelEstadisticas.actualizar();
        }
        if (panelMusculos != null) {
            panelMusculos.actualizar();
        }
        if (panelGimnasio != null) {
            panelGimnasio.repaint();
        }
        repaint();
    }

    private void verificarFinDePartida() {
        if (controlador.verificarVictoria()) {
            Personaje p = controlador.getPersonaje();
            mostrarPantallaFin("¡GANASTE LA COPA DEL BARRIO!",
                    "Saliste campeón en la categoría " + p.getObjetivo()
                            + ". Hace unos días ni te animabas a entrar al gimnasio.");
        } else if (controlador.perdioPorBurnout()) {
            mostrarPantallaFin("ABANDONASTE LA COPA",
                    "Te quedaste sin ganas de seguir entrenando. Quizás el año que viene…");
        } else if (controlador.perdioPorTiempo()) {
            String etapa = controlador.getNivelActual().getNombre();
            mostrarPantallaFin("QUEDASTE AFUERA",
                    "Llegó el día de la " + etapa + " y no alcanzaste la preparación necesaria.");
        }
    }

    // Reemplaza el gimnasio por el resumen final (evita que la partida siga después de ganar o perder)
    private void mostrarPantallaFin(String tituloResultado, String mensajeHistoria) {
        Personaje p = controlador.getPersonaje();

        JLabel lblTitulo = new JLabel(tituloResultado, SwingConstants.CENTER);
        lblTitulo.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        lblTitulo.setForeground(PanelEstadisticas.COLOR_BOTON_TEXTO);

        JLabel lblHistoria = new JLabel("<html><div style='text-align:center;'>"
                + mensajeHistoria + "<br><br><b>" + p.getNombre() + "</b> terminó con "
                + "<b>" + p.getFuerza() + "</b> de fuerza y <b>" + p.getResistencia() + "</b> de resistencia."
                + "</div></html>", SwingConstants.CENTER);
        lblHistoria.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        lblHistoria.setForeground(PanelEstadisticas.COLOR_BOTON_CONTORNO);

        JPanel encabezado = new JPanel(new BorderLayout(0, 10));
        encabezado.setOpaque(false);
        encabezado.add(lblTitulo, BorderLayout.NORTH);
        encabezado.add(lblHistoria, BorderLayout.SOUTH);

        // TEMA: ordenamiento natural con Comparable (Collections.sort usa RegistroDia.compareTo)
        List<GestorJuego.RegistroDia> historialOrdenado = new ArrayList<>(controlador.getHistorial());
        Collections.sort(historialOrdenado);

        StringBuilder sb = new StringBuilder("HISTORIAL DE ENTRENAMIENTO (día por día):\n\n");
        if (historialOrdenado.isEmpty()) {
            sb.append("Terminaste la partida en el primer día.\n");
        } else {
            for (GestorJuego.RegistroDia reg : historialOrdenado) {
                sb.append(" • ").append(reg).append("\n");
            }
        }

        JTextArea areaHistorial = new JTextArea(sb.toString(), 10, 50);
        areaHistorial.setEditable(false);
        areaHistorial.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        areaHistorial.setBackground(PanelEstadisticas.COLOR_BORDE_OSCURO);
        areaHistorial.setForeground(PanelEstadisticas.COLOR_TEXTO);
        areaHistorial.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JScrollPane scroll = new JScrollPane(areaHistorial);
        scroll.setBorder(BorderFactory.createLineBorder(PanelEstadisticas.COLOR_BOTON_CONTORNO, 3));

        JButton btnJugarDeNuevo = new JButton("JUGAR DE NUEVO");
        estilizarBotonGrandeMenu(btnJugarDeNuevo);
        btnJugarDeNuevo.addActionListener(new OyenteJugarDeNuevo());

        JPanel surBoton = new JPanel();
        surBoton.setOpaque(false);
        surBoton.add(btnJugarDeNuevo);

        PanelPlacaPixelArt tarjeta = new PanelPlacaPixelArt(new BorderLayout(0, 18));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(24, 28, 28, 28));
        tarjeta.add(encabezado, BorderLayout.NORTH);
        tarjeta.add(scroll, BorderLayout.CENTER);
        tarjeta.add(surBoton, BorderLayout.SOUTH);

        PanelFondoMenuPixelArt contenido = new PanelFondoMenuPixelArt();
        contenido.setLayout(new BorderLayout());
        contenido.setBorder(BorderFactory.createEmptyBorder(70, 150, 70, 150));
        contenido.add(tarjeta, BorderLayout.CENTER);

        panelEstadisticas = null;
        panelGimnasio = null;
        panelMusculos = null;
        setContentPane(contenido);
        revalidate();
        repaint();
    }

    private void mostrarCartel(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    // Coordenadas de parada invisibles ubicadas en el piso al costado de cada estación (sobre lienzo 960x500)
    private List<EstacionGimnasio> crearEstaciones() {
        List<EstacionGimnasio> estaciones = new ArrayList<>();
        estaciones.add(new MaquinaMuscular("Press de pecho", 280, 235, TipoMusculo.PECHO));
        estaciones.add(new MaquinaMuscular("Press militar", 410, 180, TipoMusculo.HOMBROS));
        estaciones.add(new MaquinaMuscular("Polea / Remo", 500, 150, TipoMusculo.ESPALDA));
        estaciones.add(new MaquinaMuscular("Prensa de piernas", 585, 190, TipoMusculo.PIERNAS));
        estaciones.add(new MaquinaMuscular("Máquina de brazos", 725, 245, TipoMusculo.BRAZOS));
        estaciones.add(new Cardio("Cinta de correr", 355, 355));
        estaciones.add(new Heladera("Heladera", 145, 295));
        estaciones.add(new BancoDescanso("Banco de descanso", 640, 365));
        return estaciones;
    }

    // Valores SUGERIDOS: a mayor nivel, objetivo más exigente y menos días. Ajustar jugando
    private List<Nivel> crearNiveles() {
        List<Nivel> niveles = new ArrayList<>();
        niveles.add(new Nivel(1, "Clasificatoria", 100, 60, 5));
        niveles.add(new Nivel(2, "Semifinal", 250, 150, 4));
        niveles.add(new Nivel(3, "Final", 450, 270, 3));
        return niveles;
    }

    // Panel contenedor con la misma estética 3D pixel-art (#9DBCD0, bisel #3E617D, borde #283D4E)
    private static class PanelPlacaPixelArt extends JPanel {

        private PanelPlacaPixelArt(BorderLayout layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();
            int b = 3;
            int bisel = 6;

            g2.setColor(PanelEstadisticas.COLOR_BOTON_CONTORNO);
            g2.fillRect(b, 0, w - b * 2, h);
            g2.fillRect(0, b, w, h - b * 2);

            g2.setColor(PanelEstadisticas.COLOR_BOTON_BISEL);
            g2.fillRect(b, b, w - b * 2, h - b * 2);

            int altoCara = h - b * 2 - bisel;
            g2.setColor(PanelEstadisticas.COLOR_BOTON_CARA);
            g2.fillRect(b, b, w - b * 2, altoCara);

            g2.setColor(PanelEstadisticas.COLOR_BOTON_BRILLO);
            g2.fillRect(b + 2, b, w - (b + 2) * 2, 2);
            g2.fillRect(b, b + 2, 2, Math.max(0, altoCara - 4));
            g2.fillRect(b + 2, b + altoCara - 2, w - (b + 2) * 2, 2);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Fondo del menú principal que dibuja el gimnasio completo con sus máquinas y el marco perimetral 3D pixel-art
    private class PanelFondoMenuPixelArt extends JPanel {

        private final Image imgEscenario;
        private final Image imgPecho;
        private final Image imgMilitar;
        private final Image imgRemo;
        private final Image imgPiernas;
        private final Image imgBrazos;
        private final Image imgCinta;
        private final Image imgHeladera;
        private final Image imgBanco;
        private final Image imgMancuernas;
        private final Image imgPlanta;

        private PanelFondoMenuPixelArt() {
            setBackground(COLOR_FONDO_PIZARRA);
            Image base = cargarImagenRecurso("/recursos/sprites/escenario_gimnasio.png");
            imgEscenario = (base != null) ? base : cargarImagenRecurso("/recursos/referencia_gimnasio.png");
            imgPecho = cargarImagenRecurso("/recursos/sprites/press_pecho.png");
            imgMilitar = cargarImagenRecurso("/recursos/sprites/press_militar.png");
            imgRemo = cargarImagenRecurso("/recursos/sprites/remo.png");
            imgPiernas = cargarImagenRecurso("/recursos/sprites/prensa_piernas.png");
            imgBrazos = cargarImagenRecurso("/recursos/sprites/maquina_brazos.png");
            imgCinta = cargarImagenRecurso("/recursos/sprites/cinta.png");
            imgHeladera = cargarImagenRecurso("/recursos/sprites/heladera.png");
            imgBanco = cargarImagenRecurso("/recursos/sprites/banco_descanso.png");
            imgMancuernas = cargarImagenRecurso("/recursos/sprites/mancuernas.png");
            imgPlanta = cargarImagenRecurso("/recursos/sprites/planta.png");
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            int w = getWidth();
            int h = getHeight();

            // 1. Escenario del gimnasio y todos sus equipos de fondo
            if (imgEscenario != null) {
                g2.drawImage(imgEscenario, 0, 0, w, h, this);
            }
            dibujarElementoGimnasio(g2, imgPecho, 220, 204, 0.76, w, h);
            dibujarElementoGimnasio(g2, imgMilitar, 350, 140, 0.72, w, h);
            dibujarElementoGimnasio(g2, imgRemo, 500, 102, 0.76, w, h);
            dibujarElementoGimnasio(g2, imgPiernas, 665, 148, 0.76, w, h);
            dibujarElementoGimnasio(g2, imgBrazos, 805, 212, 0.62, w, h);
            dibujarElementoGimnasio(g2, imgHeladera, 85, 275, 0.70, w, h);
            dibujarElementoGimnasio(g2, imgCinta, 255, 385, 0.78, w, h);
            dibujarElementoGimnasio(g2, imgBanco, 730, 390, 0.82, w, h);
            dibujarElementoGimnasio(g2, imgMancuernas, 825, 345, 0.74, w, h);
            dibujarElementoGimnasio(g2, imgPlanta, 895, 265, 0.72, w, h);

            // 2. Velo atmosférico suave para que el gimnasio luzca nítido detrás y resalten los botones 3D
            g2.setColor(new Color(18, 24, 34, 125));
            g2.fillRect(0, 0, w, h);

            // 3. Sombra interna del marco sobre el gimnasio
            int m = 18;
            g2.setColor(new Color(12, 16, 24, 180));
            g2.fillRect(m, m, w - m * 2, 6);
            g2.fillRect(m, h - m - 6, w - m * 2, 6);
            g2.fillRect(m, m, 6, h - m * 2);
            g2.fillRect(w - m - 6, m, 6, h - m * 2);

            // 4. Marco perimetral pixel-art en acero (#9DBCD0) con borde oscuro (#283D4E) y bisel (#3E617D)
            g2.setColor(PanelEstadisticas.COLOR_BOTON_CONTORNO);
            g2.fillRect(0, 0, w, m);
            g2.fillRect(0, h - m, w, m);
            g2.fillRect(0, 0, m, h);
            g2.fillRect(w - m, 0, m, h);

            g2.setColor(PanelEstadisticas.COLOR_BOTON_CARA);
            g2.fillRect(3, 3, w - 6, m - 7);
            g2.fillRect(3, h - m + 3, w - 6, m - 7);
            g2.fillRect(3, 3, m - 6, h - 6);
            g2.fillRect(w - m + 3, 3, m - 6, h - 6);

            g2.setColor(PanelEstadisticas.COLOR_BOTON_BISEL);
            g2.fillRect(3, m - 4, w - 6, 2);
            g2.fillRect(3, h - 4, w - 6, 2);

            // Bloques decorativos en las 4 esquinas del marco
            dibujarBloqueEsquina(g2, 0, 0);
            dibujarBloqueEsquina(g2, w - 28, 0);
            dibujarBloqueEsquina(g2, 0, h - 28);
            dibujarBloqueEsquina(g2, w - 28, h - 28);

            g2.dispose();
        }

        private void dibujarElementoGimnasio(Graphics2D g2, Image sprite,
                                             int xLogico, int yLogico, double factor, int w, int h) {
            if (sprite == null) {
                return;
            }
            int anchoOrig = sprite.getWidth(this);
            int altoOrig = sprite.getHeight(this);
            if (anchoOrig <= 0 || altoOrig <= 0) {
                return;
            }
            int x = (xLogico * Math.max(1, w)) / 960;
            int y = (yLogico * Math.max(1, h)) / 500;
            int anchoEsc = (int) Math.round((anchoOrig * Math.max(1, w) * factor) / 1679.0);
            int altoEsc = (int) Math.round((altoOrig * Math.max(1, h) * factor) / 937.0);
            g2.drawImage(sprite, x - anchoEsc / 2, y - altoEsc / 2, anchoEsc, altoEsc, this);
        }

        private void dibujarBloqueEsquina(Graphics2D g2, int x, int y) {
            g2.setColor(PanelEstadisticas.COLOR_BOTON_CONTORNO);
            g2.fillRect(x, y, 28, 28);
            g2.setColor(PanelEstadisticas.COLOR_BOTON_BISEL);
            g2.fillRect(x + 3, y + 3, 22, 22);
            g2.setColor(PanelEstadisticas.COLOR_BOTON_CARA);
            g2.fillRect(x + 3, y + 3, 22, 17);
            g2.setColor(PanelEstadisticas.COLOR_BOTON_BRILLO);
            g2.fillRect(x + 5, y + 5, 18, 2);
        }
    }

    // TEMA: clases internas (no estáticas): conectan los eventos de Swing con los métodos de la ventana
    private class OyenteIniciarPartida implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            iniciarPartida();
        }
    }

    private class OyenteCerrarDia implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            cerrarDiaActual();
        }
    }

    private class OyenteClickEstacion implements PanelGimnasio.OyenteEstacion {
        @Override
        public void alSeleccionarEstacion(EstacionGimnasio estacion) {
            manejarClickEstacion(estacion);
        }
    }

    private class OyenteJugarDeNuevo implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            mostrarPantallaInicio();
        }
    }
}
