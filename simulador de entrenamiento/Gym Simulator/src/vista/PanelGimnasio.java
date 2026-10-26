package vista;

import controlador.GestorJuego;
import modelo.BancoDescanso;
import modelo.Cardio;
import modelo.EstacionGimnasio;
import modelo.Heladera;
import modelo.MaquinaMuscular;
import modelo.Personaje;
import modelo.TipoMusculo;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

// TEMA: herencia: PanelGimnasio ES UN JPanel que dibuja el escenario, los sprites de estaciones y del personaje
public class PanelGimnasio extends JPanel {

    private static final int ANCHO_LOGICO = 960;
    private static final int ALTO_LOGICO = 500;

    private static final int RADIO_CLICK = 52;
    private static final int POS_INICIAL_X = 480;
    private static final int POS_INICIAL_Y = 250;

    private static final int CANTIDAD_PASOS_MOVIMIENTO = 20;
    private static final int PAUSA_ENTRE_PASOS_MS = 18;

    private static final Color COLOR_ETIQUETA_FONDO = new Color(24, 30, 41, 215);
    private static final Color COLOR_ETIQUETA_BLOQUEADA = new Color(160, 40, 40, 225);
    private static final Color COLOR_PERSONAJE_SOMBRA = new Color(15, 20, 28, 130);

    private static final Font FUENTE_ESTACION = new Font(Font.MONOSPACED, Font.BOLD, 11);
    private static final Font FUENTE_COSTO = new Font(Font.MONOSPACED, Font.PLAIN, 10);

    // TEMA: interfaz propia con su contrato (notifica el click sobre una estación)
    public interface OyenteEstacion {
        void alSeleccionarEstacion(EstacionGimnasio estacion);
    }

    private final GestorJuego controlador;
    private final OyenteEstacion oyenteEstacion;

    private Image imagenEscenario;
    // TEMA: colecciones genéricas (Map) para asociar cada clave de estación con su imagen individual
    private final Map<String, Image> spritesEstaciones;
    // TEMA: arrays (Image[]) con los cuadros de animación del personaje caminando
    private final Image[] cuadrosPersonaje;

    // La posición del personaje vive en la vista (estado de presentación, DISENO_CLASES sección 6)
    private int personajeX;
    private int personajeY;
    private boolean enMovimiento;
    private int pasoAnimacion;

    public PanelGimnasio(GestorJuego controlador, OyenteEstacion oyenteEstacion) {
        this.controlador = controlador;
        this.oyenteEstacion = oyenteEstacion;
        this.personajeX = POS_INICIAL_X;
        this.personajeY = POS_INICIAL_Y;
        this.enMovimiento = false;
        this.pasoAnimacion = 0;

        this.spritesEstaciones = new HashMap<>();
        this.cuadrosPersonaje = new Image[2];

        setBackground(VentanaPrincipal.COLOR_FONDO_PIZARRA);
        cargarSprites();

        // TEMA: manejo de eventos con clase interna (MouseListener)
        addMouseListener(new OyenteMouseGimnasio());
    }

    private void cargarSprites() {
        // 1. Escenario sin el personaje fijo en el centro
        imagenEscenario = cargarImagen("/recursos/sprites/escenario_gimnasio.png");
        if (imagenEscenario == null) {
            imagenEscenario = cargarImagen("/recursos/referencia_gimnasio.png");
        }

        // 2. Cuadros del personaje (en un Image[])
        cuadrosPersonaje[0] = cargarImagen("/recursos/sprites/personaje_1.png");
        cuadrosPersonaje[1] = cargarImagen("/recursos/sprites/personaje_2.png");

        // 3. Cada objeto/estación del gimnasio como imagen independiente
        spritesEstaciones.put("PECHO", cargarImagen("/recursos/sprites/press_pecho.png"));
        spritesEstaciones.put("HOMBROS", cargarImagen("/recursos/sprites/press_militar.png"));
        spritesEstaciones.put("ESPALDA", cargarImagen("/recursos/sprites/remo.png"));
        spritesEstaciones.put("PIERNAS", cargarImagen("/recursos/sprites/prensa_piernas.png"));
        spritesEstaciones.put("BRAZOS", cargarImagen("/recursos/sprites/maquina_brazos.png"));
        spritesEstaciones.put("CARDIO", cargarImagen("/recursos/sprites/cinta.png"));
        spritesEstaciones.put("HELADERA", cargarImagen("/recursos/sprites/heladera.png"));
        spritesEstaciones.put("DESCANSO", cargarImagen("/recursos/sprites/banco_descanso.png"));
    }

    private Image cargarImagen(String ruta) {
        // TEMA: try / catch con IOException al cargar un archivo de imagen
        try {
            URL recurso = getClass().getResource(ruta);
            if (recurso != null) {
                return ImageIO.read(recurso);
            }
        } catch (IOException ex) {
            // Si falla la lectura de un sprite, devolvemos null y se usa dibujo de respaldo
        }
        return null;
    }

    public void ubicarPersonajeEn(int x, int y) {
        this.personajeX = x;
        this.personajeY = y;
        this.pasoAnimacion = 0;
        repaint();
    }

    public boolean estaEnMovimiento() {
        return enMovimiento;
    }

    // TEMA: hilos de ejecución separados (Thread + Runnable).
    // Mueve el sprite del personaje paso a paso hacia la estación y, al llegar, ejecuta accionAlLlegar
    // en el hilo de eventos de Swing (Event Dispatch Thread).
    public void moverPersonajeHacia(int destinoX, int destinoY, Runnable accionAlLlegar) {
        if (enMovimiento) {
            return;
        }
        enMovimiento = true;
        Thread hilo = new Thread(new MovimientoPersonaje(destinoX, destinoY, accionAlLlegar),
                "Hilo-Movimiento-Personaje");
        hilo.start();
    }

    public int getPersonajeX() { return personajeX; }
    public int getPersonajeY() { return personajeY; }

    // TEMA: sobreescritura (@Override) de paintComponent para dibujo personalizado en Swing
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // TEMA: casteo de referencia (Graphics -> Graphics2D) para configurar nitidez pixel art
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Capa 1: Escenario base (sin el personaje fijo en el centro)
        if (imagenEscenario != null) {
            g2.drawImage(imagenEscenario, 0, 0, getWidth(), getHeight(), this);
        }

        // Capa 2: Cada estación con su sprite individual y su etiqueta encima
        Personaje personaje = controlador.getPersonaje();
        for (EstacionGimnasio estacion : controlador.getEstaciones()) {
            int ex = escalarX(estacion.getX());
            int ey = escalarY(estacion.getY());
            dibujarSpriteEstacion(g2, estacion, ex, ey);
            dibujarEtiquetaEstacion(g2, estacion, personaje, ex, ey);
        }

        // Capa 3: Sprite independiente del personaje en su coordenada actual (móvil)
        int px = escalarX(personajeX);
        int py = escalarY(personajeY);
        dibujarPersonaje(g2, personaje.getNombre(), px, py);
    }

    private void dibujarSpriteEstacion(Graphics2D g2, EstacionGimnasio estacion, int x, int y) {
        Image sprite = obtenerSpriteDeEstacion(estacion);
        if (sprite == null) {
            return;
        }
        // Escalamos proporcionalmente del tamaño original (1679x937) al panel actual
        int anchoOriginal = sprite.getWidth(this);
        int altoOriginal = sprite.getHeight(this);
        if (anchoOriginal <= 0 || altoOriginal <= 0) {
            return;
        }
        int anchoEscalado = (anchoOriginal * Math.max(1, getWidth())) / 1679;
        int altoEscalado = (altoOriginal * Math.max(1, getHeight())) / 937;
        g2.drawImage(sprite, x - anchoEscalado / 2, y - altoEscalado / 2,
                anchoEscalado, altoEscalado, this);
    }

    private Image obtenerSpriteDeEstacion(EstacionGimnasio estacion) {
        if (estacion instanceof MaquinaMuscular) {
            TipoMusculo musculo = ((MaquinaMuscular) estacion).getGrupoQueTrabaja();
            return spritesEstaciones.get(musculo.name());
        } else if (estacion instanceof Cardio) {
            return spritesEstaciones.get("CARDIO");
        } else if (estacion instanceof Heladera) {
            return spritesEstaciones.get("HELADERA");
        } else if (estacion instanceof BancoDescanso) {
            return spritesEstaciones.get("DESCANSO");
        }
        return null;
    }

    private void dibujarEtiquetaEstacion(Graphics2D g2, EstacionGimnasio estacion,
                                         Personaje personaje, int x, int y) {
        boolean usable = estacion.puedeUsarse(personaje);
        String titulo = estacion.getNombre().toUpperCase();
        String subtitulo = armarSubtituloEstacion(estacion, usable);

        g2.setFont(FUENTE_ESTACION);
        FontMetrics fmTitulo = g2.getFontMetrics();
        g2.setFont(FUENTE_COSTO);
        FontMetrics fmSub = g2.getFontMetrics();

        int anchoCaja = Math.max(fmTitulo.stringWidth(titulo), fmSub.stringWidth(subtitulo)) + 14;
        int altoCaja = 30;
        int cajaX = x - anchoCaja / 2;
        int cajaY = y - 52;

        // Punto interactivo sobre la estación
        g2.setColor(usable ? new Color(76, 201, 91, 190) : new Color(224, 70, 70, 200));
        g2.fillOval(x - 6, y - 6, 12, 12);
        g2.setColor(PanelEstadisticas.COLOR_BORDE_OSCURO);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(x - 6, y - 6, 12, 12);

        // Cartel retro encima de la máquina
        g2.setColor(usable ? COLOR_ETIQUETA_FONDO : COLOR_ETIQUETA_BLOQUEADA);
        g2.fillRect(cajaX, cajaY, anchoCaja, altoCaja);
        g2.setColor(usable ? PanelEstadisticas.COLOR_BORDE_CLARO : new Color(255, 120, 120));
        g2.drawRect(cajaX, cajaY, anchoCaja, altoCaja);

        g2.setFont(FUENTE_ESTACION);
        g2.setColor(PanelEstadisticas.COLOR_TEXTO);
        g2.drawString(titulo, x - fmTitulo.stringWidth(titulo) / 2, cajaY + 13);

        g2.setFont(FUENTE_COSTO);
        g2.setColor(usable ? new Color(242, 201, 56) : new Color(255, 200, 200));
        g2.drawString(subtitulo, x - fmSub.stringWidth(subtitulo) / 2, cajaY + 25);
    }

    private String armarSubtituloEstacion(EstacionGimnasio estacion, boolean usable) {
        if (!usable) {
            return "LESIONADO";
        }
        if (estacion instanceof MaquinaMuscular) {
            MaquinaMuscular maquina = (MaquinaMuscular) estacion;
            return maquina.getGrupoQueTrabaja().name() + " · " + estacion.getCostoTiempo() + " min";
        }
        return estacion.getCostoTiempo() + " min (click)";
    }

    private void dibujarPersonaje(Graphics2D g2, String nombre, int x, int y) {
        // Sombra debajo de los pies del personaje
        g2.setColor(COLOR_PERSONAJE_SOMBRA);
        g2.fillOval(x - 18, y + 26, 36, 12);

        // Alternamos entre los 2 cuadros del arreglo Image[] mientras camina
        int indiceCuadro = (enMovimiento && pasoAnimacion % 2 == 1) ? 1 : 0;
        Image spritePersonaje = cuadrosPersonaje[indiceCuadro];

        if (spritePersonaje != null) {
            int anchoSprite = (spritePersonaje.getWidth(this) * Math.max(1, getWidth())) / 1679;
            int altoSprite = (spritePersonaje.getHeight(this) * Math.max(1, getHeight())) / 937;
            g2.drawImage(spritePersonaje, x - anchoSprite / 2, y - altoSprite / 2,
                    anchoSprite, altoSprite, this);
        } else {
            // Respaldo si no se encontró el PNG
            g2.setColor(new Color(54, 163, 247));
            g2.fillOval(x - 10, y - 10, 20, 20);
        }

        // Etiqueta con el nombre arriba de la cabeza del personaje
        g2.setFont(FUENTE_ESTACION);
        FontMetrics fm = g2.getFontMetrics();
        String etiqueta = "▼ " + nombre.toUpperCase();
        int ancho = fm.stringWidth(etiqueta) + 10;
        int etiquetaY = y - 54;
        g2.setColor(COLOR_ETIQUETA_FONDO);
        g2.fillRect(x - ancho / 2, etiquetaY, ancho, 18);
        g2.setColor(new Color(54, 163, 247));
        g2.drawRect(x - ancho / 2, etiquetaY, ancho, 18);
        g2.setColor(PanelEstadisticas.COLOR_TEXTO);
        g2.drawString(etiqueta, x - fm.stringWidth(etiqueta) / 2, etiquetaY + 13);
    }

    private int escalarX(int xBase) {
        return (xBase * Math.max(1, getWidth())) / ANCHO_LOGICO;
    }

    private int escalarY(int yBase) {
        return (yBase * Math.max(1, getHeight())) / ALTO_LOGICO;
    }

    private int desescalarX(int xPantalla) {
        return (xPantalla * ANCHO_LOGICO) / Math.max(1, getWidth());
    }

    private int desescalarY(int yPantalla) {
        return (yPantalla * ALTO_LOGICO) / Math.max(1, getHeight());
    }

    private EstacionGimnasio buscarEstacionEn(int xLogico, int yLogico) {
        for (EstacionGimnasio estacion : controlador.getEstaciones()) {
            int dx = xLogico - estacion.getX();
            int dy = yLogico - estacion.getY();
            double distancia = Math.sqrt(dx * dx + dy * dy);
            if (distancia <= RADIO_CLICK) {
                return estacion;
            }
        }
        return null;
    }

    // TEMA: clase interna para escuchar los clicks del mouse sobre las estaciones del mapa
    private class OyenteMouseGimnasio extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (enMovimiento) {
                return;     // ignora clicks mientras el personaje está caminando hacia una estación
            }
            int xLogico = desescalarX(e.getX());
            int yLogico = desescalarY(e.getY());
            EstacionGimnasio elegida = buscarEstacionEn(xLogico, yLogico);
            if (elegida != null && oyenteEstacion != null) {
                oyenteEstacion.alSeleccionarEstacion(elegida);
            }
        }
    }

    // TEMA: hilos de ejecución separados: clase interna que implementa Runnable (Unidad 6)
    private class MovimientoPersonaje implements Runnable {

        private final int destinoX;
        private final int destinoY;
        private final Runnable accionAlLlegar;

        private MovimientoPersonaje(int destinoX, int destinoY, Runnable accionAlLlegar) {
            this.destinoX = destinoX;
            this.destinoY = destinoY;
            this.accionAlLlegar = accionAlLlegar;
        }

        @Override
        public void run() {
            int origenX = personajeX;
            int origenY = personajeY;

            // TEMA: try / catch / finally: garantiza que al terminar (o si se interrumpe) se destrabe el estado
            try {
                for (int i = 1; i <= CANTIDAD_PASOS_MOVIMIENTO; i++) {
                    final int nuevoX = origenX + ((destinoX - origenX) * i) / CANTIDAD_PASOS_MOVIMIENTO;
                    final int nuevoY = origenY + ((destinoY - origenY) * i) / CANTIDAD_PASOS_MOVIMIENTO;
                    final int pasoActual = i;

                    // Los cambios visuales de Swing se encolan en el Event Dispatch Thread
                    SwingUtilities.invokeLater(() -> {
                        personajeX = nuevoX;
                        personajeY = nuevoY;
                        pasoAnimacion = pasoActual;
                        repaint();
                    });

                    Thread.sleep(PAUSA_ENTRE_PASOS_MS);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                SwingUtilities.invokeLater(() -> {
                    personajeX = destinoX;
                    personajeY = destinoY;
                    pasoAnimacion = 0;
                    enMovimiento = false;
                    repaint();
                    if (accionAlLlegar != null) {
                        accionAlLlegar.run();
                    }
                });
            }
        }
    }
}
