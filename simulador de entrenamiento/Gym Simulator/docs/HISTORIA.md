# HISTORIA.md — Narrativa del juego: "La Copa del Barrio"

Estado: 24/10/2026 (aprobado por el grupo).

## 1. Premisa
*"Hace años que dejaste de entrenar. Pasás por el gimnasio del barrio y ves el cartel: **COPA DEL BARRIO – Inscripciones abiertas**. Te anotás sin pensarlo. Tenés pocos días para prepararte… y no te podés rendir."*

El protagonista es una persona común (el jugador elige nombre y edad) que busca demostrarse que todavía puede ponerse una meta exigente y cumplirla.

## 2. Correspondencia entre reglas e historia
| Regla del modelo | Qué significa en la historia |
|---|---|
| `Objetivo` (`FUERZA` / `RESISTENCIA` / `HIPERTROFIA`) | La **categoría** en la que se anota: levantamiento (+25% fuerza), carrera (+25% resistencia) o fisicoculturismo (+10% en ambas). Todas las categorías exigen una base de fuerza y de resistencia |
| 3 instancias de `Nivel` | Las 3 etapas del torneo: **Clasificatoria** (Nivel 1), **Semifinal** (Nivel 2) y **Final** (Nivel 3) |
| `diasDisponibles` (5 → 4 → 3) | Los días que faltan para cada etapa: a medida que se acerca la final, hay menos margen |
| 90 minutos por día | El rato libre que le deja su rutina diaria (trabajo, estudio, familia) |
| Botón "Irse a casa" | Terminar el entrenamiento del día y volver a casa |
| Energía (+40 cada mañana) | Lo que recupera durmiendo a la noche |
| Fatiga localizada y lesión (2 días) | El cuerpo avisa: si ignora la sobrecarga y sigue entrenando el mismo músculo, se lesiona |
| Penalización por entrenar `SOBRECARGADO` (-5 motivación) | Entrenar dolorido y rindiendo al 25% frustra y desanima al personaje |
| Motivación (burnout en 0) | Las ganas de seguir. Si cae a 0, abandona la preparación |
| Desmayo (energía 0 → pierde 10% stats y despierta con 10 de energía) | Se exige de más, se descompensa en el gimnasio y se despierta débil: tiene que hidratarse o descansar antes de volver a entrenar |
| `EventoAleatorio` | La vida cotidiana fuera del gimnasio (dormir mal, cruzarse con un amigo, gimnasio lleno, escuchar su canción favorita) |

## 3. Los tres finales (en `VentanaPrincipal.verificarFinDePartida`)
1. **Victoria (`verificarVictoria()`):** *"¡GANASTE LA COPA DEL BARRIO! Saliste campeón en la categoría {objetivo}. Hace unos días ni te animabas a entrar al gimnasio."*
2. **Derrota por burnout (`perdioPorBurnout()`):** *"ABANDONASTE LA COPA. Te quedaste sin ganas de seguir entrenando. Quizás el año que viene…"*
3. **Derrota por tiempo (`perdioPorTiempo()`):** *"QUEDASTE AFUERA. Llegó el día de la {etapa} y no alcanzaste la preparación necesaria."*

En los tres casos la vista reemplaza el gimnasio por la pantalla final con las estadísticas alcanzadas, el historial día por día ordenado con `Collections.sort` (`RegistroDia implements Comparable`) y el botón **"JUGAR DE NUEVO"**.
