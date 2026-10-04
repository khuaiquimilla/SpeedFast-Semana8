package semana8.modelo;

import java.time.LocalDate;
import java.time.LocalTime;

// Clase del MODELO: representa una fila de la tabla "entregas".
// Una entrega es el registro de que un repartidor quedó a cargo de un pedido, en una fecha y hora.
public class Entrega {

    private int id;           // lo genera MySQL (AUTO_INCREMENT)
    private int idPedido;     // llave foránea -> pedidos(id)
    private int idRepartidor; // llave foránea -> repartidores(id)
    private LocalDate fecha;  // ej: 2026-10-04
    private LocalTime hora;   // ej: 20:15

    // Estos dos atributos NO son columnas de la tabla "entregas".
    // Se llenan con un JOIN al leer, para mostrar en la tabla la dirección y el nombre
    // en vez de solo los números de id.
    private String direccionPedido;
    private String nombreRepartidor;

    // Constructor para una entrega NUEVA: todavía no tiene id porque MySQL lo asigna al guardar.
    public Entrega(int idPedido, int idRepartidor, LocalDate fecha, LocalTime hora) {
        this.id = 0; // el 0 es relleno: MySQL asigna el id real al guardar
        this.idPedido = idPedido;
        this.idRepartidor = idRepartidor;
        this.fecha = fecha;
        this.hora = hora;
    }

    // Constructor para una entrega que YA existe en la base de datos (al leerla o al editarla).
    public Entrega(int id, int idPedido, int idRepartidor, LocalDate fecha, LocalTime hora) {
        this.id = id;
        this.idPedido = idPedido;
        this.idRepartidor = idRepartidor;
        this.fecha = fecha;
        this.hora = hora;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public int getIdRepartidor() {
        return idRepartidor;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public String getDireccionPedido() {
        return direccionPedido;
    }

    public String getNombreRepartidor() {
        return nombreRepartidor;
    }

    // Setters (solo de los datos "de apoyo" que vienen del JOIN)
    public void setDireccionPedido(String direccionPedido) {
        this.direccionPedido = direccionPedido;
    }

    public void setNombreRepartidor(String nombreRepartidor) {
        this.nombreRepartidor = nombreRepartidor;
    }

    @Override
    public String toString() {
        return "Entrega (Pedido #" + idPedido + " -> Repartidor #" + idRepartidor + ") " + fecha + " " + hora;
    }
}
