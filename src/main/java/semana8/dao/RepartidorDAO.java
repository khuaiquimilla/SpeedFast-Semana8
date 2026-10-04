package semana8.dao;

import semana8.conexion.ConexionBD;
import semana8.modelo.Repartidor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// DAO de la tabla "repartidores": es la única clase que escribe SQL para esa tabla.
// Tiene las 4 operaciones CRUD: create (crear), readAll (leer), update (actualizar) y delete (eliminar).
public class RepartidorDAO {

    // CREATE: inserta un repartidor nuevo. Devuelve true si se guardó bien, false si algo falló.
    public boolean create(Repartidor repartidor) {
        // Solo enviamos el nombre: el id lo genera MySQL (AUTO_INCREMENT)
        String sql = "INSERT INTO repartidores (nombre) VALUES (?)";

        // try-with-resources: la conexión y el statement se cierran solos al terminar
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, repartidor.getNombre()); // llena el primer "?"
            stmt.executeUpdate();                      // ejecuta el INSERT
            return true;

        } catch (SQLException e) {
            System.err.println("Error al guardar el repartidor: " + e.getMessage());
            return false;
        }
    }

    // READ: trae todos los repartidores de la base de datos, como una lista de objetos Repartidor.
    public List<Repartidor> readAll() {
        List<Repartidor> repartidores = new ArrayList<>();
        String sql = "SELECT id, nombre FROM repartidores ORDER BY nombre";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Recorremos cada fila del resultado y la convertimos en un objeto Repartidor
            while (rs.next()) {
                Repartidor r = new Repartidor(rs.getInt("id"), rs.getString("nombre"));
                repartidores.add(r);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar los repartidores: " + e.getMessage());
        }

        return repartidores;
    }

    // UPDATE: cambia el nombre de un repartidor que ya existe.
    // Recibe el objeto completo: de él sacamos el nombre nuevo y el id del repartidor a modificar.
    public boolean update(Repartidor repartidor) {
        // El WHERE es clave: sin él, se cambiaría el nombre de TODOS los repartidores
        String sql = "UPDATE repartidores SET nombre = ? WHERE id = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, repartidor.getNombre()); // primer "?": el nombre nuevo
            stmt.setInt(2, repartidor.getId());        // segundo "?": a qué repartidor se lo cambiamos

            // executeUpdate() devuelve cuántas filas cambió.
            // Si es 0, significa que no existía un repartidor con ese id.
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar el repartidor: " + e.getMessage());
            return false;
        }
    }

    // DELETE: elimina un repartidor según su id.
    // Si el repartidor tiene entregas registradas, MySQL NO deja borrarlo (por la llave foránea
    // en la tabla "entregas") y lanza un SQLException: en ese caso devolvemos false.
    public boolean delete(int id) {
        String sql = "DELETE FROM repartidores WHERE id = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id); // el único "?": el id del repartidor a borrar
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar el repartidor: " + e.getMessage());
            return false;
        }
    }
}