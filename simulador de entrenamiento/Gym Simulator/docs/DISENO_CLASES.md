# DISENO_CLASES.md — Diccionario de clases y reglas del juego

Fuente de verdad del diseño. Estado: 24/10/2026. Lo marcado **[HECHO]** ya está implementado.
La narrativa que une todas las reglas ("La Copa del Barrio") está en `docs/HISTORIA.md`.
Si algo de acá choca con lo que te piden, avisá antes de cambiarlo.

> **Sobre el diagrama UML aprobado por el docente:** los enums aparecen como *notas amarillas* (el docente objetó clases sin atributos ni métodos) y sus atributos figuran como `String`. **En el código se usan enums reales.** Hay cambios pendientes del diagrama en `PENDIENTES.md`.

---

## 1. Reglas del juego

### Tiempo por día
Presupuesto fijo: **90 minutos** (`TIEMPO_POR_DIA = 90`, `static final` en `GestorJuego`).

| Estación | Minutos | Observación |
|---|---|---|
| Máquina muscular (cualquiera de las 5) | 15 fijo | Mismo costo para las 5; cambia el `TipoMusculo` que impacta |
| Cinta (Cardio) | 5 / 10 / 15 | El jugador elige la duración |
| Heladera | 1 fijo | No seleccionable |
| Banco de Descanso | 1 / 2 / 3 | El jugador elige la duración |

Si el costo supera `tiempoRestante`, el controlador lanza `TiempoInsuficienteException`.
El botón **"Irse a casa"** está siempre visible y habilitado; termina el día en cualquier momento (sin lógica de habilitación condicional).

### Fatiga por grupo muscular
| nivelFatiga | Estado | Efectividad |
|---|---|---|
| 0-39 | OPTIMO | 100% |
| 40-69 | FATIGADO | 60% |
| 70-89 | SOBRECARGADO | 25% (y resta **5 de motivación** al entrenar ese músculo: `PENALIZACION_SOBRECARGA = -5` en `MaquinaMuscular`) |
| 90-100 | LESIONADO | 0% (no se puede entrenar) |

Cada sesión de entrenamiento sube la fatiga **+25**. **La fatiga solo baja con el Banco de Descanso** (no hay recuperación automática por día).

### Lesión por días (APROBADO)
- Al llegar a fatiga 100 el músculo pasa a `LESIONADO` y arranca un contador de **2 días** (`DIAS_LESION`, ajustable a 3 si jugando se siente flojo).
- Mientras está lesionado, **el Banco de Descanso no lo cura**: la lesión tiene su propio plazo (`recuperar()` no hace nada en ese estado).
- Al cerrar cada día el contador baja (`pasarDia()`). Al llegar a 0 el músculo vuelve con fatiga **50** (`FATIGADO`).
- Ejemplo: lesión el día 3 → no se puede entrenar el día 3 ni el 4 → vuelve el día 5.
- Un músculo lesionado **no se puede entrenar**: `puedeUsarse(Personaje)` devuelve `false`, así no se cobra tiempo por una acción imposible.
- Al subir de nivel, `reiniciar()` deja fatiga 0 y cura la lesión.

### Niveles y progresión
- `Nivel`: `numero`, `nombre`, `fuerzaObjetivo`, `resistenciaObjetivo`, `diasDisponibles`, `boolean objetivoCumplido(Personaje p)`. Dos constructores sobrecargados (si no se indica `nombre`, usa `"Nivel " + numero`).
- Etapas de la Copa del Barrio en `VentanaPrincipal`: Nivel 1 `"Clasificatoria"` (100 fuerza, 60 resistencia, 5 días), Nivel 2 `"Semifinal"` (250, 150, 4 días), Nivel 3 `"Final"` (450, 270, 3 días).
- A mayor nivel: objetivo más exigente y menos días. Los niveles piden **solo fuerza y resistencia**; la energía es un recurso diario.
- Los días se cuentan desde 1. Al avanzar de nivel: `diaActual` vuelve a 1 y la fatiga de los 5 grupos vuelve a `OPTIMO` y se curan las lesiones (`Personaje.reiniciarFatigaGrupos()`). La fuerza y la resistencia **no** se reinician (los objetivos de nivel son acumulativos).
- **Cuándo cambia el nivel (APROBADO):** al **cerrar el día**, si se cumplió el objetivo del nivel y hay un nivel siguiente. La vista muestra un cartel **"Nivel completado"**.
- Las barras de fuerza/resistencia de la UI muestran el progreso hacia la meta: `(fuerza * 100) / fuerzaObjetivo`.

### Día nuevo
1. `recuperarEnergiaDiaria()`: energía **+40**, tope 100.
2. **Recién después**, desde el **día 2** (el día 1 nunca tiene evento), con probabilidad del **30%** (`PROBABILIDAD_EVENTO`) se sortea un `EventoAleatorio`. El orden importa: si el evento fuera primero, se comería la recuperación.
3. La motivación **no** se resetea por día.
4. Mientras haya un evento sin resolver, el controlador **bloquea** las acciones y el cierre del día.

### Cierre de partida (APROBADO)
- **Victoria:** estar en el último nivel con su objetivo cumplido (`verificarVictoria()`). Se puede consultar apenas se cumple, sin esperar a cerrar el día.
- **Derrota** (`verificarDerrota()`): se divide en dos consultas para mostrar finales distintos en la vista: `perdioPorBurnout()` (motivación en 0) y `perdioPorTiempo()` (`diaActual > diasDisponibles` sin cumplir el objetivo). Al terminar la partida, la vista reemplaza el gimnasio por la pantalla final con el historial ordenado y el botón "Jugar de nuevo".

---

## 2. Enums (paquete `modelo`) **[HECHO]**

| Enum | Valores / contenido |
|---|---|
| `TipoMusculo` | PECHO, ESPALDA, PIERNAS, HOMBROS, BRAZOS |
| `Objetivo` | Categoría en la Copa del Barrio: `HIPERTROFIA(1.10, 1.10)`, `FUERZA(1.25, 1.0)`, `RESISTENCIA(1.0, 1.25)`. Atributos `multiplicadorFuerza` y `multiplicadorResistencia` + getters; `Personaje.sumarFuerza` y `sumarResistencia` aplican el multiplicador |
| `EstadoFatiga` | OPTIMO(1.0), FATIGADO(0.6), SOBRECARGADO(0.25), LESIONADO(0.0). Atributo `double efectividad` + `getEfectividad()` |
| `DuracionDescanso` | Atributos: minutos, energiaRecuperada, reduccionFatiga, recuperacionMotivacion. Getters: `getMinutos()`, `getEnergiaRecuperada()`, `getReduccionFatiga()`, `getRecuperacionMotivacion()` |
| `DuracionCardio` | Atributos: minutos, resistenciaGanada, energiaGastada. Getters: `getMinutos()`, `getResistenciaGanada()`, `getEnergiaGastada()` |

`DuracionDescanso` (afecta a **los 5 grupos a la vez**):

| Opción | Min | Energía | Fatiga (c/u de los 5) | Motivación |
|---|---|---|---|---|
| CORTO | 1 | +10 | -5 | +5 |
| MEDIO | 2 | +20 | -10 | +10 |
| LARGO | 3 | +30 | -15 | +15 |

`DuracionCardio` (valores sugeridos, ajustables):

| Opción | Min | Resistencia | Energía gastada |
|---|---|---|---|
| CORTO | 5 | +10 | -10 |
| MEDIO | 10 | +20 | -20 |
| LARGO | 15 | +30 | -30 |

---

## 3. Excepciones (paquete `excepciones`) **[HECHO]**

| Clase | Extiende | Categoría | Se lanza cuando |
|---|---|---|---|
| `DatosInvalidosException` | `Exception` | Checked | Se crea el `Personaje` con nombre vacío o edad <= 0 |
| `TiempoInsuficienteException` | `RuntimeException` | Unchecked | El controlador intenta ejecutar una estación que cuesta más que `tiempoRestante` |

No existe `EnergiaInsuficienteException`: el desmayo es comportamiento normal, no excepcional.

---

## 4. Modelo (paquete `modelo`)

### 4.1 `GrupoMuscular` **[HECHO]**
- Atributos: `final TipoMusculo tipo`, `int nivelFatiga` (0-100), `EstadoFatiga estado`, `int diasLesionRestantes`, `final List<Integer> historialEstimulo`.
- Constantes `static final`: `INCREMENTO_FATIGA = 25`, `FATIGA_MAXIMA = 100`, `DIAS_LESION = 2`, `FATIGA_TRAS_LESION = 50`.
- Métodos públicos: `obtenerEfectividad()`, `registrarEntrenamiento()` (suma 25, tope 100, agrega el nivel al historial y, si recién se lesionó, arranca el contador de días), `recuperar(int)` (piso 0; **no hace nada si está lesionado**), getters (incluye `getDiasLesionRestantes()`), `toString()`.
- Métodos **package-private (sin modificador, a propósito)**: `actualizarEstado()` (si hay días de lesión pendientes fuerza `LESIONADO`; si no, calcula el estado por umbrales), `pasarDia()` (baja el contador; al llegar a 0 la fatiga pasa a 50), `reiniciar()` (fatiga 0 y lesión curada). Solo los usa `Personaje`.
- `historialEstimulo` guarda el nivel de fatiga alcanzado en cada sesión. `getHistorialEstimulo()` devuelve `Collections.unmodifiableList(...)`.
- **No implementa `Comparable`** (el docente lo corrigió: la fatiga es estado mutable y rompería la consistencia con `equals()`). Para ordenar por fatiga en la UI se usa `ComparadorPorFatiga implements Comparator<GrupoMuscular>` en `PanelMusculos`.

### 4.2 `Personaje` **[HECHO]**
- Atributos: `final String nombre`, `final int edad`, `final Objetivo objetivo`, `int fuerza`, `int resistencia`, `int energia`, `int motivacion`, `final Map<TipoMusculo, GrupoMuscular> gruposMusculares` (un `LinkedHashMap` para mantener el orden).
- **No tiene atributo `nivel`** (decisión del equipo): el nivel actual vive solo en `GestorJuego`.
- Constantes: `ENERGIA_MAXIMA = 100`, `MOTIVACION_MAXIMA = 100`, `ENERGIA_DIARIA = 40`, `PORCENTAJE_PERDIDA_DESMAYO = 10`, `ENERGIA_AL_DESPERTAR = 10`.
- **Constructores sobrecargados**: `Personaje(String nombre, int edad, Objetivo objetivo)` delega con `this(...)` al completo `Personaje(nombre, edad, objetivo, fuerza, resistencia, energia, motivacion)`. Ambos declaran `throws DatosInvalidosException` (nombre vacío o edad <= 0). La validación vive solo en el completo.
- **Valores iniciales (aprobados):** fuerza 0, resistencia 0, energía 100, motivación 100.
- **Desmayo (aprobado):** `desmayarse()` hace perder el **10%** de fuerza y de resistencia y deja la energía en **10** (`ENERGIA_AL_DESPERTAR`), para que el personaje se despierte débil y no vuelva a desmayarse en bucle.
- El mapa se arma recorriendo `TipoMusculo.values()` (un array).
- Métodos: `consumirEnergia(int)` (piso 0; si llega a 0 llama a `desmayarse()`), `desmayarse()`, `sumarFuerza(int)` y `sumarResistencia(int)` (aplican el multiplicador del `Objetivo`), `sumarEnergia(int)` (tope 100), `sumarMotivacion(int)` (acepta negativos, acota entre 0 y 100), `recuperarEnergiaDiaria()`, `reiniciarFatigaGrupos()` (llama `reiniciar()` a cada grupo), `pasarDia()` (llama `pasarDia()` a cada grupo; lo invoca `GestorJuego` al cerrar el día), `getGrupoMuscular(TipoMusculo)`, `getGruposMusculares()` (`Collections.unmodifiableMap`), getters, `toString()` (sin nivel).
- Relación: **composición** `Personaje 1 — 5 GrupoMuscular`.

### 4.3 Jerarquía `EstacionGimnasio` **[HECHO]**
```java
public abstract class EstacionGimnasio {
    protected String nombre;
    protected int x;
    protected int y;
    protected EstacionGimnasio(String nombre, int x, int y) { ... }
    public abstract void interactuar(Personaje p);
    public abstract int getCostoTiempo();
    public String getMotivoNoUsable(Personaje p) { return ""; }   // "" = se puede usar; si no, el motivo
    public boolean puedeUsarse(Personaje p) { return getMotivoNoUsable(p).isEmpty(); }
    // getNombre(), getX(), getY()
}
```
Cada subclase llama a `super(nombre, x, y)` y sobreescribe `interactuar` y `getCostoTiempo` con `@Override`. `getMotivoNoUsable(Personaje)` devuelve `""` por defecto (la estación se puede usar); `puedeUsarse` se apoya en él. Solo `MaquinaMuscular` sobreescribe `getMotivoNoUsable`.

| Subclase | Atributos propios | Comportamiento |
|---|---|---|
| `MaquinaMuscular` | `final TipoMusculo grupoQueTrabaja`, `impactoFuerza` (20), `costoTiempo` (15), `costoEnergia` (15), `PENALIZACION_SOBRECARGA = -5` | Dos constructores: el corto usa los valores por defecto (`this(...)`). `getMotivoNoUsable`: si su grupo está `LESIONADO` devuelve "Tu pecho está lesionado: vuelve a estar disponible en N día(s)" (con el nombre del músculo en minúscula); si no, `""`. `interactuar`: si la efectividad del grupo es 0, no hace nada; si el grupo estaba `SOBRECARGADO` resta 5 de motivación; luego suma `(int)(impactoFuerza * efectividad)` de fuerza, registra el entrenamiento y consume energía |
| `Cardio` | `DuracionCardio duracionSeleccionada` (arranca en CORTO) | `seleccionarDuracion(DuracionCardio)`. `interactuar`: suma resistencia y consume energía según la duración. El costo de tiempo es el de la duración |
| `Heladera` | `recuperacionEnergia` (15), `costoTiempo` (1) | `interactuar`: suma energía. Sin selección |
| `BancoDescanso` | `DuracionDescanso duracionSeleccionada` (arranca en CORTO) | `seleccionarDuracion(DuracionDescanso)`. `interactuar`: recupera fatiga de **los 5 grupos**, suma energía y motivación |

`Cardio` y `BancoDescanso` arrancan en `CORTO` para evitar un `NullPointerException` si el jugador no elige duración.
Las 5 máquinas del mapa son 5 **instancias** de `MaquinaMuscular` con distinto `TipoMusculo`.
**Cómo se evita cobrar tiempo por un músculo lesionado:** la vista llama a `GestorJuego.validarAccion(estacion)` **antes** de mover al personaje; si falla, muestra un cartel con el motivo y no pasa nada más.

### 4.4 `Nivel` **[HECHO]**
Clase **inmutable** (todos los atributos `final`, sin setters) en `modelo`. Atributos: `int numero`, `String nombre`, `int fuerzaObjetivo`, `int resistenciaObjetivo`, `int diasDisponibles`. Dos constructores sobrecargados con `this(...)`. Método: `boolean objetivoCumplido(Personaje p)` (fuerza **y** resistencia alcanzan los objetivos). Getters y `toString()`.

### 4.5 `EventoAleatorio` y `OpcionEvento` **[HECHO]**
- `EventoAleatorio`: `final String descripcion`, `final List<OpcionEvento> opciones` (**mínimo 2**, copia defensiva; si no, `IllegalArgumentException("Un evento necesita al menos 2 opciones")`).
- **Sobrecarga de métodos:** `aplicar(Personaje p, OpcionEvento elegida)` y `aplicar(Personaje p, int indiceOpcion)` (esta delega en la primera). Si la opción no pertenece al evento lanza `IllegalArgumentException("La opción no pertenece a este evento")`; un índice inválido lanza `IndexOutOfBoundsException`. Un delta de energía negativo se aplica con `consumirEnergia` (así se dispara el desmayo si llega a 0); uno positivo con `sumarEnergia`; la motivación con `sumarMotivacion`.
- `getDescripcion()` y `getOpciones()` (`Collections.unmodifiableList`).
- `OpcionEvento`: **clase estática anidada, `final` e inmutable**. Atributos: `texto`, `deltaEnergia`, `deltaMotivacion`, con getters.
- **Decisión: los eventos son con elección** (el jugador elige entre opciones).
- Relación: composición `EventoAleatorio 1 — 2..* OpcionEvento`.

### 4.6 `CatalogoEventos` **[HECHO]**
Clase utilitaria `final` en `modelo`, con constructor **privado** y un método estático `crearEventos()` que devuelve la lista de eventos del juego (hoy 4: mala noche, batido, gimnasio concurrido, canción favorita). Patrón GRASP **Pure Fabrication**: existe para que `GestorJuego` no cargue con el contenido del juego.
**Regla de diseño:** ninguna opción de evento resta **40 o más** de energía (así un evento nunca provoca un desmayo apenas arranca el día).

---

## 5. Controlador (paquete `controlador`) **[HECHO]**

### `GestorJuego` (GRASP Controller)
Es el **único dueño del nivel actual** (`Personaje` no tiene atributo `nivel`).
- Atributos: `final Personaje personaje`, `final List<EstacionGimnasio> estaciones`, `final List<Nivel> niveles`, `final List<EventoAleatorio> eventos`, `final Random azar`, `int indiceNivelActual`, `int diaActual` (arranca en 1), `int tiempoRestante`, `EventoAleatorio eventoPendiente` (`null` si no hay), `private static final int TIEMPO_POR_DIA = 90`, `private static final double PROBABILIDAD_EVENTO = 0.30`, `final List<RegistroDia> historial`.
- **Constructores sobrecargados:** `GestorJuego(Personaje, List<EstacionGimnasio>, List<Nivel>)` delega con `this(...)` al completo `GestorJuego(Personaje, List<EstacionGimnasio>, List<Nivel>, List<EventoAleatorio>, Random)` (sin catálogo de eventos y con un `Random` nuevo). El completo hace **copias defensivas** de las listas, lanza `IllegalArgumentException` si no hay niveles y deja `eventoPendiente` en `null` (el día 1 no tiene evento). Recibir los eventos y el `Random` de afuera permite probar con azar fijo (inyección de dependencias).
- `void validarAccion(EstacionGimnasio)`: primero verifica que no haya un evento pendiente (si lo hay, `IllegalStateException("Resolvé el evento antes de continuar")`); después pregunta `getMotivoNoUsable(personaje)`; si hay motivo lanza `IllegalStateException(motivo)`; si el costo supera `tiempoRestante` lanza `TiempoInsuficienteException`. **La vista lo llama antes de mover al personaje** y muestra el cartel con el mensaje.
- `void procesarAccion(EstacionGimnasio)`: `validarAccion`, luego `interactuar` y descuenta el tiempo.
- `boolean verificarVictoria()`, `boolean verificarDerrota()`, `boolean perdioPorBurnout()` y `boolean perdioPorTiempo()` (reglas en la sección 1).
- `boolean cerrarDia()` (también es "Irse a casa"): guarda un `RegistroDia`, llama `personaje.pasarDia()`, si cumplió el objetivo y hay nivel siguiente avanza de nivel (`avanzarNivel()`: índice +1, día en 1, `reiniciarFatigaGrupos()`) y si no, `diaActual++`. Antes de todo verifica que no haya un evento pendiente. Después restablece el tiempo (90), suma la energía diaria (+40) y **recién entonces** sortea el evento del día. **Devuelve `true` si subió de nivel** (la vista muestra "Nivel completado").
- Eventos: `boolean hayEventoPendiente()`; `EventoAleatorio getEventoPendiente()` (lanza `IllegalStateException("No hay un evento pendiente")` si no hay, en vez de devolver `null`); `void resolverEvento(int indiceOpcion)` (aplica la opción y **después** limpia el evento: si el índice es inválido el evento sigue pendiente). Privados: `sortearEventoDelDia()` y `verificarSinEventoPendiente()`.
- Getters: `getPersonaje()`, `getNivelActual()`, `getDiaActual()`, `getTiempoRestante()`, `getEstaciones()` y `getHistorial()` (ambos `Collections.unmodifiableList`).

### `RegistroDia`
**Clase estática anidada, `final` e inmutable** dentro de `GestorJuego`, con constructor `private` (solo el controlador crea registros). Campos: `numeroNivel`, `numeroDia`, `energiaFinal`, `motivacionFinal`, `fuerzaFinal`, `resistenciaFinal`. Implementa `Comparable<RegistroDia>`: orden cronológico, **primero por nivel y luego por día** (se agregó `numeroNivel` porque el día se reinicia en cada nivel). En el diagrama figura como interfaz `Comparable_RegistroDia`; en el código es `java.lang.Comparable<RegistroDia>`.

---

## 6. Vista (paquete `vista`) **[HECHO]**

Estética retro pixel art sobre fondo pizarra (`#495466`) y paneles HUD (`#2C3545` con bordes `#8CA0BA`), usando el mapa isométrico `src/recursos/referencia_gimnasio.png`.

| Clase | Estado | Rol |
|---|---|---|
| `VentanaPrincipal extends JFrame` | **[HECHO]** | Ventana raíz (`960x740`). Pantalla de inicio con la premisa de la Copa del Barrio y formulario (`txtNombre`, `txtEdad`, `cbObjetivo`, `btnIniciarPartida`); pantalla de juego (`PanelEstadisticas`, `PanelGimnasio`, `PanelMusculos`, `DialogoEvento`); pantalla final (`mostrarPantallaFin`) con los 3 finales, el historial ordenado con `Collections.sort` y el botón "JUGAR DE NUEVO". Clases internas: `OyenteIniciarPartida`, `OyenteCerrarDia`, `OyenteClickEstacion`, `OyenteJugarDeNuevo` |
| `PanelEstadisticas extends JPanel` | **[HECHO]** | Header en `NORTH`: nombre, objetivo y nivel actual, día, tiempo restante, botón "IRSE A CASA" y las 4 barras (`JProgressBar`) de fuerza, resistencia, energía y motivación |
| `PanelMusculos extends JPanel` | **[HECHO]** | Barra en `SOUTH`: muestra los 5 `GrupoMuscular` con su barra de fatiga, estado y días de lesión, ordenados de mayor a menor fatiga mediante la clase interna estática `ComparadorPorFatiga implements Comparator<GrupoMuscular>` |
| `PanelGimnasio extends JPanel` | **[HECHO]** | Mapa central en `CENTER`: carga `referencia_gimnasio.png`, dibuja los carteles de cada estación, el marcador del personaje y detecta clicks con `OyenteMouseGimnasio extends MouseAdapter`. Define la interfaz `OyenteEstacion` y el hilo de movimiento `MovimientoPersonaje implements Runnable` |
| `DialogoEvento extends JDialog` | **[HECHO]** | Diálogo modal que muestra el `EventoAleatorio` pendiente y un botón por cada `OpcionEvento` indicando su impacto en energía y motivación. Clase interna: `OyenteElegirOpcion` |

Reglas Swing: construir y modificar la UI **en el Event Dispatch Thread** (`SwingUtilities.invokeLater`). Listeners como clases internas.

---

## 7. Relaciones para el UML
- `Personaje 1 ◆— 5 GrupoMuscular` (composición)
- `GestorJuego 1 ◆— 1 Personaje` (composición)
- `GestorJuego 1 ◆— 1..* Nivel` (composición)
- `GestorJuego 1 ◇— 1..* EstacionGimnasio` (agregación)
- `GestorJuego 1 ◆— 0..* RegistroDia` (composición, clase anidada)
- `EventoAleatorio 1 ◆— 2..* OpcionEvento` (composición)
- `EstacionGimnasio ◁— MaquinaMuscular / Cardio / Heladera / BancoDescanso` (herencia)
- `RegistroDia ┄▷ Comparable<RegistroDia>` (realización)
- `ComparadorPorFatiga ┄▷ Comparator<GrupoMuscular>` (realización, clase interna en `PanelMusculos`)
- `MovimientoPersonaje ┄▷ Runnable` (realización, clase interna en `PanelGimnasio`)
- `GestorJuego ┄> EventoAleatorio` (dependencia, "dispara")
- `GestorJuego ┄> TiempoInsuficienteException` y `Personaje ┄> DatosInvalidosException` («throws»)
- `VentanaPrincipal → GestorJuego` (asociación unidireccional)

## 8. GRASP y SOLID aplicados (identificarlos en la memoria técnica)
- **Controller (GRASP):** `GestorJuego` recibe los eventos de la vista y coordina el modelo.
- **Information Expert (GRASP):** `GrupoMuscular` calcula su propia efectividad y fatiga porque tiene el dato.
- **Pure Fabrication (GRASP):** `CatalogoEventos` existe solo para sacar el contenido de los eventos del controlador.
- **Open/Closed (SOLID):** una estación nueva (ej. "Sauna") solo requiere una subclase de `EstacionGimnasio`.
- **Single Responsibility (SOLID):** la vista dibuja, el controlador coordina, el modelo guarda estado y reglas.
- **Liskov (SOLID):** cualquier subtipo de `EstacionGimnasio` se usa polimórficamente vía `interactuar()` sin romper nada.

## 9. Valores numéricos
**Confirmados:** 90 min/día · máquina 15 min · cinta 5/10/15 · descanso 1/2/3 · agua 1 min · efectividad 100/60/25/0 % · umbrales de fatiga 0-39/40-69/70-89/90-100 · el Banco recupera los 5 grupos a la vez · energía diaria +40 (tope 100) · valores iniciales del personaje (0/0/100/100) · pérdida por desmayo 10% · lesión de 2 días (el músculo vuelve con fatiga 50).

**Sugeridos (ajustables jugando):** incremento de fatiga por sesión (+25) · `impactoFuerza` (20) · `costoEnergia` de máquinas (15) · `recuperacionEnergia` de la heladera (15) · valores de `DuracionCardio` · probabilidad del evento diario (~30%) · niveles (100/60 en 5d, 250/150 en 4d, 450/270 en 3d).
