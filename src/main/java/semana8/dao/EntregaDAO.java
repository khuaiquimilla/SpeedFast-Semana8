package semana8.dao;

import semana8.conexion.ConexionBD;
import semana8.modelo.Entrega;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

// DAO de la tabla "entregas": es la única clase que escribe SQL para esa tabla.
// Registra qué repartidor quedó a cargo de qué pedido, y cuándo.
// Tiene las 4 operaciones CRUD: create, readAll, update y delete.
public class EntregaDAO {

    // CREATE: inserta una entrega nueva. Devuelve true si se guardó bien, false si algo falló.
    public boolean create(Entrega entrega) {
        String sql = "INSERT INTO entregas (id_pedido, id_repartidor, fecha, hora) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Estos dos ids son llaves foráneas: MySQL revisa que el pedido y el repartidor
            // existan de verdad. Si no existen, lanza un SQLException y no guarda nada.
            stmt.setInt(1, entrega.getIdPedido());
            stmt.setInt(2, entrega.getIdRepartidor());

            // Java usa LocalDate/LocalTime, pero JDBC trabaja con java.sql.Date/Time.
            // Date.valueOf() y Time.valueOf() hacen la conversión.
            stmt.setDate(3, Date.valueOf(entrega.getFecha()));
            stmt.setTime(4, Time.valueOf(entrega.getHora()));

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al guardar la entrega: " + e.getMessage());
            return false;
        }
    }

    // READ: trae todas las entregas, con la dirección del pedido y el nombre del repartidor.
    // Las más recientes aparecen primero.
    public List<Entrega> readAll() {
        List<Entrega> entregas = new ArrayList<>();

        // JOIN: une cada entrega con su pedido y su repartidor para traer datos legibles
        String sql = "SELECT e.id, e.id_pedido, e.id_repartidor, e.fecha, e.hora, "
                + "p.direccion, r.nombre "
                + "FROM entregas e "
                + "JOIN pedidos p ON p.id = e.id_pedido "
                + "JOIN repartidores r ON r.id = e.id_repartidor "
                + "ORDER BY e.fecha DESC, e.hora DESC";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Al revés que al guardar: java.sql.Date/Time -> LocalDate/LocalTime
                Entrega entrega = new Entrega(
                        rs.getInt("id"),
                        rs.getInt("id_pedido"),
                        rs.getInt("id_repartidor"),
                        rs.getDate("fecha").toLocalDate(),
                        rs.getTime("hora").toLocalTime());
                entrega.setDireccionPedido(rs.getString("direccion"));
                entrega.setNombreRepartidor(rs.getString("nombre"));
                entregas.add(entrega);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar las entregas: " + e.getMessage());
        }

        // Si hubo error, se devuelve la lista vacía (nunca null), así la vista no se cae
        return entregas;
    }

    // UPDATE: modifica el pedido, el repartidor, la fecha y la hora de una entrega que ya existe.
    public boolean update(Entrega entrega) {
        // El WHERE es clave: sin él, se modificarían TODAS las entregas
        String sql = "UPDATE entregas SET id_pedido = ?, id_repartidor = ?, fecha = ?, hora = ? WHERE id = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, entrega.getIdPedido());
            stmt.setInt(2, entrega.getIdRepartidor());
            stmt.setDate(3, Date.valueOf(entrega.getFecha()));
            stmt.setTime(4, Time.valueOf(entrega.getHora()));
            stmt.setInt(5, entrega.getId()); // el último "?" es el del WHERE

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar la entrega: " + e.getMessage());
            return false;
        }
    }

    // DELETE: elimina una entrega según su id.
    // Aquí no hay problema de llave foránea: ninguna otra tabla apunta a "entregas".
    public boolean delete(int id) {
        String sql = "DELETE FROM entregas WHERE id = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar la entrega: " + e.getMessage());
            return false;
        }
    }
}
