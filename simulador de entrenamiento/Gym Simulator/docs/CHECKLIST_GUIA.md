# CHECKLIST_GUIA.md — Requisitos obligatorios de la guía del docente

Fuente: "Guía del Trabajo Práctico Final" (Pablo Hernández, 2° cuatrimestre 2026). Cada ítem tiene que poder señalarse en el código entregado y mencionarse en la memoria técnica.

Estado al 19/10/2026:
- ✅ implementado
- 🟡 cubierto en el diseño, falta implementarlo
- ⚠️ requiere una decisión del grupo (ver `PENDIENTES.md`)
- ❌ todavía sin cubrir

## Unidad 1 — Introducción a la POO y UML
| Requisito | Estado | Dónde / qué hacer |
|---|---|---|
| Diagrama de clases del sistema completo | ✅ | Aprobado por el docente (puede cambiar al codear; ver `PENDIENTES.md`) |
| **Diagrama de secuencia** de un caso de uso relevante | ❌ | Entregable obligatorio, se hace en Fase 7. Propuesta: "entrenar en una máquina", con un marco `alt` para `TiempoInsuficienteException` |
| Clases, objetos, atributos, métodos, relaciones (cardinalidad, direccionalidad, composición) | ✅ | Todo el modelo |
| Constructores y destructores | ✅ | Constructores en `Personaje`, estaciones, `GrupoMuscular`. Java no tiene destructores: usa Garbage Collector (aclararlo en la memoria) |
| Tipos primitivos y wrapper | ✅ | `int`/`double` en stats; `Integer` en `List<Integer> historialEstimulo` |

## Unidad 2 — Funcionamiento del lenguaje
| Requisito | Estado | Dónde / qué hacer |
|---|---|---|
| Estructura básica, sintaxis, buenas prácticas | ✅ | Convenciones en `AGENTS.md` |
| Casteo | ✅ | `(int) (impactoFuerza * efectividad)` en `MaquinaMuscular`; `(Objetivo) cbObjetivo.getSelectedItem()` en `VentanaPrincipal`; `(Graphics2D) g` en `PanelGimnasio` |
| **Arreglos (arrays)** | ✅ | `TipoMusculo.values()` en `Personaje`; `Objetivo.values()` y `String[]` en `VentanaPrincipal`; `JLabel[]` y `JProgressBar[]` en `PanelMusculos` |

## Unidad 3 — Clases y objetos
| Requisito | Estado | Dónde / qué hacer |
|---|---|---|
| Herencia (subclase/superclase) | ✅ | `EstacionGimnasio` → 4 subclases; `VentanaPrincipal extends JFrame`; `DialogoEvento extends JDialog`; `PanelEstadisticas`, `PanelMusculos` y `PanelGimnasio extends JPanel` |
| Sobrecarga de **constructores** | ✅ | `Personaje`, `MaquinaMuscular` y `GestorJuego` (2 constructores cada uno, con `this(...)`) |
| | | (también `Nivel`, con y sin `nombre`) |
| Sobrecarga de **métodos** | ✅ | `EventoAleatorio.aplicar(Personaje, OpcionEvento)` y `aplicar(Personaje, int)` |
| Sobreescritura | ✅ | `interactuar()` y `getCostoTiempo()` en cada subclase; `getMotivoNoUsable()` en `MaquinaMuscular`; `paintComponent(Graphics)` en `PanelGimnasio` |
| Modificadores `public`, `private`, `protected` | ✅ | `protected`: atributos y constructor de `EstacionGimnasio`; `paintComponent` en `PanelGimnasio` |
| Modificador **default** (package-private) | ✅ | `GrupoMuscular.actualizarEstado()`, `pasarDia()` y `reiniciar()` |
| Clase abstracta | ✅ | `EstacionGimnasio` |
| **Interfaz con su contrato** | ✅ | `Comparable`, `Comparator` y `Runnable` (de Java) + `PanelGimnasio.OyenteEstacion` (interfaz propia con su contrato) |
| Polimorfismo | ✅ | Lista de `EstacionGimnasio` con `interactuar()`, `getCostoTiempo()` y `puedeUsarse()` en controlador y vista |
| `super()` y `this()` | ✅ | `super(nombre, x, y)` en estaciones; `super(...)` en `VentanaPrincipal` y `DialogoEvento`; `this(...)` en `Personaje`, `MaquinaMuscular` y `GestorJuego` |
| `static` | ✅ | Constantes `static final` en `GrupoMuscular`, `Personaje`, `GestorJuego` y paneles de vista |
| Al menos un patrón **GRASP** y un principio **SOLID** identificados | ✅ | Ver `DISENO_CLASES.md`, sección 8 (Controller, Information Expert, Pure Fabrication con `CatalogoEventos`, Open/Closed, SRP, Liskov); escribirlo en la memoria |
| Opcional valorado: inmutables, enums, clases internas | ✅ | Enums hechos. `Nivel` y `RegistroDia` inmutables. `OpcionEvento` anidada estática e inmutable. `ComparadorPorFatiga` anidada estática en `PanelMusculos`. Clases internas en `VentanaPrincipal`, `PanelGimnasio` (`OyenteMouseGimnasio`, `MovimientoPersonaje`) y `DialogoEvento` (`OyenteElegirOpcion`) |

## Unidad 4 — Colecciones
| Requisito | Estado | Dónde / qué hacer |
|---|---|---|
| Colecciones genéricas | ✅ | `Map<TipoMusculo, GrupoMuscular>`, `List<Integer>`, `List<EstacionGimnasio>`, `List<RegistroDia>`, `List<Nivel>`, `List<EventoAleatorio>`, `List<OpcionEvento>` |
| Ordenamiento natural o personalizado | ✅ | Natural: `RegistroDia implements Comparable<RegistroDia>` usado con `Collections.sort` en `VentanaPrincipal.mostrarPantallaFin`. Personalizado: `ComparadorPorFatiga implements Comparator<GrupoMuscular>` en `PanelMusculos` (de mayor a menor fatiga) |
| Recorrido con cursores (for-each) | ✅ | Recorridos en `Personaje`, `BancoDescanso` y `PanelGimnasio` |

## Unidad 5 — Excepciones
| Requisito | Estado | Dónde / qué hacer |
|---|---|---|
| Excepción propia | ✅ | `DatosInvalidosException` (checked), `TiempoInsuficienteException` (unchecked) |
| `throw` y `throws` | ✅ | `throw` y `throws` en el constructor de `Personaje`; `throw` de `TiempoInsuficienteException` en `GestorJuego.validarAccion` |
| **`try` / `catch` / `finally`** | ✅ | En `VentanaPrincipal`: `catch DatosInvalidosException` y `catch NumberFormatException` al crear el personaje; `catch (IllegalStateException \| TiempoInsuficienteException)` con `finally { actualizarVista(); }` al usar una estación o cerrar el día; `try / catch (InterruptedException) / finally` en `MovimientoPersonaje` |
| Manejo de excepciones comunes (ej. `NullPointerException`) | ✅ | `NumberFormatException` al parsear edad, `IllegalStateException` e `IllegalArgumentException` en `GestorJuego`, `IOException` al cargar imagen, `InterruptedException` en el hilo. Valores por defecto en las estaciones evitan `NullPointerException` |

## Unidad 6 — Entrada/Salida, Swing, hilos
| Requisito | Estado | Dónde / qué hacer |
|---|---|---|
| Archivo **o** GUI con eventos "según lo acuerde el grupo con el docente" | ✅ / ⚠️ | Implementada GUI Swing con eventos (`ActionListener`, `MouseListener`). Confirmar con el docente el reemplazo de persistencia |
| **Hilos** de ejecución separados | ✅ | `MovimientoPersonaje implements Runnable` ejecutado con `new Thread(...)` en `PanelGimnasio.moverPersonajeHacia(...)`, comunicándose con la UI mediante `SwingUtilities.invokeLater` |

## Entregables y evaluación
- Diagramas UML (clase **y secuencia**), código fuente completo, compilable y ejecutable en Java, **memoria técnica** que explique el diseño y señale dónde se aplica cada tema, y defensa oral.
- Aprobación: mínimo 4 en cada entrega parcial. En la entrega final, nota **igual o superior a 8** para aprobación directa de la cursada; entre 4 y 8 aprueba sin promoción directa.
- Entrega y defensa junto con el 2° Parcial: 16/11/2026 (Grupo 1) o 30/11/2026 (Grupo 2).
