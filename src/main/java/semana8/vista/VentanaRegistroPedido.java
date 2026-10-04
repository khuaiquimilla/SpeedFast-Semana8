package semana8.vista;

import semana8.controlador.ControladorPedidos;
import semana8.modelo.EstadoPedido;
import semana8.modelo.Pedido;

import javax.swing.*;
import java.awt.*;

// VISTA: formulario de pedidos. Sirve para DOS cosas (así reutilizamos la misma ventana):
//  - Registrar un pedido nuevo   -> se abre con new VentanaRegistroPedido(controlador, null)
//  - Editar un pedido existente  -> se abre con new VentanaRegistroPedido(controlador, pedido)
// El ID siempre lo genera MySQL, por eso no aparece en el formulario.
public class VentanaRegistroPedido extends JFrame {

    // Componentes del formulario
    private JTextField txtDireccion;
    private JComboBox<String> cbxTipo;
    private JComboBox<EstadoPedido> cbxEstado;
    private JButton btnGuardar;
    private JButton btnCancelar;

    private final ControladorPedidos controlador;

    // El pedido que se está editando. Si es null, la ventana está en modo "registrar nuevo".
    private final Pedido pedidoEditando;

    // Si pedidoEditando es null, la ventana registra un pedido nuevo.
    // Si trae un pedido, la ventana lo edita y rellena el formulario con sus datos.
    public VentanaRegistroPedido(ControladorPedidos controlador, Pedido pedidoEditando) {
        this.controlador = controlador;
        this.pedidoEditando = pedidoEditando;

        // DISPOSE_ON_CLOSE cierra solo esta ventana, no toda la aplicación
        setTitle(pedidoEditando == null ? "Registrar Nuevo Pedido" : "Editar Pedido #" + pedidoEditando.getId());
        setSize(360, 230);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // GridLayout(4, 2) = una grilla de 4 filas y 2 columnas (etiqueta | campo)
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Fila 1: dirección
        panel.add(new JLabel("Dirección de Entrega:"));
        txtDireccion = new JTextField();
        panel.add(txtDireccion);

        // Fila 2: tipo. Usamos un JComboBox (lista desplegable) en vez de un campo de texto
        // para que el usuario solo pueda elegir valores válidos.
        panel.add(new JLabel("Tipo de Pedido:"));
        String[] tipos = {"COMIDA", "ENCOMIENDA", "EXPRESS"};
        cbxTipo = new JComboBox<>(tipos);
        panel.add(cbxTipo);

        // Fila 3: estado. El combo guarda directamente los valores del enum EstadoPedido.
        panel.add(new JLabel("Estado:"));
        EstadoPedido[] estados = {EstadoPedido.PENDIENTE, EstadoPedido.EN_REPARTO, EstadoPedido.ENTREGADO};
        cbxEstado = new JComboBox<>(estados);
        panel.add(cbxEstado);

        // Fila 4: botones
        btnGuardar = new JButton("Guardar");
        btnCancelar = new JButton("Cancelar");
        panel.add(btnGuardar);
        panel.add(btnCancelar);

        add(panel);

        // Si estamos editando, mostramos los datos actuales del pedido en el formulario
        if (pedidoEditando != null) {
            txtDireccion.setText(pedidoEditando.getDireccionEntrega());
            cbxTipo.setSelectedItem(pedidoEditando.getTipo());
            cbxEstado.setSelectedItem(pedidoEditando.getEstado());
        }

        // Eventos
        btnGuardar.addActionListener(e -> guardarPedido());
        btnCancelar.addActionListener(e -> dispose()); // dispose() cierra esta ventana

        // Permite guardar presionando Enter, sin tener que hacer clic en el botón
        getRootPane().setDefaultButton(btnGuardar);
    }

    // Toma los datos del formulario y se los pasa al controlador
    private void guardarPedido() {
        String direccion = txtDireccion.getText().trim(); // trim() quita espacios al inicio y al final
        String tipo = (String) cbxTipo.getSelectedItem();
        EstadoPedido estado = (EstadoPedido) cbxEstado.getSelectedItem();

        try {
            if (pedidoEditando == null) {
                // Modo REGISTRAR: el controlador valida y, si todo está bien, guarda en MySQL.
                controlador.registrarPedido(direccion, tipo, estado);

                JOptionPane.showMessageDialog(this, "Pedido registrado correctamente.",
                        "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);

                // Limpiamos el formulario para poder registrar otro pedido de inmediato
                txtDireccion.setText("");
                cbxTipo.setSelectedIndex(0);
                cbxEstado.setSelectedItem(EstadoPedido.PENDIENTE);
                txtDireccion.requestFocus();

            } else {
                // Modo EDITAR: guardamos los cambios en el pedido que estamos editando
                controlador.actualizarPedido(pedidoEditando.getId(), direccion, tipo, estado);

                JOptionPane.showMessageDialog(this, "Pedido actualizado correctamente.",
                        "Edición Exitosa", JOptionPane.INFORMATION_MESSAGE);

                dispose(); // cerramos: la ventana principal refresca su tabla al detectar el cierre
            }

        } catch (IllegalArgumentException ex) {
            // Mostramos el mensaje exacto que armó el controlador
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error de validación", JOptionPane.WARNING_MESSAGE);
        }
    }
}
