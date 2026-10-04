package semana8.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Clase encargada de abrir la conexión con la base de datos MySQL.
// Es la ÚNICA clase que conoce la URL, el usuario y la contraseña de la BD.

public class ConexionBD {

    // Dirección del servidor: "localhost" = este mismo computador,
    // 3306 = puerto por defecto de MySQL, speedfast_db = nombre de la base de datos
    private static final String URL = "jdbc:mysql://localhost:3306/speedfast_db";
    private static final String USUARIO = "root";
    private static final String CONTRASENA = "tu_contraseña"; // <-- La contraseña de MySQL

    // Abre y devuelve una conexión nueva.
    // Es "static" para poder llamarla sin crear un objeto: ConexionBD.obtenerConexion()

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }

    // Prueba si se puede conectar a MySQL. Devuelve true si funcionó, false si no.
    // Se usa al iniciar la aplicación (en Main) para avisar al usuario si MySQL está apagado.


    public static boolean probarConexion() {
        Connection conn = null;
        try {
            conn = obtenerConexion();
            System.out.println("Conexión exitosa a la base de datos.");
            return true;
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos: " + e.getMessage());
            return false;
        } finally {
            // Cerramos la conexión para no dejar recursos abiertos en MySQL
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar la conexión: " + e.getMessage());
                }
            }
        }
    }
}
