package semana8.vista;

import semana8.controlador.ControladorRepartidores;
import semana8.modelo.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// VISTA: ventana para gestionar repartidores (CRUD completo).
// Arriba hay un formulario con el nombre y los botones; abajo, una tabla con todos los repartidores.
// Al hacer clic en una fila, su nombre se carga en el formulario para poder editarlo o eliminarlo.
public class VentanaRepartidores extends JFrame {

    private final ControladorRepartidores controlador;

    private JTextField txtNombre;
    private JTable tblRepartidores;
    private DefaultTableModel modeloTabla;

    // Guarda el id del repartidor seleccionado en la tabla. -1 significa "ninguno seleccionado".
    private int idSeleccionado = -1;

    public VentanaRepartidores(ControladorRepartidores controlador) {
        this.controlador = controlador;

        setTitle("Gestión de Repartidores");
        setSize(520, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // cierra solo esta ventana
        setLocationRelativeTo(null);

        // --- Formulario (arriba) ---
        JPanel panelFormulario = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelFormulario.add(new JLabel("Nombre:"));
        txtNombre = new JTextField(20);
        panelFormulario.add(txtNombre);

        JButton btnAgregar = new JButton("Agregar");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        // Un panel que agrupa el formulario y los botones, uno debajo del otro
        JPanel panelSuperior = new JPanel(new GridLayout(2, 1));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panelSuperior.add(panelFormulario);
        panelSuperior.add(panelBotones);

        // --- Tabla (centro) ---
        String[] columnas = {"ID", "Nombre"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // el usuario no puede escribir dentro de las celdas
            }
        };
        tblRepartidores = new JTable(modeloTabla);
        tblRepartidores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblRepartidores.setRowHeight(24);
        tblRepartidores.getColumnModel().getColumn(0).setMaxWidth(60);

        // Cuando el usuario selecciona una fila, cargamos sus datos en el formulario
        tblRepartidores.getSelectionModel().addListSelectionListener(e -> cargarSeleccion());

        add(panelSuperior, BorderLayout.NORTH);
        add(new JScrollPane(tblRepartidores), BorderLayout.CENTER);

        // --- Eventos de los botones ---
        btnAgregar.addActionListener(e -> agregar());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        cargarTabla(); // mostramos los repartidores apenas se abre la ventana
    }

    // Vuelve a pedir los repartidores a la base de datos y los dibuja en la tabla
    private void cargarTabla() {
        modeloTabla.setRowCount(0); // borra las filas anteriores para no duplicarlas
        for (Repartidor r : controlador.listarRepartidores()) {
            modeloTabla.addRow(new Object[]{r.getId(), r.getNombre()});
        }
    }

    // Copia los datos de la fila seleccionada al formulario y recuerda su id
    private void cargarSeleccion() {
        int fila = tblRepartidores.getSelectedRow(); // -1 si no hay ninguna fila seleccionada
        if (fila == -1) {
            return;
        }
        idSeleccionado = (Integer) modeloTabla.getValueAt(fila, 0);
        txtNombre.setText((String) modeloTabla.getValueAt(fila, 1));
    }

    // Deja el formulario vacío y sin ningún repartidor seleccionado
    private void limpiarFormulario() {
        txtNombre.setText("");
        idSeleccionado = -1;
        tblRepartidores.clearSelection();
        txtNombre.requestFocus();
    }

    private void agregar() {
        try {
            controlador.registrarRepartidor(txtNombre.getText().trim());
            JOptionPane.showMessageDialog(this, "Repartidor agregado correctamente.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarTabla();
            limpiarFormulario();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void editar() {
        // Validación: primero hay que elegir a quién editar
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un repartidor de la tabla para editarlo.",
                    "Ningún repartidor seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            controlador.actualizarRepartidor(idSeleccionado, txtNombre.getText().trim());
            JOptionPane.showMessageDialog(this, "Repartidor actualizado correctamente.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarTabla();
            limpiarFormulario();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminar() {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un repartidor de la tabla para eliminarlo.",
                    "Ningún repartidor seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Pedimos confirmación porque borrar no se puede deshacer
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Seguro que quieres eliminar a " + txtNombre.getText() + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controlador.eliminarRepartidor(idSeleccionado);
            JOptionPane.showMessageDialog(this, "Repartidor eliminado correctamente.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarTabla();
            limpiarFormulario();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
        }
    }
}
