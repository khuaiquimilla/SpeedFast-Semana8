package semana8.controlador;

import semana8.dao.PedidoDAO;
import semana8.modelo.EstadoPedido;
import semana8.modelo.Pedido;

import javax.swing.table.DefaultTableModel;
import java.util.List;

// CONTROLADOR de pedidos: es el intermediario entre las ventanas (vista) y el DAO (base de datos).
//  - Recibe los datos que escribió el usuario en la ventana
//  - Los valida (revisa que tengan sentido)
//  - Le pide al DAO que los guarde, los lea, los modifique o los elimine
// Si algo está mal, lanza una IllegalArgumentException con un mensaje claro para el usuario.
public class ControladorPedidos {

    // Valor especial de los filtros de la ventana principal: significa "no filtrar"
    public static final String FILTRO_TODOS = "TODOS";

    // "final" significa que, una vez asignado en el constructor, no se puede reemplazar
    private final PedidoDAO pedidoDAO;

    public ControladorPedidos() {
        this.pedidoDAO = new PedidoDAO();
    }

    // Valida los datos del formulario y guarda un pedido nuevo.
    public void registrarPedido(String direccion, String tipo, EstadoPedido estado) throws IllegalArgumentException {
        validarDatos(direccion, tipo, estado);

        // El 0 es solo un valor de relleno: MySQL asigna el id real al guardar
        Pedido nuevoPedido = new Pedido(0, direccion, tipo);
        nuevoPedido.setEstado(estado);

        boolean exito = pedidoDAO.create(nuevoPedido);
        if (!exito) {
            throw new IllegalArgumentException("No se pudo guardar el pedido. Revisa que MySQL esté encendido.");
        }
    }

    // Valida los datos nuevos y los guarda en el pedido que tiene ese id.
    public void actualizarPedido(int id, String direccion, String tipo, EstadoPedido estado) throws IllegalArgumentException {
        validarDatos(direccion, tipo, estado);

        Pedido editado = new Pedido(id, direccion, tipo); // aquí el id SÍ importa: dice a quién modificar
        editado.setEstado(estado);

        boolean exito = pedidoDAO.update(editado);
        if (!exito) {
            throw new IllegalArgumentException("No se pudo actualizar el pedido. Revisa que todavía exista.");
        }
    }

    // Elimina el pedido que tiene ese id.
    public void eliminarPedido(int id) throws IllegalArgumentException {
        boolean exito = pedidoDAO.delete(id);
        if (!exito) {
            // La causa más común: el pedido tiene una entrega registrada y la llave foránea
            // de la tabla "entregas" no permite borrarlo.
            throw new IllegalArgumentException("No se pudo eliminar el pedido.\n"
                    + "Si tiene una entrega registrada, primero elimina esa entrega.");
        }
    }

    // Devuelve la lista de pedidos tal cual (la usan la ventana "Asignar Repartidor" y la de entregas)
    public List<Pedido> listarPedidos() {
        return pedidoDAO.readAll();
    }

    // Llena el modelo de la JTable con los pedidos, aplicando los filtros por estado y por tipo.
    // Si un filtro vale "TODOS", ese filtro no se aplica.
    public void cargarTabla(DefaultTableModel modelo, String filtroEstado, String filtroTipo) {
        modelo.setRowCount(0); // borra las filas anteriores para no duplicarlas

        for (Pedido p : listarPedidos()) {
            // Un filtro "se cumple" si vale TODOS o si coincide con el dato del pedido
            boolean cumpleEstado = filtroEstado.equals(FILTRO_TODOS) || p.getEstado().name().equals(filtroEstado);
            boolean cumpleTipo = filtroTipo.equals(FILTRO_TODOS) || p.getTipo().equals(filtroTipo);

            // Solo agregamos a la tabla los pedidos que cumplen los dos filtros
            if (cumpleEstado && cumpleTipo) {
                // Si el pedido aún no tiene repartidor, mostramos un guion en vez de "null"
                String repartidor = (p.getNombreRepartidor() == null) ? "-" : p.getNombreRepartidor();

                // Cada fila es un arreglo de objetos, en el mismo orden que las columnas:
                // ID, Dirección, Tipo, Estado, Repartidor
                modelo.addRow(new Object[]{
                        p.getId(),
                        p.getDireccionEntrega(),
                        p.getTipo(),
                        p.getEstado(),
                        repartidor
                });
            }
        }
    }

    // Cambia solo el estado de un pedido (lo usa el botón "Marcar como Entregado")
    public void actualizarEstado(int idPedido, EstadoPedido nuevoEstado) throws IllegalArgumentException {
        boolean exito = pedidoDAO.actualizarEstado(idPedido, nuevoEstado);
        if (!exito) {
            throw new IllegalArgumentException("No se pudo actualizar el estado del pedido.");
        }
    }

    // Validación reutilizable: la usan registrar y actualizar, así no repetimos el mismo código.
    private void validarDatos(String direccion, String tipo, EstadoPedido estado) throws IllegalArgumentException {
        if (direccion == null || direccion.isEmpty()) {
            throw new IllegalArgumentException("Por favor, ingrese la dirección de entrega.");
        }
        // 100 es el largo máximo de la columna "direccion" (VARCHAR(100)) en la base de datos
        if (direccion.length() > 100) {
            throw new IllegalArgumentException("La dirección no puede superar los 100 caracteres.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Por favor, seleccione el tipo de pedido.");
        }
        if (estado == null) {
            throw new IllegalArgumentException("Por favor, seleccione el estado del pedido.");
        }
    }
}
