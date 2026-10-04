package semana8.dao;

import semana8.conexion.ConexionBD;
import semana8.modelo.EstadoPedido;
import semana8.modelo.Pedido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// DAO de la tabla "pedidos": es la ÚNICA clase que escribe SQL para esa tabla.
// Tiene las 4 operaciones CRUD (create, readAll, update, delete)
// y además actualizarEstado(), que usan "Asignar Repartidor" y "Marcar como Entregado".
public class PedidoDAO {

    // CREATE: inserta un pedido nuevo. Devuelve true si se guardó bien, false si algo falló.
    public boolean create(Pedido pedido) {
        String sql = "INSERT INTO pedidos (direccion, tipo, estado) VALUES (?, ?, ?)";

        // try-with-resources: lo que se abre dentro de los paréntesis (conexión y statement)
        // se cierra automáticamente al terminar, haya error o no.
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Llenamos cada "?" en orden (el primero es el 1, no el 0)
            stmt.setString(1, pedido.getDireccionEntrega());
            stmt.setString(2, pedido.getTipo());
            stmt.setString(3, pedido.getEstado().name()); // name() convierte el enum a texto: "PENDIENTE"

            // executeUpdate() se usa para INSERT, UPDATE y DELETE (sentencias que modifican datos)
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al guardar el pedido: " + e.getMessage());
            return false;
        }
    }

    // READ: trae todos los pedidos, junto con el nombre de su repartidor (si tiene una entrega asignada).
    public List<Pedido> readAll() {
        List<Pedido> pedidos = new ArrayList<>();

        // LEFT JOIN: trae TODOS los pedidos, aunque todavía no tengan entrega ni repartidor
        // (en ese caso, nombre_repartidor viene como null).
        String sql = "SELECT p.id, p.direccion, p.tipo, p.estado, r.nombre AS nombre_repartidor "
                + "FROM pedidos p "
                + "LEFT JOIN entregas e ON e.id_pedido = p.id "
                + "LEFT JOIN repartidores r ON r.id = e.id_repartidor "
                + "ORDER BY p.id";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) { // executeQuery() se usa para SELECT

            // El ResultSet es como un cursor que recorre las filas del resultado, una por una.
            while (rs.next()) {
                Pedido p = new Pedido(rs.getInt("id"), rs.getString("direccion"), rs.getString("tipo"));
                p.setEstado(EstadoPedido.valueOf(rs.getString("estado"))); // texto "EN_REPARTO" -> enum
                p.setNombreRepartidor(rs.getString("nombre_repartidor"));   // puede ser null
                pedidos.add(p);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar los pedidos: " + e.getMessage());
        }

        // Si hubo error, se devuelve la lista vacía (nunca null), así la vista no se cae
        return pedidos;
    }

    // UPDATE: modifica la dirección, el tipo y el estado de un pedido que ya existe.
    public boolean update(Pedido pedido) {
        // El WHERE es clave: sin él, se modificarían TODOS los pedidos de la tabla
        String sql = "UPDATE pedidos SET direccion = ?, tipo = ?, estado = ? WHERE id = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pedido.getDireccionEntrega());
            stmt.setString(2, pedido.getTipo());
            stmt.setString(3, pedido.getEstado().name());
            stmt.setInt(4, pedido.getId()); // el último "?" es el del WHERE: a qué pedido le aplicamos el cambio

            // executeUpdate() devuelve cuántas filas cambió. Si es 0, no existía un pedido con ese id.
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar el pedido: " + e.getMessage());
            return false;
        }
    }

    // DELETE: elimina un pedido según su id.
    // Si el pedido tiene una entrega registrada, MySQL NO deja borrarlo (llave foránea en "entregas")
    // y lanza un SQLException: en ese caso devolvemos false.
    public boolean delete(int id) {
        String sql = "DELETE FROM pedidos WHERE id = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar el pedido: " + e.getMessage());
            return false;
        }
    }

    // Cambia SOLO el estado de un pedido (por ejemplo, de EN_REPARTO a ENTREGADO).
    public boolean actualizarEstado(int idPedido, EstadoPedido nuevoEstado) {
        String sql = "UPDATE pedidos SET estado = ? WHERE id = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevoEstado.name());
            stmt.setInt(2, idPedido);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar el estado del pedido: " + e.getMessage());
            return false;
        }
    }
}
