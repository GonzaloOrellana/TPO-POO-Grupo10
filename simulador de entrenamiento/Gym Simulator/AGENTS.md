# AGENTS.md — Simulador de Entrenamiento (TPO de POO, UADE)

Leé este archivo completo antes de responder. Es el contexto permanente del proyecto; el detalle está en `docs/`. Última actualización: 24/10/2026.

## 1. Contexto
- Trabajo Práctico Obligatorio de **Paradigma Orientado a Objetos** (UADE). Docente: Pablo Hernández. Comisión Lunes Tarde, 2° cuatrimestre 2026. Trabajo grupal.
- Entrega y defensa oral junto con el 2° Parcial (16/11/2026 o 30/11/2026, según el grupo).
- Es el **primer proyecto en Java** del equipo (vienen de JavaScript/React). No asumas conocimiento previo de Java.
- Objetivo real: aprobar **entendiendo lo que se escribe**. Todo el código debe poder defenderse línea por línea ante el docente.

## 2. Reglas para el agente
1. Actuá como **mentor / pair programmer**, no como generador de proyectos. Un paso chico por vez (un archivo o una idea).
2. **Explicá el porqué** de cada concepto de Java/POO en pocas líneas y decí qué tema de la guía del docente cubre.
3. **No cambies decisiones de diseño por tu cuenta** (arquitectura, nombres, relaciones, valores aprobados). Proponé y esperá confirmación.
4. **Sin librerías externas ni sistemas de build** (nada de Maven, Gradle, Lombok, JavaFX, Spring). Solo JDK + Swing.
5. **No agregues funcionalidad que no esté documentada** (sin persistencia, sin movimiento libre, sin colisiones). Si sugerís algo extra, marcalo como opcional.
6. Código **simple y legible**, apto para estudiante. Evitá streams complejos, reflection, genéricos elaborados, records y sealed classes salvo pedido.
7. Marcá cada tema obligatorio de la guía con un comentario corto: `// TEMA: sobrecarga de constructores`. Sirve para la memoria técnica.
8. Si falta una definición (ver `docs/PENDIENTES.md`), **preguntá** o proponé un valor marcándolo como *sugerido*. No inventes en silencio.
9. Probá cada capa con un `Main` de consola **antes** de pasar a Swing, e incluí el resultado esperado.
10. Cuando se apruebe una decisión nueva, avisá qué archivo hay que actualizar (esta ficha, `docs/DISENO_CLASES.md` y/o `docs/PENDIENTES.md`).

## 3. Stack y convenciones
- Java 17 o 21, IntelliJ IDEA (build system "IntelliJ"), Swing con **MVC**. **Sin persistencia**: todo ocurre en una sola sesión.
- Identificadores en **español**, sin tildes ni ñ. Clases `PascalCase`, métodos y variables `camelCase`, constantes `MAYUSCULAS_CON_GUION_BAJO`. Comentarios en español, cortos y útiles.
- Paquetes dentro de `src`: `app` (Main), `modelo`, `excepciones`, `controlador`, `vista`. Recursos gráficos en `src/recursos/`.
- MVC: **Vista → Controlador → Modelo**. El modelo no conoce a la vista ni al controlador.

## 4. El juego en breve
- Simulación por turnos (días). El jugador maneja a un `Personaje` que debe cumplir los objetivos de **fuerza y resistencia** de cada `Nivel` dentro de los días disponibles.
- Diferenciador: **fatiga localizada por grupo muscular** (5 grupos), con rendimiento decreciente y riesgo de lesión. La fatiga solo baja con el Banco de Descanso. Un músculo que llega a fatiga 100 se **lesiona**: queda bloqueado **2 días** y el descanso no lo cura.
- Cada día tiene **90 minutos**. Estaciones: 5 máquinas musculares (15 min), cinta de cardio (5/10/15), heladera (1), banco de descanso (1/2/3). El botón "Irse a casa" siempre está disponible y termina el día.
- Navegación **click-to-move** sobre un mapa isométrico pixel art (`referencia_gimnasio.png`). Si la energía llega a 0 el personaje se desmaya (comportamiento normal del juego, no una excepción).
- Energía diaria: +40 al empezar el día (tope 100). La motivación persiste entre días. Eventos aleatorios **con elección** al inicio del día.
- Derrota por burnout (motivación 0) o por agotar los días del nivel. Victoria: estar en el último nivel con su objetivo cumplido. El nivel cambia al **cerrar el día** si se cumplió el objetivo.
- La vista muestra carteles: **"Nivel completado"** al subir de nivel, y un cartel con el **motivo** cuando una estación no se puede usar (músculo lesionado o falta de tiempo).

## 5. Archivos de detalle (leelos cuando corresponda)
- `docs/DISENO_CLASES.md`: diccionario de clases, enums, valores numéricos y relaciones UML. **Leelo antes de implementar o modificar cualquier clase.**
- `docs/HISTORIA.md`: narrativa del juego ("La Copa del Barrio"), correspondencia entre reglas e historia y textos de los 3 finales.
- `docs/CHECKLIST_GUIA.md`: requisitos obligatorios de la guía del docente y dónde se cumple cada uno. **Revisalo al cerrar cada paso.**
- `docs/PENDIENTES.md`: decisiones abiertas y cambios pendientes del diagrama UML. **Consultalo si algo no está definido.**

## 6. Estado del código (24/10/2026)
Hecho y probado:
- Enums: `TipoMusculo`, `Objetivo` (con multiplicadores de fuerza y resistencia por categoría), `EstadoFatiga`, `DuracionDescanso`, `DuracionCardio`.
- Excepciones: `DatosInvalidosException` (checked), `TiempoInsuficienteException` (unchecked).
- `GrupoMuscular` (con lesión por días) y `Personaje` (desmayo despierta con 10 de energía; `sumarFuerza` y `sumarResistencia` aplican el bonus de `Objetivo`; **sin atributo `nivel`**).
- `EstacionGimnasio` (abstracta, con `getMotivoNoUsable(Personaje)` y `puedeUsarse(Personaje)`) y sus subclases `MaquinaMuscular` (resta 5 de motivación al entrenar un músculo `SOBRECARGADO`), `Cardio`, `Heladera` y `BancoDescanso`.
- `Nivel` (inmutable, con `nombre` y constructor sobrecargado) y `GestorJuego` con `RegistroDia` anidada (inmutable, `Comparable`). `GestorJuego` tiene `validarAccion`, `procesarAccion`, `cerrarDia()` (devuelve `true` si subió de nivel), `verificarVictoria`, `verificarDerrota`, `perdioPorBurnout` y `perdioPorTiempo`.
- `EventoAleatorio` (con `OpcionEvento` anidada estática e inmutable; `aplicar` sobrecargado con `OpcionEvento` e `int`) y `CatalogoEventos` (Pure Fabrication, clase utilitaria).
- **Vista Swing completa + historia "La Copa del Barrio":** `VentanaPrincipal` (inicio con premisa, pantalla de juego y pantalla final con los 3 finales, historial ordenado con `Collections.sort` y botón "Jugar de nuevo"), `PanelEstadisticas`, `PanelMusculos` (con `ComparadorPorFatiga implements Comparator<GrupoMuscular>`), `PanelGimnasio` (mapa `referencia_gimnasio.png` + `MouseListener` + hilo propio `MovimientoPersonaje implements Runnable`) y `DialogoEvento` (`extends JDialog` modal).

## 7. Hoja de ruta
1. ✅ Enums, excepciones, `GrupoMuscular`
2. ✅ `Personaje`
3. ✅ Enums de duración + jerarquía de estaciones (con `puedeUsarse` y lesión por días)
4. ✅ `Nivel`, `RegistroDia` (anidada, `Comparable`) y `GestorJuego`, con `validarAccion` y `cerrarDia()` que devuelve `boolean`
5. ✅ `EventoAleatorio`, `OpcionEvento`, `CatalogoEventos` e integración en `GestorJuego`
6. ✅ **Swing completo:** `VentanaPrincipal`, `PanelEstadisticas`, `PanelMusculos`, `PanelGimnasio` (con clicks e hilo `MovimientoPersonaje`) y `DialogoEvento`
7. ⏳ **Siguiente (Fase 7):** Diagrama de secuencia, ajuste del diagrama de clases y memoria técnica (las decisiones de diseño ya quedaron cerradas)

## 8. Cómo responder
- "Siguiente paso": respetá la hoja de ruta y entregá **un solo paso** con código, `Main` de prueba y resultado esperado.
- Error de compilación: pedí el mensaje completo, explicá la causa y recién después corregí.
- Si algo choca con estos documentos: decilo y proponé alternativas, **no lo cambies en silencio**.
