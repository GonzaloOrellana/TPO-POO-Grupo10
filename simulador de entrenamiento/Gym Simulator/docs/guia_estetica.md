# Guía estética — Simulador de Entrenamiento (propuesta)

Estado: **propuesta para aprobar con el grupo**. Si se aprueba, copiarla a `docs/ESTETICA.md`.

![Boceto de referencia (generado con IA, solo orientativo)](C:/Users/Gonzalo/.gemini/antigravity-acp/brain/a5b0e24f-f38f-4a1e-9894-d6f280fca52e/mockup_gimnasio_pixel_1792431815509.jpg)

> [!NOTE]
> El boceto sirve para la **composición y el clima**, no como arte final: las imágenes de IA no respetan una grilla de píxeles real. Los textos del juego van en español.

## 1. Decisiones tomadas
| Tema | Decisión |
|---|---|
| Estilo | Pixel art retro tipo 16 bits, paleta limitada, sin antialiasing |
| Perspectiva | Cenital 3/4 (estilo Pokémon o Stardew Valley) |
| Resolución lógica | **480×270**, escalada **×2** → área de juego de 960×540 |
| Tamaño de tile | **16×16** px lógicos (el gimnasio mide 30×16 tiles y sobran 14 px para el borde) |
| Sprites | Packs gratuitos con licencia libre (CC0 o CC-BY) más retoques propios |
| Personaje | Animación de caminar con 2 a 4 cuadros en un `Image[]` |
| Fuente | Pixel `.ttf` con licencia OFL (propuesta: *Press Start 2P*, de Google Fonts) |

## 2. Paleta propuesta (sacada del boceto)
| Uso | Nombre de la constante | Hex |
|---|---|---|
| Fondo exterior y bordes | `VERDE_OSCURO` | `#1B4D45` |
| Paneles del HUD | `VERDE_HUD` | `#2F7A6B` |
| Brillos y bordes claros | `VERDE_CLARO` | `#8FD3B6` |
| Piso | `MADERA` / `MADERA_OSCURA` | `#C8774A` / `#8E4E2E` |
| Paredes | `CREMA` | `#EFE3C8` |
| Texto | `TEXTO` | `#F4F1E8` |
| Sombras y contornos | `SOMBRA` | `#12202A` |
| Barra de fuerza | `COLOR_FUERZA` | `#E0533D` |
| Barra de resistencia | `COLOR_RESISTENCIA` | `#4AA3DF` |
| Barra de energía | `COLOR_ENERGIA` | `#F2C53D` |
| Barra de motivación | `COLOR_MOTIVACION` | `#5BD15B` |

Estados de fatiga (para el panel de músculos, si se aprueba): OPTIMO `#5BD15B` · FATIGADO `#F2C53D` · SOBRECARGADO `#F08A3C` · LESIONADO `#E0533D`.

## 3. Distribución de la ventana
```
+------------------------------------------------+
| PanelEstadisticas  960x80  (HUD: nombre, nivel,|
| día, tiempo, 4 barras segmentadas)             |
+------------------------------------------------+
|                                                |
| PanelGimnasio  960x540  (480x270 lógico x2)    |
| 5 máquinas arriba · cinta, heladera y banco    |
| abajo · botón "IRSE A CASA" abajo a la derecha |
|                                                |
+------------------------------------------------+
```
La ventana usa `pack()` en lugar de `setSize(...)`, así mide exactamente lo que piden los paneles (960×620 de contenido).

## 4. Cómo se logra el pixel art en Swing (solo JDK)
- **Coordenadas lógicas:** las `x, y` de las estaciones del modelo pasan a estar en píxeles lógicos (0–480 × 0–270). `PanelGimnasio` dibuja con `g2.scale(2, 2)` y divide por 2 las coordenadas del mouse.
- **Píxeles nítidos:** `RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR` y antialiasing apagado, así el escalado no difumina.
- **Barras segmentadas** dibujadas a mano con `fillRect`, en lugar del `JProgressBar` estándar, que rompe la estética.
- **Botones y diálogos:** colores de la paleta, borde grueso (`LineBorder`) y sin el recuadro de foco. Los `JOptionPane` se tiñen con `UIManager`; los diálogos propios (evento, duración) se dibujan con la misma estética.

## 5. Recursos (assets)
- Carpeta: `src/recursos/sprites/*.png` y `src/recursos/fuentes/*.ttf`. IntelliJ copia los archivos que no son `.java` a la salida, y se cargan con `getClass().getResource("/recursos/...")`.
- **Carga segura:** `ImageIO.read(...)` puede lanzar `IOException`. Si falla, se dibuja un rectángulo de color en su lugar y el juego sigue andando. Suma un `try/catch` real (Unidad 5).
- **Fuente:** `Font.createFont(...)` lanza `FontFormatException` e `IOException`. Se resuelve con un **multi-catch** y, si falla, se usa `Monospaced` (Unidad 5).
- **Licencias:** usar solo CC0, CC-BY u OFL. Guardar la licencia de cada pack y armar un `CREDITOS.md` con autor y enlace. Evitar packs que prohíban modificar el arte.
- **Dónde buscar:** itch.io (etiquetas *top-down*, *16x16*, *interior*, *gym*) y OpenGameArt. Un candidato para verificar es *Modern Interiors* de LimeZu (16×16, cenital 3/4); revisar qué permite la versión gratuita antes de usarlo.

## 6. Temas de la guía que aporta
| Requisito | Dónde |
|---|---|
| Arrays | `Image[]` con los cuadros de la animación |
| `try/catch` y multi-catch | Carga de imágenes y de la fuente, con valor por defecto si fallan |
| Hilos | El hilo de movimiento cambia de cuadro de animación mientras camina |
| Constantes `static final` | Paleta, `ESCALA`, `TAMANO_TILE` |

## 7. Decisiones abiertas (requieren aprobación)
1. **Clases nuevas en `vista`** (no están en `DISENO_CLASES.md`): `Estilo`, con la paleta, la fuente y helpers de botones y bordes, y `Recursos`, que carga los sprites con su valor por defecto. Ambas serían clases utilitarias como `CatalogoEventos`, otra vez Pure Fabrication.
2. **Coordenadas del modelo en píxeles lógicos (480×270):** cambia los valores de `crearEstaciones()` del paso 6.1.
3. **Dónde va el panel de músculos** (PENDIENTES 1.5): una franja en el HUD, una barra lateral (agranda la ventana) o un cartel al pasar el mouse sobre cada máquina.
4. **Qué pack de sprites se elige**, después de revisar su licencia.