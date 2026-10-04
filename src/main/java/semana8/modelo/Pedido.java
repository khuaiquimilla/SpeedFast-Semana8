package semana8.modelo;

// Clase del MODELO: representa un pedido de SpeedFast.
public class Pedido {

    // Atributos: coinciden con las columnas de la tabla "pedidos"
    private int id;                  // lo genera MySQL (AUTO_INCREMENT)
    private String direccionEntrega; // columna "direccion"
    private String tipo;             // COMIDA, ENCOMIENDA o EXPRESS
    private EstadoPedido estado;     // PENDIENTE, EN_REPARTO o ENTREGADO

    // Este atributo NO es una columna de la tabla "pedidos".
    private String nombreRepartidor;

    // Constructor: todo pedido nuevo nace en estado PENDIENTE
    public Pedido(int id, String direccionEntrega, String tipo) {
        this.id = id;
        this.direccionEntrega = direccionEntrega;
        this.tipo = tipo;
        this.estado = EstadoPedido.PENDIENTE;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public String getTipo() {
        return tipo;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public String getNombreRepartidor() {
        return nombreRepartidor;
    }

    // Setters
    public void setEstado(EstadoPedido nuevoEstado) {
        this.estado = nuevoEstado;
    }

    public void setNombreRepartidor(String nombreRepartidor) {
        this.nombreRepartidor = nombreRepartidor;
    }

    // toString()
    @Override
    public String toString() {
        return "Pedido #" + id + " - " + direccionEntrega;
    }
}
