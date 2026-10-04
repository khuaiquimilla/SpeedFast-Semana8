package semana8.controlador;

import semana8.dao.RepartidorDAO;
import semana8.modelo.Repartidor;

import java.util.List;

// CONTROLADOR de repartidores: intermediario entre las ventanas y RepartidorDAO.
// Valida los datos antes de mandarlos a la base de datos.
// Si algo está mal, lanza una IllegalArgumentException con un mensaje claro
// que la ventana le muestra al usuario con JOptionPane.
public class ControladorRepartidores {

    private final RepartidorDAO repartidorDAO;

    public ControladorRepartidores() {
        this.repartidorDAO = new RepartidorDAO();
    }

    // Valida el nombre y guarda un repartidor nuevo.
    public void registrarRepartidor(String nombre) throws IllegalArgumentException {
        validarNombre(nombre);

        Repartidor nuevo = new Repartidor(0, nombre); // el 0 es relleno: MySQL asigna el id real
        boolean exito = repartidorDAO.create(nuevo);

        if (!exito) {
            throw new IllegalArgumentException("No se pudo guardar el repartidor. Revisa que MySQL esté encendido.");
        }
    }

    // Valida el nombre nuevo y lo guarda en el repartidor que tiene ese id.
    public void actualizarRepartidor(int id, String nombre) throws IllegalArgumentException {
        validarNombre(nombre);

        Repartidor editado = new Repartidor(id, nombre); // aquí el id SÍ importa: dice a quién modificar
        boolean exito = repartidorDAO.update(editado);

        if (!exito) {
            throw new IllegalArgumentException("No se pudo actualizar el repartidor. Revisa que todavía exista.");
        }
    }

    // Elimina el repartidor que tiene ese id.
    public void eliminarRepartidor(int id) throws IllegalArgumentException {
        boolean exito = repartidorDAO.delete(id);

        if (!exito) {
            // La causa más común: el repartidor tiene entregas registradas y la llave foránea
            // de la tabla "entregas" no permite borrarlo.
            throw new IllegalArgumentException("No se pudo eliminar el repartidor.\n"
                    + "Si tiene entregas registradas, primero elimina esas entregas.");
        }
    }

    // Devuelve todos los repartidores (los usan la tabla de repartidores y el JComboBox de entregas)
    public List<Repartidor> listarRepartidores() {
        return repartidorDAO.readAll();
    }

    // Validación reutilizable: la usan registrar y actualizar, así no repetimos el mismo código dos veces.
    private void validarNombre(String nombre) throws IllegalArgumentException {
        if (nombre == null || nombre.isEmpty()) {
            throw new IllegalArgumentException("Por favor, ingrese un nombre.");
        }
        // 100 es el largo máximo de la columna "nombre" (VARCHAR(100)) en la base de datos
        if (nombre.length() > 100) {
            throw new IllegalArgumentException("El nombre no puede superar los 100 caracteres.");
        }
    }
}
