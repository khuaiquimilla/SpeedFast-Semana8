package semana8.controlador;

import semana8.dao.EntregaDAO;
import semana8.dao.PedidoDAO;
import semana8.modelo.Entrega;
import semana8.modelo.EstadoPedido;
import semana8.modelo.Pedido;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

// CONTROLADOR de entregas: coordina todo lo que pasa con las entregas.
// Necesita DOS DAOs porque una entrega también afecta al pedido:
//   - Al registrar una entrega, el pedido pasa de PENDIENTE a EN_REPARTO.
//   - Al eliminarla, el pedido vuelve de EN_REPARTO a PENDIENTE (para poder asignarlo de nuevo).
// Si algo está mal, lanza una IllegalArgumentException con un mensaje claro para el usuario.
public class ControladorEntregas {

    private final EntregaDAO entregaDAO;
    private final PedidoDAO pedidoDAO;

    public ControladorEntregas() {
        this.entregaDAO = new EntregaDAO();
        this.pedidoDAO = new PedidoDAO();
    }

    // Registra una entrega con la fecha y hora de ESTE momento.
    // Lo usa la ventana "Asignar Repartidor" (la asignación rápida).
    public void registrarEntrega(int idPedido, int idRepartidor) throws IllegalArgumentException {
        // withNano(0) quita las fracciones de segundo, que MySQL no guarda en una columna TIME
        registrar(idPedido, idRepartidor, LocalDate.now(), LocalTime.now().withNano(0));
    }

    // Registra una entrega con la fecha y hora que escribió el usuario.
    // Lo usa la ventana "Gestionar Entregas".
    public void registrarEntrega(int idPedido, int idRepartidor, String fechaTexto, String horaTexto)
            throws IllegalArgumentException {
        LocalDate fecha = convertirFecha(fechaTexto);
        LocalTime hora = convertirHora(horaTexto);
        registrar(idPedido, idRepartidor, fecha, hora);
    }

    // Lógica común de los dos métodos anteriores (así no la repetimos).
    private void registrar(int idPedido, int idRepartidor, LocalDate fecha, LocalTime hora) {
        validarIds(idPedido, idRepartidor);
        validarPedidoSinOtraEntrega(idPedido, 0); // 0 = no hay una entrega propia que excluir (es nueva)

        boolean exito = entregaDAO.create(new Entrega(idPedido, idRepartidor, fecha, hora));
        if (!exito) {
            throw new IllegalArgumentException("No se pudo registrar la entrega. Revisa que MySQL esté encendido.");
        }

        // El pedido ya tiene repartidor: si estaba PENDIENTE, pasa a EN_REPARTO
        marcarEnRepartoSiPendiente(idPedido);
    }

    // Valida los datos nuevos y los guarda en la entrega que tiene ese id.
    public void actualizarEntrega(int id, int idPedido, int idRepartidor, String fechaTexto, String horaTexto)
            throws IllegalArgumentException {
        LocalDate fecha = convertirFecha(fechaTexto);
        LocalTime hora = convertirHora(horaTexto);
        validarIds(idPedido, idRepartidor);
        validarPedidoSinOtraEntrega(idPedido, id); // excluimos esta misma entrega de la revisión

        // Guardamos cómo estaba antes, para saber si el usuario cambió el pedido
        Entrega anterior = buscarEntrega(id);
        if (anterior == null) {
            throw new IllegalArgumentException("Esa entrega ya no existe. Presiona \"Refrescar\".");
        }

        boolean exito = entregaDAO.update(new Entrega(id, idPedido, idRepartidor, fecha, hora));
        if (!exito) {
            throw new IllegalArgumentException("No se pudo actualizar la entrega. Revisa que MySQL esté encendido.");
        }

        // Si se cambió el pedido: el pedido anterior se queda sin entrega (vuelve a PENDIENTE)
        // y el pedido nuevo pasa a EN_REPARTO.
        if (anterior.getIdPedido() != idPedido) {
            volverAPendienteSiEnReparto(anterior.getIdPedido());
            marcarEnRepartoSiPendiente(idPedido);
        }
    }

    // Elimina la entrega que tiene ese id.
    public void eliminarEntrega(int id) throws IllegalArgumentException {
        Entrega entrega = buscarEntrega(id);
        if (entrega == null) {
            throw new IllegalArgumentException("Esa entrega ya no existe. Presiona \"Refrescar\".");
        }

        boolean exito = entregaDAO.delete(id);
        if (!exito) {
            throw new IllegalArgumentException("No se pudo eliminar la entrega. Revisa que MySQL esté encendido.");
        }

        // El pedido se quedó sin repartidor: si iba EN_REPARTO, vuelve a PENDIENTE
        volverAPendienteSiEnReparto(entrega.getIdPedido());
    }

    // Devuelve las entregas, filtradas por pedido y/o por repartidor.
    // Un filtro con valor 0 significa "no filtrar" (los ids de MySQL parten en 1).
    public List<Entrega> listarEntregas(int idPedidoFiltro, int idRepartidorFiltro) {
        List<Entrega> resultado = new ArrayList<>();

        for (Entrega e : entregaDAO.readAll()) {
            // Un filtro "se cumple" si vale 0 (sin filtro) o si coincide con el id de la entrega
            boolean cumplePedido = idPedidoFiltro == 0 || e.getIdPedido() == idPedidoFiltro;
            boolean cumpleRepartidor = idRepartidorFiltro == 0 || e.getIdRepartidor() == idRepartidorFiltro;

            if (cumplePedido && cumpleRepartidor) {
                resultado.add(e);
            }
        }
        return resultado;
    }

    // ---------------- Validaciones y métodos de apoyo (privados) ----------------

    // Convierte el texto de la fecha a LocalDate. Formato esperado: aaaa-mm-dd (ej: 2026-10-04)
    private LocalDate convertirFecha(String texto) {
        if (texto == null || texto.isEmpty()) {
            throw new IllegalArgumentException("Por favor, ingrese la fecha de la entrega.");
        }
        try {
            return LocalDate.parse(texto); // si el texto no es una fecha válida, lanza DateTimeParseException
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("La fecha \"" + texto + "\" no es válida.\n"
                    + "Usa el formato aaaa-mm-dd (ejemplo: 2026-10-04).");
        }
    }

    // Convierte el texto de la hora a LocalTime. Formato esperado: HH:mm (ej: 09:30)
    private LocalTime convertirHora(String texto) {
        if (texto == null || texto.isEmpty()) {
            throw new IllegalArgumentException("Por favor, ingrese la hora de la entrega.");
        }
        try {
            return LocalTime.parse(texto);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("La hora \"" + texto + "\" no es válida.\n"
                    + "Usa el formato HH:mm de 24 horas (ejemplo: 09:30 o 18:45).");
        }
    }

    // Revisa que se haya elegido un pedido y un repartidor en los combos
    private void validarIds(int idPedido, int idRepartidor) {
        if (idPedido <= 0) {
            throw new IllegalArgumentException("Por favor, seleccione un pedido.");
        }
        if (idRepartidor <= 0) {
            throw new IllegalArgumentException("Por favor, seleccione un repartidor.");
        }
    }

    // Regla de negocio: cada pedido tiene como máximo UNA entrega.
    // idEntregaActual permite excluir la propia entrega cuando se está editando.
    private void validarPedidoSinOtraEntrega(int idPedido, int idEntregaActual) {
        for (Entrega e : entregaDAO.readAll()) {
            if (e.getIdPedido() == idPedido && e.getId() != idEntregaActual) {
                throw new IllegalArgumentException("El pedido #" + idPedido + " ya tiene una entrega registrada.\n"
                        + "Si quieres cambiarla, edita esa entrega en vez de crear otra.");
            }
        }
    }

    // Busca una entrega por su id (devuelve null si no existe)
    private Entrega buscarEntrega(int id) {
        for (Entrega e : entregaDAO.readAll()) {
            if (e.getId() == id) {
                return e;
            }
        }
        return null;
    }

    // Busca un pedido por su id (devuelve null si no existe)
    private Pedido buscarPedido(int id) {
        for (Pedido p : pedidoDAO.readAll()) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    private void marcarEnRepartoSiPendiente(int idPedido) {
        Pedido pedido = buscarPedido(idPedido);
        if (pedido != null && pedido.getEstado() == EstadoPedido.PENDIENTE) {
            pedidoDAO.actualizarEstado(idPedido, EstadoPedido.EN_REPARTO);
        }
    }

    private void volverAPendienteSiEnReparto(int idPedido) {
        Pedido pedido = buscarPedido(idPedido);
        if (pedido != null && pedido.getEstado() == EstadoPedido.EN_REPARTO) {
            pedidoDAO.actualizarEstado(idPedido, EstadoPedido.PENDIENTE);
        }
    }
}
