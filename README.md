# SpeedFast – Gestión de pedidos y entregas (Semana 8)

Aplicación de escritorio en **Java Swing** con persistencia en **MySQL** mediante **JDBC**, desarrollada para la actividad sumativa de la semana 8 de *Desarrollo Orientado a Objetos II* (DUOC UC).

Permite gestionar **repartidores**, **pedidos** y **entregas** con operaciones CRUD completas (crear, leer, actualizar y eliminar).

**Autor:** Kevin Huaiquimilla Neira

---

## Requisitos

- Java 17 o superior
- Maven (incluido en IntelliJ IDEA)
- MySQL 8 (servicio encendido)

## Cómo ejecutar

1. Ejecutar el script `src/main/resources/speedfast_db.sql` en MySQL Workbench. Crea la base de datos `speedfast_db` con las tablas `repartidores`, `pedidos` y `entregas`.
2. Abrir `src/main/java/semana8/conexion/ConexionBD.java` y reemplazar `"tu_contraseña"` por la contraseña del usuario `root` de MySQL.
3. Abrir el proyecto en IntelliJ IDEA y recargar Maven, para descargar el conector `mysql-connector-java`.
4. Ejecutar `src/main/java/semana8/main/Main.java`.

## Estructura del proyecto (separación por capas)

| Paquete | Contenido |
|---|---|
| `modelo` | Clases que representan los datos: `Repartidor`, `Pedido`, `Entrega` y el enum `EstadoPedido`. |
| `conexion` | `ConexionBD`: única clase que conoce la URL, el usuario y la contraseña de la base de datos. |
| `dao` | `RepartidorDAO`, `PedidoDAO`, `EntregaDAO`: únicas clases que escriben SQL. Cada una tiene `create()`, `readAll()`, `update()` y `delete()`, usando `PreparedStatement` y `ResultSet`. |
| `controlador` | Intermediarios entre las ventanas y los DAO: validan los datos y aplican las reglas de negocio. |
| `vista` | Ventanas Swing (`JFrame`, `JPanel`, `JTable`, `JTextField`, `JComboBox`, `JButton`, `JOptionPane`). |
| `main` | `Main`: prueba la conexión y abre la ventana principal. |

## Funcionalidades

**Repartidores** (botón *Gestionar Repartidores*)
- Registrar, editar y eliminar repartidores, con el listado en una tabla.

**Pedidos** (ventana principal)
- Registrar pedidos con dirección, tipo (COMIDA, ENCOMIENDA, EXPRESS) y estado (PENDIENTE, EN_REPARTO, ENTREGADO).
- Editar y eliminar pedidos.
- Filtrar la tabla por estado y por tipo.
- Marcar como entregado un pedido que está EN_REPARTO.

**Entregas** (botón *Gestionar Entregas*)
- Registrar una entrega asociando un pedido y un repartidor, con fecha y hora. El pedido y el repartidor se eligen en un `JComboBox` cargado desde la base de datos, que muestra "id - nombre/dirección" y conserva el id internamente.
- Editar y eliminar entregas.
- Filtrar la tabla por pedido o por repartidor.
- *Asignar Repartidor*: asignación rápida con la fecha y hora actuales.

## Validaciones y manejo de errores

- Campos obligatorios y largo máximo según las columnas de la base de datos (nombre: 100, dirección: 100).
- Formato de fecha `aaaa-mm-dd` y de hora `HH:mm`, con detección de fechas inexistentes (por ejemplo, 30 de febrero).
- Regla de negocio: cada pedido puede tener como máximo una entrega.
- Los estados se actualizan solos: al registrar una entrega, el pedido pasa a EN_REPARTO; al eliminarla, vuelve a PENDIENTE.
- Las excepciones SQL se capturan con `try-catch` en los DAO. El detalle técnico se muestra en la consola y el usuario ve un mensaje claro con `JOptionPane`. Por ejemplo, al intentar eliminar un repartidor que tiene entregas registradas, el sistema explica que primero hay que eliminar esas entregas, por la llave foránea.
- Todos los recursos (`Connection`, `PreparedStatement`, `ResultSet`) se cierran automáticamente con `try-with-resources`.

## Notas

- El paso 2 de las instrucciones menciona `ClienteDAO`; en el caso SpeedFast la entidad equivalente es el repartidor, por lo que se implementó `RepartidorDAO`.

## Mejoras propuestas

- Agregar un teléfono de contacto a cada repartidor, para poder comunicarse con él durante una entrega.
