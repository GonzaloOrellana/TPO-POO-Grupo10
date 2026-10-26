# PENDIENTES.md — Decisiones abiertas y cambios pendientes

Estado: 24/10/2026. **Si algo de esta lista afecta lo que te piden, preguntá en vez de inventar.**
Cuando el grupo apruebe una decisión: pasarla a `docs/DISENO_CLASES.md`, borrarla de acá y, si corresponde, actualizar `AGENTS.md`.

Resueltas recientemente (ya están en `DISENO_CLASES.md` y `HISTORIA.md`): historia "La Copa del Barrio"; efecto de `Objetivo` (+25% en la estadística de su categoría o +10% en ambas para `HIPERTROFIA`); despertar tras desmayo con 10 de energía (`ENERGIA_AL_DESPERTAR = 10`); penalización de -5 de motivación al entrenar un músculo `SOBRECARGADO`; `Nivel` con `nombre` ("Clasificatoria", "Semifinal", "Final"); separación de derrota en `perdioPorBurnout()` y `perdioPorTiempo()`; pantalla final con los 3 finales, historial ordenado con `Collections.sort` y botón "Jugar de nuevo".

## 1. Decisiones de diseño abiertas

1. **Confirmar con el docente** que la GUI Swing con manejo de eventos reemplaza el requisito de archivo.
(El requisito de interfaz propia con contrato ya queda cubierto con `PanelGimnasio.OyenteEstacion`, además de `Comparable`, `Comparator` y `Runnable`).

## 2. Cambios pendientes en el diagrama UML
El docente aprobó el diagrama pero avisó que puede cambiar al codear. Cuando termine la vista, actualizarlo con:

- `GrupoMuscular`: atributo `diasLesionRestantes`; métodos `pasarDia()`, `reiniciar()` y `actualizarEstado()` con visibilidad `~` (package-private); `getDiasLesionRestantes()`.
- `Personaje`: **quitar `nivel`**; agregar `sumarResistencia(int)` y `pasarDia()`.
- `EstacionGimnasio`: constructor `protected`, getters `getX()` / `getY()`, `getMotivoNoUsable(Personaje)` y `puedeUsarse(Personaje)`; `MaquinaMuscular` sobreescribe `getMotivoNoUsable`.
- `GestorJuego`: atributos `eventos`, `azar`, `eventoPendiente`; dos constructores; `validarAccion`, `perdioPorBurnout()`, `perdioPorTiempo()`, `cerrarDia()` que devuelve `boolean`, `hayEventoPendiente()`, `getEventoPendiente()` y `resolverEvento(int)`.
- `RegistroDia`: agregar `numeroNivel`; clase estática, final e inmutable; constructor privado.
- `Nivel`: inmutable (atributos `final`), agregar atributo `nombre : String`, `getNombre()` y constructor sobrecargado.
- `EventoAleatorio`: `aplicar` sobrecargado (`OpcionEvento` e `int`); `OpcionEvento` estática, final e inmutable.
- **Clase nueva `CatalogoEventos`** (utilitaria, constructor privado, método estático `crearEventos()`), con una dependencia hacia `EventoAleatorio`.
- Atributos tipados con enums en lugar de `String` (`Map<TipoMusculo, GrupoMuscular>`, `Objetivo`, `EstadoFatiga`, `TipoMusculo`, `DuracionDescanso`, `DuracionCardio`). Los enums siguen como notas, no como clases.
- `MaquinaMuscular`: dos constructores sobrecargados y atributo `grupoQueTrabaja` de tipo `TipoMusculo`.
- `Cardio` y `BancoDescanso`: `getDuracionSeleccionada()`; ambas arrancan en `CORTO`.
- `VentanaPrincipal`: agregar `txtEdad`, `mostrarPantallaFin`, `OyenteJugarDeNuevo` y referencias a `PanelEstadisticas`, `PanelGimnasio` y `PanelMusculos`.
- Agregar `PanelEstadisticas`, `PanelMusculos` (con `ComparadorPorFatiga`), `PanelGimnasio` (con `OyenteMouseGimnasio`, `OyenteEstacion` y el hilo de movimiento) y `DialogoEvento`.
- `RegistroDia` aparece con una "interfaz" `Comparable_RegistroDia`: en el código es `java.lang.Comparable<RegistroDia>`.

## 3. Entregables que faltan
- **Diagrama de secuencia** (obligatorio). Propuesta: "entrenar en una máquina", con marco `alt` para `TiempoInsuficienteException` (el flujo pasa por `validarAccion`). Se hace más adelante, antes de la entrega.
- **Memoria técnica**: explicar el diseño y señalar dónde se aplica cada tema (los comentarios `// TEMA: ...` del código sirven para armarla).
