# Client for landmine

> It currently works for a fork of the original project on https://github.com/TeoryMan143/compu-internet-1

# Análisis Conceptual

## 1. Delimitación de Mensajes en TCP

TCP es un protocolo **orientado a flujo de bytes** (stream), no a mensajes ni bloques. Esto significa que no existe ningún concepto de "fin de mensaje" a nivel de protocolo: los bytes que el cliente escribe se concatenan en un buffer y el receptor los lee como un flujo continuo, sin garantía de que una sola escritura (`write`) corresponda a una sola lectura (`read`).

Si el cliente envía el JSON **sin `\n`**, el servidor —que normalmente usa `BufferedReader.readLine()`— quedará esperando indefinidamente un salto de línea que nunca llega, porque `readLine()` bloquea hasta encontrar el delimitador. El mensaje puede haber llegado físicamente al socket, pero el servidor no sabe "dónde termina".

Si el cliente **no hace `flush()`**, los datos pueden quedar retenidos en el buffer interno del `Writer`/`Stream` del lado del cliente y nunca salir realmente por la red, provocando que el servidor no reciba nada y se bloquee esperando datos.

Esto es lo que se llama **TCP Framing**: la necesidad de que la aplicación (no TCP) defina explícitamente los límites de cada mensaje, ya sea mediante:
- Un delimitador (`\n`, `\0`, etc.)
- Un prefijo con la longitud del mensaje (length-prefixing)
- Un formato de tamaño fijo

## 2. Ventajas y Sobrecargas de ThreadPools

Usar `Executors.newFixedThreadPool(5)` en vez de `new Thread()` por cliente ofrece:

- **Reutilización de hilos**: evita el costo de crear/destruir hilos (creación de pila, registro en el SO) por cada conexión.
- **Control de recursos**: limita el número máximo de hilos concurrentes, evitando que el servidor colapse por agotamiento de memoria o de contexto si llegan miles de clientes.
- **Cola de tareas interna**: las tareas que no caben en el pool se encolan automáticamente.

**Con 6 o más clientes simultáneos**: como el pool tiene tamaño fijo (5), la sexta conexión **no se rechaza**, sino que su tarea queda **encolada** en la `BlockingQueue` interna del `ExecutorService`, esperando a que se libere un hilo. Ese sexto cliente experimentará latencia (o incluso bloqueo aparente) hasta que alguno de los 5 hilos activos termine su tarea.

## 3. Condiciones de Carrera y Exclusión Mutua

Si dos clientes ejecutan `selectCell` casi al mismo tiempo sobre la instancia compartida de `BoardGame` **sin `synchronized`**, ocurre una **condición de carrera (race condition)**.

El problema concreto: la operación de "leer el estado del tablero → validar la jugada → escribir el nuevo estado" no es atómica. Es posible que:
- Ambos hilos lean el mismo estado del tablero antes de que ninguno escriba.
- Ambos validen sus jugadas como correctas (porque cada uno ve el tablero "viejo").
- Ambos escriban, y una de las dos jugadas **sobrescriba** o **corrompa** el resultado de la otra (lost update).

Esto puede dejar el tablero en un estado inconsistente, donde una celda parece ocupada por el jugador equivocado o se pierde una jugada válida. La solución es declarar `synchronized` en los métodos que leen y modifican el estado compartido, garantizando **exclusión mutua**.

## 4. Conexiones Cortas vs. Conexiones Persistentes

| Aspecto | Short-lived (cierra tras cada comando) | Persistent (socket abierto toda la partida) |
|---|---|---|
| **Descriptores de archivo** | Bajo uso por conexión, pero alta tasa de apertura/cierre (overhead del SO y handshake/TCP teardown) | Un descriptor ocupado por cliente durante toda la sesión; más descriptores simultáneos retenidos, pero sin reapertura constante |
| **Latencia** | Cada comando paga el costo de un nuevo *3-way handshake* (y potencialmente *TIME_WAIT*), aumentando la latencia percibida | Solo se paga el handshake una vez; los comandos posteriores son más rápidos |
| **Escalabilidad** | Puede saturar el sistema con conexiones en estado `TIME_WAIT`, agotando puertos efímeros con muchos clientes | Escala mejor en número de comandos, pero requiere mantener hilos/recursos reservados por cliente durante toda la partida, limitando el número de partidas simultáneas |

En resumen: short-lived es más simple de implementar pero ineficiente para interacciones frecuentes; persistent es más eficiente en latencia pero exige gestionar mejor el ciclo de vida de los hilos y detectar desconexiones (timeouts, clientes zombis).

## 5. Declaración y Reflexión sobre IAG

## 5. Declaración y Reflexión sobre IAG

**Herramienta utilizada:** Github Copilot.

**Prompt empleado:**

> "I want this BoardService class to have both the ServerConnector and the BoardGame this is the main way the Main class is going to interact with the system, every action sent through the ServerConnector (that always returns the new board Cell[][]) must update the board locally. also implement the user interaction based on the code thats already there in the Main class"

**Contexto de uso:** Este prompt se usó específicamente para integrar la capa de red (`ServerConnector`) con la lógica del tablero (`BoardGame`) a través de una clase intermedia `BoardService`, que centraliza la comunicación cliente-servidor y mantiene sincronizado el estado local del tablero con cada respuesta del servidor.

**Validación del código generado:**

- Se revisó manualmente que cada método de `BoardService` que invoca al `ServerConnector` actualice el arreglo `Cell[][]` local únicamente después de recibir una respuesta válida del servidor, evitando que la interfaz muestre un estado desincronizado.
- Se probó la interacción de usuario integrada en `Main` con múltiples ejecuciones manuales, confirmando que las entradas del usuario se traducían correctamente en las llamadas correspondientes a `BoardService`.