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

import javax.swing.BorderFactory;
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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private final JTextField txtEdad;
    private final JComboBox<Objetivo> cbObjetivo;
    private final JButton btnIniciarPartida;

    public VentanaPrincipal() {
        super("Simulador de Entrenamiento");      // TEMA: super(...): el constructor de JFrame recibe el título
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(ANCHO, ALTO);
        setLocationRelativeTo(null);              // centra la ventana en la pantalla
        setResizable(false);

        txtNombre = new JTextField(15);
        txtEdad = new JTextField(5);
        // TEMA: arrays: Objetivo.values() devuelve un Objetivo[] con las 3 opciones del combo
        cbObjetivo = new JComboBox<>(Objetivo.values());
        btnIniciarPartida = new JButton("INICIAR PARTIDA");
        PanelEstadisticas.estilizarBotonRetro(btnIniciarPartida);
        // TEMA: manejo de eventos: el botón avisa a su oyente cada vez que lo clickean
        btnIniciarPartida.addActionListener(new OyenteIniciarPartida());

        mostrarPantallaInicio();
    }

    private void mostrarPantallaInicio() {
        Font fuenteRetro = new Font(Font.MONOSPACED, Font.BOLD, 14);

        JLabel titulo = new JLabel("SIMULADOR DE ENTRENAMIENTO", SwingConstants.CENTER);
        titulo.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        titulo.setForeground(PanelEstadisticas.COLOR_TEXTO);

        JLabel premisa = new JLabel("<html><div style='text-align:center;'>"
                + "Hace años que dejaste de entrenar. Pasás por el gimnasio del barrio y ves el cartel:<br>"
                + "<b>COPA DEL BARRIO – Inscripciones abiertas</b>.<br>"
                + "Te anotás sin pensarlo. Tenés pocos días para prepararte… y no te podés rendir."
                + "</div></html>", SwingConstants.CENTER);
        premisa.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        premisa.setForeground(PanelEstadisticas.COLOR_BORDE_CLARO);

        JPanel encabezado = new JPanel(new BorderLayout(0, 10));
        encabezado.setOpaque(false);
        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(premisa, BorderLayout.SOUTH);

        // Grilla de 3 filas x 2 columnas: etiqueta a la izquierda, campo a la derecha
        JPanel formulario = new JPanel(new GridLayout(3, 2, 10, 12));
        formulario.setOpaque(false);

        JLabel lblNombre = new JLabel("NOMBRE:");
        lblNombre.setForeground(PanelEstadisticas.COLOR_TEXTO);
        lblNombre.setFont(fuenteRetro);

        JLabel lblEdad = new JLabel("EDAD:");
        lblEdad.setForeground(PanelEstadisticas.COLOR_TEXTO);
        lblEdad.setFont(fuenteRetro);

        JLabel lblObjetivo = new JLabel("OBJETIVO:");
        lblObjetivo.setForeground(PanelEstadisticas.COLOR_TEXTO);
        lblObjetivo.setFont(fuenteRetro);

        txtNombre.setFont(fuenteRetro);
        txtEdad.setFont(fuenteRetro);
        cbObjetivo.setFont(fuenteRetro);

        formulario.add(lblNombre);
        formulario.add(txtNombre);
        formulario.add(lblEdad);
        formulario.add(txtEdad);
        formulario.add(lblObjetivo);
        formulario.add(cbObjetivo);

        JPanel tarjeta = new JPanel(new BorderLayout(0, 22));
        tarjeta.setBackground(PanelEstadisticas.COLOR_FONDO_HUD);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PanelEstadisticas.COLOR_BORDE_CLARO, 3),
                BorderFactory.createEmptyBorder(28, 32, 28, 32)
        ));
        tarjeta.add(encabezado, BorderLayout.NORTH);
        tarjeta.add(formulario, BorderLayout.CENTER);
        tarjeta.add(btnIniciarPartida, BorderLayout.SOUTH);

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setBackground(COLOR_FONDO_PIZARRA);
        contenido.setBorder(BorderFactory.createEmptyBorder(120, 200, 0, 200));
        contenido.add(tarjeta, BorderLayout.NORTH);

        setContentPane(contenido);
        revalidate();
        repaint();
    }

    private void iniciarPartida() {
        // TEMA: try / catch: un error del usuario se muestra en un cartel y el programa sigue andando
        try {
            String nombre = txtNombre.getText();
            int edad = Integer.parseInt(txtEdad.getText().trim());          // puede lanzar NumberFormatException
            Objetivo objetivo = (Objetivo) cbObjetivo.getSelectedItem();    // TEMA: casteo de referencia (Object -> Objetivo)
            Personaje personaje = new Personaje(nombre, edad, objetivo);    // puede lanzar DatosInvalidosException

            controlador = new GestorJuego(personaje, crearEstaciones(), crearNiveles(),
                    CatalogoEventos.crearEventos(), new Random());
            mostrarPantallaJuego();
        } catch (NumberFormatException ex) {
            // TEMA: manejo de excepción común de Java (unchecked)
            mostrarCartel("La edad tiene que ser un número entero");
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
        if (panelGimnasio != null && panelGimnasio.estaEnMovimiento()) {
            return;     // espera a que termine de caminar antes de cerrar el día
        }
        // TEMA: try / catch / finally: si hay un evento pendiente sin resolver, cerrarDia lanza IllegalStateException
        try {
            boolean subioDeNivel = controlador.cerrarDia();
            panelGimnasio.ubicarPersonajeEn(480, 250);              // vuelve al centro al empezar un nuevo día
            if (subioDeNivel) {
                String proximaEtapa = controlador.getNivelActual().getNombre();
                JOptionPane.showMessageDialog(this,
                        "¡Superaste la etapa! Ahora te toca prepararte para la " + proximaEtapa + ".",
                        "Progreso", JOptionPane.INFORMATION_MESSAGE);
            }
            if (controlador.hayEventoPendiente()) {
                mostrarDialogoEvento();
            }
            verificarFinDePartida();
        } catch (IllegalStateException ex) {
            mostrarCartel(ex.getMessage());
        } finally {
            actualizarVista();
        }
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
        lblTitulo.setForeground(PanelEstadisticas.COLOR_TEXTO);

        JLabel lblHistoria = new JLabel("<html><div style='text-align:center;'>"
                + mensajeHistoria + "<br><br><b>" + p.getNombre() + "</b> terminó con "
                + "<b>" + p.getFuerza() + "</b> de fuerza y <b>" + p.getResistencia() + "</b> de resistencia."
                + "</div></html>", SwingConstants.CENTER);
        lblHistoria.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        lblHistoria.setForeground(PanelEstadisticas.COLOR_BORDE_CLARO);

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
        scroll.setBorder(BorderFactory.createLineBorder(PanelEstadisticas.COLOR_BORDE_CLARO, 2));

        JButton btnJugarDeNuevo = new JButton("JUGAR DE NUEVO");
        PanelEstadisticas.estilizarBotonRetro(btnJugarDeNuevo);
        btnJugarDeNuevo.addActionListener(new OyenteJugarDeNuevo());

        JPanel tarjeta = new JPanel(new BorderLayout(0, 18));
        tarjeta.setBackground(PanelEstadisticas.COLOR_FONDO_HUD);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PanelEstadisticas.COLOR_BORDE_CLARO, 3),
                BorderFactory.createEmptyBorder(24, 28, 24, 28)
        ));
        tarjeta.add(encabezado, BorderLayout.NORTH);
        tarjeta.add(scroll, BorderLayout.CENTER);
        tarjeta.add(btnJugarDeNuevo, BorderLayout.SOUTH);

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setBackground(COLOR_FONDO_PIZARRA);
        contenido.setBorder(BorderFactory.createEmptyBorder(80, 160, 80, 160));
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

    // Coordenadas alineadas a las estaciones de referencia_gimnasio.png (sobre lienzo base 960x500)
    private List<EstacionGimnasio> crearEstaciones() {
        List<EstacionGimnasio> estaciones = new ArrayList<>();
        estaciones.add(new MaquinaMuscular("Press de pecho", 205, 195, TipoMusculo.PECHO));
        estaciones.add(new MaquinaMuscular("Press militar", 345, 130, TipoMusculo.HOMBROS));
        estaciones.add(new MaquinaMuscular("Polea / Remo", 510, 100, TipoMusculo.ESPALDA));
        estaciones.add(new MaquinaMuscular("Prensa de piernas", 675, 155, TipoMusculo.PIERNAS));
        estaciones.add(new MaquinaMuscular("Máquina de brazos", 835, 205, TipoMusculo.BRAZOS));
        estaciones.add(new Cardio("Cinta de correr", 355, 355));
        estaciones.add(new Heladera("Heladera", 65, 250));
        estaciones.add(new BancoDescanso("Banco de descanso", 670, 370));
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
