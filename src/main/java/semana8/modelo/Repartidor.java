package semana8.modelo;

// Clase del MODELO: representa a un repartidor de SpeedFast.
// Cada objeto Repartidor corresponde a una fila de la tabla "repartidores" en MySQL.
public class Repartidor {

    // Atributos: coinciden con las columnas de la tabla "repartidores"
    private int id;        // lo genera MySQL (AUTO_INCREMENT)
    private String nombre;

    // Constructor: se usa tanto para crear un repartidor nuevo (con id 0, de relleno)
    // como para reconstruir uno leído desde la base de datos (con su id real)
    public Repartidor(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    // Sin este método, el JComboBox de repartidores mostraría algo como "Repartidor@1a2b3c".
    // Con él, muestra "id - nombre" (ej: "3 - Juan Pérez"), como pide la pauta.
    // El combo guarda el objeto completo, así que internamente seguimos teniendo el id.
    @Override
    public String toString() {
        return id + " - " + nombre;
    }
}
