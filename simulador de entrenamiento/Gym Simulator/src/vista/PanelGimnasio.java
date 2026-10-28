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
    // Altura lógica fija del personaje en el lienzo 960x500 (independiente de la resolución en píxeles del PNG)
    private static final int ALTO_LOGICO_PERSONAJE = 82;

    private static final int RADIO_CLICK = 68;
    private static final int POS_INICIAL_X = 480;
    private static final int POS_INICIAL_Y = 250;
    private static final int POS_PUERTA_X = 480;
    private static final int POS_PUERTA_Y = 465;

    // Punto de paso corto para bordear las cintas por la derecha sin alejarse hasta el centro (480, 250)
    private static final int PASO_ESQUINA_CINTA_X = 335;
    private static final int PASO_ESQUINA_CINTA_Y = 305;

    // Movimiento a velocidad constante (~60 FPS) desacoplado de la frecuencia de cambio de sprite
    private static final int PAUSA_ENTRE_PASOS_MS = 16;
    private static final double VELOCIDAD_PX_POR_TICK = 5.5;
    private static final int PASOS_MINIMOS_MOVIMIENTO = 16;
    private static final int TICKS_POR_CUADRO_ANIMACION = 6;

    // Modos de orientación visual del personaje (costado normal, de frente hacia la salida, de espaldas al entrar)
    private static final int ORIENTACION_COSTADO = 0;
    private static final int ORIENTACION_FRENTE = 1;
    private static final int ORIENTACION_ESPALDA = 2;

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
    private Image spriteMancuernas;
    private Image spritePlanta;
    // TEMA: colecciones genéricas (Map) para asociar cada clave de estación con su imagen individual
    private final Map<String, Image> spritesEstaciones;
    // TEMA: arrays (Image[]) con los cuadros de animación de costado, de frente y de espaldas
    private final Image[] cuadrosPersonaje;
    private final Image[] cuadrosFrente;
    private final Image[] cuadrosEspalda;

    // La posición, orientación y nivel de oscuridad viven en la vista (estado de presentación, DISENO_CLASES sección 6)
    private int personajeX;
    private int personajeY;
    private boolean enMovimiento;
    private boolean mirandoIzquierda;
    private int orientacionActual;
    private int pasoAnimacion;
    private int opacidadOscuridad;  // 0 = totalmente iluminado, 255 = totalmente oscurecido

    public PanelGimnasio(GestorJuego controlador, OyenteEstacion oyenteEstacion) {
        this.controlador = controlador;
        this.oyenteEstacion = oyenteEstacion;
        this.personajeX = POS_INICIAL_X;
        this.personajeY = POS_INICIAL_Y;
        this.enMovimiento = false;
        this.mirandoIzquierda = false;
        this.orientacionActual = ORIENTACION_COSTADO;
        this.pasoAnimacion = 0;
        this.opacidadOscuridad = 0;

        this.spritesEstaciones = new HashMap<>();
        this.cuadrosPersonaje = new Image[4];
        this.cuadrosFrente = new Image[4];
        this.cuadrosEspalda = new Image[4];

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

        // 2. Ciclo de caminata lateral/normal usando personaje_1.png (neutro) y personaje_2.png (zancada)
        Image spriteIdle = cargarImagen("/recursos/sprites/personaje_1.png");
        Image spritePasoCostado = cargarImagen("/recursos/sprites/personaje_2.png");
        Image spritePasoAlt = cargarImagen("/recursos/sprites/personaje_3.png");

        cuadrosPersonaje[0] = spriteIdle;
        cuadrosPersonaje[1] = (spritePasoCostado != null) ? spritePasoCostado : spriteIdle;
        cuadrosPersonaje[2] = spriteIdle;
        cuadrosPersonaje[3] = (spritePasoAlt != null) ? spritePasoAlt : cuadrosPersonaje[1];

        // 3. Ciclo de caminata de frente (cuando se dirige hacia la salida del gimnasio)
        Image frenteDer = cargarImagen("/recursos/sprites/personaje_frente_piernaDerecha.png");
        Image frenteIzq = cargarImagen("/recursos/sprites/personaje_frente_piernaIzquierda.png");
        cuadrosFrente[0] = (frenteDer != null) ? frenteDer : spriteIdle;
        cuadrosFrente[1] = (frenteIzq != null) ? frenteIzq : cuadrosFrente[0];
        cuadrosFrente[2] = cuadrosFrente[0];
        cuadrosFrente[3] = cuadrosFrente[1];

        // 4. Ciclo de caminata de espaldas (cuando entra al gimnasio desde la puerta hacia el centro)
        Image atrasDer = cargarImagen("/recursos/sprites/personaje_de_atras_piernaDerecha.png");
        Image atrasIzq = cargarImagen("/recursos/sprites/personaje_de_atras_piernaIzquierda.png");
        cuadrosEspalda[0] = (atrasDer != null) ? atrasDer : spriteIdle;
        cuadrosEspalda[1] = (atrasIzq != null) ? atrasIzq : cuadrosEspalda[0];
        cuadrosEspalda[2] = cuadrosEspalda[0];
        cuadrosEspalda[3] = cuadrosEspalda[1];

        // 5. Cada objeto/estación del gimnasio como imagen independiente
        spritesEstaciones.put("PECHO", cargarImagen("/recursos/sprites/press_pecho.png"));
        spritesEstaciones.put("HOMBROS", cargarImagen("/recursos/sprites/press_militar.png"));
        spritesEstaciones.put("ESPALDA", cargarImagen("/recursos/sprites/remo.png"));
        spritesEstaciones.put("PIERNAS", cargarImagen("/recursos/sprites/prensa_piernas.png"));
        spritesEstaciones.put("BRAZOS", cargarImagen("/recursos/sprites/maquina_brazos.png"));
        spritesEstaciones.put("CARDIO", cargarImagen("/recursos/sprites/cinta.png"));
        spritesEstaciones.put("HELADERA", cargarImagen("/recursos/sprites/heladera.png"));
        spritesEstaciones.put("DESCANSO", cargarImagen("/recursos/sprites/banco_descanso.png"));

        // 6. Elementos decorativos del gimnasio
        spriteMancuernas = cargarImagen("/recursos/sprites/mancuernas.png");
        spritePlanta = cargarImagen("/recursos/sprites/planta.png");
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
        this.mirandoIzquierda = false;
        this.orientacionActual = ORIENTACION_COSTADO;
        this.opacidadOscuridad = 0;
        repaint();
    }

    public boolean estaEnMovimiento() {
        return enMovimiento;
    }

    // TEMA: hilos de ejecución separados (Thread + Runnable).
    // Mueve el sprite del personaje paso a paso hacia la estación. Si va entre la heladera y las estaciones
    // inferiores (cinta o banco), rodea cortito por delante de las cintas (335, 305) sin ir hasta el centro.
    public void moverPersonajeHacia(int destinoX, int destinoY, Runnable accionAlLlegar) {
        if (enMovimiento) {
            return;
        }
        orientacionActual = ORIENTACION_COSTADO;
        if (requiereBordearCinta(personajeX, personajeY, destinoX, destinoY)) {
            moverPersonajeConLuz(PASO_ESQUINA_CINTA_X, PASO_ESQUINA_CINTA_Y,
                    opacidadOscuridad, opacidadOscuridad, () -> {
                        moverPersonajeConLuz(destinoX, destinoY,
                                opacidadOscuridad, opacidadOscuridad, accionAlLlegar);
                    });
        } else {
            moverPersonajeConLuz(destinoX, destinoY, opacidadOscuridad, opacidadOscuridad, accionAlLlegar);
        }
    }

    // Detecta si el trayecto directo entre la heladera y la zona inferior pasaría por encima de las cintas
    private boolean requiereBordearCinta(int origenX, int origenY, int destinoX, int destinoY) {
        boolean origenEnHeladera = (origenX < 200 && origenY > 260);
        boolean destinoEnHeladera = (destinoX < 200 && destinoY > 260);
        boolean origenEnZonaInferior = (origenY >= 340);
        boolean destinoEnZonaInferior = (destinoY >= 340);

        return (origenEnHeladera && destinoEnZonaInferior)
                || (origenEnZonaInferior && destinoEnHeladera);
    }

    private void moverPersonajeConLuz(int destinoX, int destinoY,
                                      int opacidadInicial, int opacidadFinal,
                                      Runnable accionAlLlegar) {
        if (enMovimiento) {
            return;
        }
        enMovimiento = true;
        if (orientacionActual == ORIENTACION_COSTADO && destinoX != personajeX) {
            mirandoIzquierda = (destinoX < personajeX);
        }
        Thread hilo = new Thread(
                new MovimientoPersonaje(destinoX, destinoY, opacidadInicial, opacidadFinal, accionAlLlegar),
                "Hilo-Movimiento-Personaje"
        );
        hilo.start();
    }

    // Secuencia de salida al cerrar el día:
    // 1) Si no está en el centro, vuelve primero al centro (480, 250) caminando de costado.
    // 2) Desde el centro camina DE FRENTE hacia la puerta inferior (480, 465) mientras la pantalla se oscurece.
    public void animarSalidaGimnasio(Runnable alLlegarASalida) {
        if (enMovimiento) {
            return;
        }
        if (personajeX != POS_INICIAL_X || personajeY != POS_INICIAL_Y) {
            orientacionActual = ORIENTACION_COSTADO;
            moverPersonajeConLuz(POS_INICIAL_X, POS_INICIAL_Y, 0, 0, () -> {
                orientacionActual = ORIENTACION_FRENTE;
                mirandoIzquierda = false;
                moverPersonajeConLuz(POS_PUERTA_X, POS_PUERTA_Y, 0, 255, alLlegarASalida);
            });
        } else {
            orientacionActual = ORIENTACION_FRENTE;
            mirandoIzquierda = false;
            moverPersonajeConLuz(POS_PUERTA_X, POS_PUERTA_Y, 0, 255, alLlegarASalida);
        }
    }

    // Secuencia de entrada al comenzar un nuevo día:
    // El personaje camina DE ESPALDAS desde la puerta (480, 465) hasta el centro (480, 250) mientras se aclara.
    public void animarEntradaGimnasio(Runnable alLlegarAlCentro) {
        this.personajeX = POS_PUERTA_X;
        this.personajeY = POS_PUERTA_Y;
        this.mirandoIzquierda = false;
        this.orientacionActual = ORIENTACION_ESPALDA;
        this.opacidadOscuridad = 255;
        repaint();

        moverPersonajeConLuz(POS_INICIAL_X, POS_INICIAL_Y, 255, 0, () -> {
            orientacionActual = ORIENTACION_COSTADO;
            mirandoIzquierda = false;
            repaint();
            if (alLlegarAlCentro != null) {
                alLlegarAlCentro.run();
            }
        });
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

        // Capa 2: Sprites físicos de las estaciones y elementos decorativos en el piso/pared
        Personaje personaje = controlador.getPersonaje();
        for (EstacionGimnasio estacion : controlador.getEstaciones()) {
            int mx = escalarX(obtenerXVisualMaquina(estacion));
            int my = escalarY(obtenerYVisualMaquina(estacion));
            dibujarSpriteEstacion(g2, estacion, mx, my);
        }
        dibujarDecoracion(g2);

        // Capa 3: Sprite independiente del personaje en su coordenada actual (móvil)
        int px = escalarX(personajeX);
        int py = escalarY(personajeY);
        dibujarPersonaje(g2, personaje.getNombre(), px, py);

        // Capa 4: Carteles de interfaz (HUD) de las estaciones y del personaje siempre por encima
        for (EstacionGimnasio estacion : controlador.getEstaciones()) {
            int mx = escalarX(obtenerXVisualMaquina(estacion));
            int my = escalarY(obtenerYVisualMaquina(estacion));
            dibujarEtiquetaEstacion(g2, estacion, personaje, mx, my);
        }
        dibujarEtiquetaPersonaje(g2, personaje.getNombre(), px, py);

        // Capa 5: Velo de oscuridad progresivo al irse a casa (fade out) o empezar un nuevo día (fade in)
        if (opacidadOscuridad > 0) {
            int alpha = Math.max(0, Math.min(255, opacidadOscuridad));
            g2.setColor(new Color(10, 14, 20, alpha));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void dibujarDecoracion(Graphics2D g2) {
        // Mancuernas a la derecha del banco de descanso, apoyadas contra la pared inferior derecha
        dibujarSpriteDecorativo(g2, spriteMancuernas, 825, 345, 0.74);
        // Planta decorativa en el rincón derecho del gimnasio (junto a la máquina de brazos)
        dibujarSpriteDecorativo(g2, spritePlanta, 895, 265, 0.72);
    }

    private void dibujarSpriteDecorativo(Graphics2D g2, Image sprite, int xLogico, int yLogico, double factor) {
        if (sprite == null) {
            return;
        }
        int anchoOriginal = sprite.getWidth(this);
        int altoOriginal = sprite.getHeight(this);
        if (anchoOriginal <= 0 || altoOriginal <= 0) {
            return;
        }
        int x = escalarX(xLogico);
        int y = escalarY(yLogico);
        int anchoEscalado = (int) Math.round((anchoOriginal * Math.max(1, getWidth()) * factor) / 1679.0);
        int altoEscalado = (int) Math.round((altoOriginal * Math.max(1, getHeight()) * factor) / 937.0);

        g2.drawImage(sprite, x - anchoEscalado / 2, y - altoEscalado / 2,
                anchoEscalado, altoEscalado, this);
    }

    private void dibujarSpriteEstacion(Graphics2D g2, EstacionGimnasio estacion, int x, int y) {
        Image sprite = obtenerSpriteDeEstacion(estacion);
        if (sprite == null) {
            return;
        }
        int anchoOriginal = sprite.getWidth(this);
        int altoOriginal = sprite.getHeight(this);
        if (anchoOriginal <= 0 || altoOriginal <= 0) {
            return;
        }
        double factor = obtenerEscalaVisualMaquina(estacion);
        int anchoEscalado = (int) Math.round((anchoOriginal * Math.max(1, getWidth()) * factor) / 1679.0);
        int altoEscalado = (int) Math.round((altoOriginal * Math.max(1, getHeight()) * factor) / 937.0);

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

    // Factor de escala individual para que ninguna máquina supere la altura de la pared del gimnasio
    private double obtenerEscalaVisualMaquina(EstacionGimnasio estacion) {
        if (estacion instanceof MaquinaMuscular) {
            TipoMusculo musculo = ((MaquinaMuscular) estacion).getGrupoQueTrabaja();
            switch (musculo) {
                case PECHO: return 0.76;
                case HOMBROS: return 0.72;
                case ESPALDA: return 0.76;
                case PIERNAS: return 0.76;
                case BRAZOS: return 0.62;   // maquina_brazos.png es más alta (376px), se reduce para igualar al resto
                default: return 0.75;
            }
        } else if (estacion instanceof Cardio) {
            return 0.78;
        } else if (estacion instanceof Heladera) {
            return 0.70;
        } else if (estacion instanceof BancoDescanso) {
            return 0.82;
        }
        return 0.75;
    }

    // Coordenada X visual de cada estación sobre el piso/pared del gimnasio
    private int obtenerXVisualMaquina(EstacionGimnasio estacion) {
        if (estacion instanceof MaquinaMuscular) {
            TipoMusculo musculo = ((MaquinaMuscular) estacion).getGrupoQueTrabaja();
            switch (musculo) {
                case PECHO: return 220;
                case HOMBROS: return 350;
                case ESPALDA: return 500;
                case PIERNAS: return 665;
                case BRAZOS: return 805;
                default: return estacion.getX();
            }
        } else if (estacion instanceof Cardio) {
            return 255;     // pegada a la pared inferior izquierda
        } else if (estacion instanceof Heladera) {
            return 85;      // apoyada en el rincón izquierdo señalado en la captura
        } else if (estacion instanceof BancoDescanso) {
            return 730;     // pegado a la pared inferior derecha
        }
        return estacion.getX();
    }

    // Coordenada Y visual de cada estación sobre el piso/pared del gimnasio
    private int obtenerYVisualMaquina(EstacionGimnasio estacion) {
        if (estacion instanceof MaquinaMuscular) {
            TipoMusculo musculo = ((MaquinaMuscular) estacion).getGrupoQueTrabaja();
            switch (musculo) {
                case PECHO: return 204;
                case HOMBROS: return 140;
                case ESPALDA: return 102;
                case PIERNAS: return 148;
                case BRAZOS: return 212;
                default: return estacion.getY();
            }
        } else if (estacion instanceof Cardio) {
            return 385;     // pegada a la pared inferior izquierda
        } else if (estacion instanceof Heladera) {
            return 275;     // más abajo, donde indica la flecha roja en el rincón izquierdo
        } else if (estacion instanceof BancoDescanso) {
            return 390;     // pegado a la pared inferior derecha
        }
        return estacion.getY();
    }

    private void dibujarEtiquetaEstacion(Graphics2D g2, EstacionGimnasio estacion,
                                         Personaje personaje, int maqX, int maqY) {
        boolean usable = estacion.puedeUsarse(personaje);
        String titulo = estacion.getNombre().toUpperCase();
        String subtitulo = armarSubtituloEstacion(estacion, usable);

        g2.setFont(FUENTE_ESTACION);
        FontMetrics fmTitulo = g2.getFontMetrics();
        g2.setFont(FUENTE_COSTO);
        FontMetrics fmSub = g2.getFontMetrics();

        int anchoCaja = Math.max(fmTitulo.stringWidth(titulo), fmSub.stringWidth(subtitulo)) + 14;
        int altoCaja = 30;
        int cajaX = maqX - anchoCaja / 2;
        // En las estaciones inferiores (cinta y banco) el cartel se ubica sobre el cuerpo de la máquina
        // para dejar libre el pasillo de piso por donde camina el personaje
        int offsetCartelY = (estacion instanceof Cardio || estacion instanceof BancoDescanso) ? 32 : 58;
        int cajaY = Math.max(6, maqY - offsetCartelY);

        // Cartel retro encima de la máquina (sin círculo verde visible)
        g2.setColor(usable ? COLOR_ETIQUETA_FONDO : COLOR_ETIQUETA_BLOQUEADA);
        g2.fillRect(cajaX, cajaY, anchoCaja, altoCaja);
        g2.setColor(usable ? PanelEstadisticas.COLOR_BORDE_CLARO : new Color(255, 120, 120));
        g2.drawRect(cajaX, cajaY, anchoCaja, altoCaja);

        g2.setFont(FUENTE_ESTACION);
        g2.setColor(PanelEstadisticas.COLOR_TEXTO);
        g2.drawString(titulo, maqX - fmTitulo.stringWidth(titulo) / 2, cajaY + 13);

        g2.setFont(FUENTE_COSTO);
        g2.setColor(usable ? new Color(242, 201, 56) : new Color(255, 200, 200));
        g2.drawString(subtitulo, maqX - fmSub.stringWidth(subtitulo) / 2, cajaY + 25);
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

    private Image obtenerSpriteActualPersonaje(int indiceCuadro) {
        if (enMovimiento) {
            if (orientacionActual == ORIENTACION_FRENTE) {
                return cuadrosFrente[indiceCuadro];
            } else if (orientacionActual == ORIENTACION_ESPALDA) {
                return cuadrosEspalda[indiceCuadro];
            }
        }
        return cuadrosPersonaje[indiceCuadro];
    }

    private void dibujarPersonaje(Graphics2D g2, String nombre, int x, int y) {
        // Selección de cuadro del ciclo de 4 tiempos desacoplada de los ticks de posición
        int indiceCuadro = enMovimiento
                ? (pasoAnimacion / TICKS_POR_CUADRO_ANIMACION) % cuadrosPersonaje.length
                : 0;
        boolean enZancada = enMovimiento && (indiceCuadro == 1 || indiceCuadro == 3);
        int reboteY = enZancada ? -2 : 0;

        // Sombra debajo de los pies (se achica levemente al elevar el paso)
        int anchoSombra = enZancada ? 32 : 36;
        g2.setColor(COLOR_PERSONAJE_SOMBRA);
        g2.fillOval(x - anchoSombra / 2, y + 26, anchoSombra, 12);

        Image spritePersonaje = obtenerSpriteActualPersonaje(indiceCuadro);

        if (spritePersonaje != null) {
            int anchoOriginal = spritePersonaje.getWidth(this);
            int altoOriginal = spritePersonaje.getHeight(this);
            if (anchoOriginal > 0 && altoOriginal > 0) {
                // Escalamos a una altura lógica uniforme (ALTO_LOGICO_PERSONAJE) respetando la proporción del PNG
                int altoSprite = escalarY(ALTO_LOGICO_PERSONAJE);
                int anchoSprite = (int) Math.round(altoSprite * (anchoOriginal / (double) altoOriginal));
                int posY = y - altoSprite / 2 + reboteY;

                Object interpolacionPrevia = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                // Espejado horizontal solo cuando camina de costado hacia la izquierda
                boolean espejar = (orientacionActual == ORIENTACION_COSTADO) && mirandoIzquierda;
                if (espejar) {
                    g2.drawImage(spritePersonaje, x + anchoSprite / 2, posY,
                            -anchoSprite, altoSprite, this);
                } else {
                    g2.drawImage(spritePersonaje, x - anchoSprite / 2, posY,
                            anchoSprite, altoSprite, this);
                }

                if (interpolacionPrevia != null) {
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolacionPrevia);
                }
                return;
            }
        }

        // Respaldo si no se encontró el PNG
        g2.setColor(new Color(54, 163, 247));
        g2.fillOval(x - 10, y - 10 + reboteY, 20, 20);
    }

    private void dibujarEtiquetaPersonaje(Graphics2D g2, String nombre, int x, int y) {
        int indiceCuadro = enMovimiento
                ? (pasoAnimacion / TICKS_POR_CUADRO_ANIMACION) % cuadrosPersonaje.length
                : 0;
        int reboteY = (enMovimiento && (indiceCuadro == 1 || indiceCuadro == 3)) ? -2 : 0;

        // Etiqueta con el nombre arriba de la cabeza del personaje
        g2.setFont(FUENTE_ESTACION);
        FontMetrics fm = g2.getFontMetrics();
        String etiqueta = "▼ " + nombre.toUpperCase();
        int ancho = fm.stringWidth(etiqueta) + 10;
        int etiquetaY = y - 54 + reboteY;
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
            // Detecta el click directamente sobre la máquina, banco o heladera (o su cartel)
            int maqX = obtenerXVisualMaquina(estacion);
            int maqY = obtenerYVisualMaquina(estacion);
            double distMaquina = Math.hypot(xLogico - maqX, yLogico - maqY);
            double distCartel = Math.hypot(xLogico - maqX, yLogico - (maqY - 43));
            if (distMaquina <= RADIO_CLICK || distCartel <= RADIO_CLICK) {
                return estacion;
            }
        }
        return null;
    }

    // TEMA: clase interna para escuchar los clicks del mouse sobre las estaciones del mapa
    private class OyenteMouseGimnasio extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (enMovimiento || controlador.hayEventoPendiente()) {
                return;     // ignora clicks mientras el personaje está caminando o hay un evento sin resolver
            }
            int xLogico = desescalarX(e.getX());
            int yLogico = desescalarY(e.getY());
            EstacionGimnasio elegida = buscarEstacionEn(xLogico, yLogico);
            if (elegida != null && oyenteEstacion != null) {
                oyenteEstacion.alSeleccionarEstacion(elegida);
            } else if (personajeX != POS_INICIAL_X || personajeY != POS_INICIAL_Y) {
                // Si hace click en el piso (fuera de las máquinas), camina de vuelta al centro sin gastar tiempo
                moverPersonajeHacia(POS_INICIAL_X, POS_INICIAL_Y, () -> {
                    mirandoIzquierda = false;
                    repaint();
                });
            }
        }
    }

    // TEMA: hilos de ejecución separados: clase interna que implementa Runnable (Unidad 6)
    private class MovimientoPersonaje implements Runnable {

        private final int destinoX;
        private final int destinoY;
        private final int opacidadInicial;
        private final int opacidadFinal;
        private final Runnable accionAlLlegar;

        private MovimientoPersonaje(int destinoX, int destinoY,
                                    int opacidadInicial, int opacidadFinal,
                                    Runnable accionAlLlegar) {
            this.destinoX = destinoX;
            this.destinoY = destinoY;
            this.opacidadInicial = opacidadInicial;
            this.opacidadFinal = opacidadFinal;
            this.accionAlLlegar = accionAlLlegar;
        }

        @Override
        public void run() {
            int origenX = personajeX;
            int origenY = personajeY;

            // Calculamos los pasos según la distancia real para mantener una velocidad constante
            double distancia = Math.hypot(destinoX - origenX, destinoY - origenY);
            int totalPasos = Math.max(PASOS_MINIMOS_MOVIMIENTO,
                    (int) Math.round(distancia / VELOCIDAD_PX_POR_TICK));

            // TEMA: try / catch / finally: garantiza que al terminar (o si se interrumpe) se destrabe el estado
            try {
                for (int i = 1; i <= totalPasos; i++) {
                    final int nuevoX = origenX + ((destinoX - origenX) * i) / totalPasos;
                    final int nuevoY = origenY + ((destinoY - origenY) * i) / totalPasos;
                    final int nuevaOpacidad = opacidadInicial
                            + ((opacidadFinal - opacidadInicial) * i) / totalPasos;
                    final int pasoActual = i;

                    // Los cambios visuales de Swing se encolan en el Event Dispatch Thread
                    SwingUtilities.invokeLater(() -> {
                        personajeX = nuevoX;
                        personajeY = nuevoY;
                        opacidadOscuridad = nuevaOpacidad;
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
                    opacidadOscuridad = opacidadFinal;
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
