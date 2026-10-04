package semana8.vista;

import semana8.controlador.ControladorEntregas;
import semana8.controlador.ControladorPedidos;
import semana8.controlador.ControladorRepartidores;
import semana8.modelo.EstadoPedido;
import semana8.modelo.Pedido;
import semana8.modelo.Repartidor;

import javax.swing.*;
import java.awt.*;
import java.util.List;

// VISTA: ventana para asignar un repartidor a un pedido.
// Es la única ventana que usa los TRES controladores
public class VentanaAsignarEntrega extends JFrame {

    // JComboBox<Pedido> guarda objetos Pedido completos (no solo texto).
    private JComboBox<Pedido> cbxPedidos;
    private JComboBox<Repartidor> cbxRepartidores;
    private JButton btnConfirmar;
    private JButton btnCancelar;

    private final ControladorEntregas controladorEntregas;

    public VentanaAsignarEntrega(ControladorPedidos controladorPedidos,
                                 ControladorRepartidores controladorRepartidores,
                                 ControladorEntregas controladorEntregas) {
        this.controladorEntregas = controladorEntregas;

        setTitle("Asignar Repartidor a Pedido");
        setSize(420, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Consultamos la base de datos justo al abrir la ventana, para tener datos actualizados.
        // Solo dejamos los pedidos PENDIENTES: no tiene sentido asignar uno que ya va en camino
        // o que ya se entregó. removeIf(...) borra de la lista los que cumplen la condición.
        List<Pedido> pedidos = controladorPedidos.listarPedidos();
        pedidos.removeIf(p -> p.getEstado() != EstadoPedido.PENDIENTE);
        List<Repartidor> repartidores = controladorRepartidores.listarRepartidores();

        // toArray(...) convierte la List en un arreglo, que es lo que pide el JComboBox
        panel.add(new JLabel("Pedido pendiente:"));
        cbxPedidos = new JComboBox<>(pedidos.toArray(new Pedido[0]));
        panel.add(cbxPedidos);

        panel.add(new JLabel("Repartidor:"));
        cbxRepartidores = new JComboBox<>(repartidores.toArray(new Repartidor[0]));
        panel.add(cbxRepartidores);

        btnConfirmar = new JButton("Confirmar Asignación");
        btnCancelar = new JButton("Cancelar");
        panel.add(btnConfirmar);
        panel.add(btnCancelar);

        add(panel);

        btnConfirmar.addActionListener(e -> confirmarAsignacion());
        btnCancelar.addActionListener(e -> dispose());
    }

    // Se ejecuta al presionar "Confirmar Asignación"
    private void confirmarAsignacion() {
        // getSelectedItem() devuelve un Object, por eso lo convertimos (cast) a Pedido / Repartidor
        Pedido pedidoSeleccionado = (Pedido) cbxPedidos.getSelectedItem();
        Repartidor repartidorSeleccionado = (Repartidor) cbxRepartidores.getSelectedItem();

        // Si una lista está vacía, getSelectedItem() devuelve null.
        // Damos un mensaje distinto para cada caso, para que el usuario sepa qué le falta.
        if (pedidoSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "No hay pedidos PENDIENTES. Registra un pedido nuevo primero.",
                    "Sin pedidos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (repartidorSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "No hay repartidores registrados. Registra un repartidor primero.",
                    "Sin repartidores",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Guarda la entrega y cambia el pedido a EN_REPARTO
            controladorEntregas.registrarEntrega(pedidoSeleccionado.getId(), repartidorSeleccionado.getId());

            JOptionPane.showMessageDialog(this,
                    repartidorSeleccionado.getNombre() + " quedó a cargo del " + pedidoSeleccionado + ".",
                    "Asignación Confirmada",
                    JOptionPane.INFORMATION_MESSAGE);

            // Al cerrar, VentanaPrincipal se entera y refresca su tabla automáticamente
            dispose();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}
