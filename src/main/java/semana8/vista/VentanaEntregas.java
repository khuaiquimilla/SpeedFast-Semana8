package semana8.vista;

import semana8.controlador.ControladorEntregas;
import semana8.controlador.ControladorPedidos;
import semana8.controlador.ControladorRepartidores;
import semana8.modelo.Entrega;
import semana8.modelo.Pedido;
import semana8.modelo.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// VISTA: ventana para gestionar entregas (CRUD completo).
// Arriba: formulario (pedido, repartidor, fecha, hora), botones y filtros.
// Abajo: tabla con las entregas. Al hacer clic en una fila, sus datos se cargan en el formulario.
public class VentanaEntregas extends JFrame {

    // Primera opción de los combos de filtro: significa "no filtrar"
    private static final String TODOS = "TODOS";

    private final ControladorEntregas controladorEntregas;
    private final ControladorPedidos controladorPedidos;
    private final ControladorRepartidores controladorRepartidores;

    // Combos del formulario: guardan objetos completos (Pedido, Repartidor).
    // Muestran el texto de su toString() ("Pedido #3 - Av. X", "2 - Juan"), pero internamente
    // conservamos el objeto, y de él sacamos el id con getId().
    private JComboBox<Pedido> cbxPedido;
    private JComboBox<Repartidor> cbxRepartidor;
    private JTextField txtFecha;
    private JTextField txtHora;

    // Combos de filtro: muestran "TODOS" y luego el texto de cada pedido / repartidor
    private JComboBox<String> cbxFiltroPedido;
    private JComboBox<String> cbxFiltroRepartidor;

    // Listas con los pedidos y repartidores cargados en los combos de filtro.
    // La opción 1 del combo es el elemento 0 de la lista (la opción 0 es "TODOS").
    private List<Pedido> pedidosCargados = new ArrayList<>();
    private List<Repartidor> repartidoresCargados = new ArrayList<>();

    private JTable tblEntregas;
    private DefaultTableModel modeloTabla;

    // Las entregas que se muestran en la tabla, en el MISMO orden que las filas.
    // Así, la fila 0 de la tabla corresponde a entregasEnTabla.get(0), y así sucesivamente.
    private List<Entrega> entregasEnTabla = new ArrayList<>();

    // Id de la entrega seleccionada en la tabla. -1 significa "ninguna seleccionada".
    private int idSeleccionado = -1;

    // Mientras recargamos los combos, los filtros "cambian solos": con esta bandera
    // evitamos que eso dispare una recarga de la tabla a medias.
    private boolean cargandoCombos = false;

    public VentanaEntregas(ControladorEntregas controladorEntregas,
                           ControladorPedidos controladorPedidos,
                           ControladorRepartidores controladorRepartidores) {
        this.controladorEntregas = controladorEntregas;
        this.controladorPedidos = controladorPedidos;
        this.controladorRepartidores = controladorRepartidores;

        setTitle("Gestión de Entregas");
        setSize(820, 540);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // cierra solo esta ventana
        setLocationRelativeTo(null);

        // --- Formulario: grilla de 2 filas x 4 columnas (etiqueta | campo | etiqueta | campo) ---
        cbxPedido = new JComboBox<>();
        cbxRepartidor = new JComboBox<>();
        txtFecha = new JTextField();
        txtHora = new JTextField();

        JPanel panelFormulario = new JPanel(new GridLayout(2, 4, 8, 8));
        panelFormulario.add(new JLabel("Pedido:"));
        panelFormulario.add(cbxPedido);
        panelFormulario.add(new JLabel("Repartidor:"));
        panelFormulario.add(cbxRepartidor);
        panelFormulario.add(new JLabel("Fecha (aaaa-mm-dd):"));
        panelFormulario.add(txtFecha);
        panelFormulario.add(new JLabel("Hora (HH:mm):"));
        panelFormulario.add(txtHora);

        // --- Botones ---
        JButton btnRegistrar = new JButton("Registrar");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnRefrescar = new JButton("Refrescar");

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        panelBotones.add(btnRegistrar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnRefrescar);

        // --- Filtros de la tabla: por pedido o por repartidor ---
        cbxFiltroPedido = new JComboBox<>();
        cbxFiltroRepartidor = new JComboBox<>();
        // Ancho fijo, para que los combos no queden angostos cuando se recargan sus opciones
        cbxFiltroPedido.setPreferredSize(new Dimension(240, 26));
        cbxFiltroRepartidor.setPreferredSize(new Dimension(180, 26));

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        panelFiltros.add(new JLabel("Filtrar por pedido:"));
        panelFiltros.add(cbxFiltroPedido);
        panelFiltros.add(new JLabel("por repartidor:"));
        panelFiltros.add(cbxFiltroRepartidor);

        // Formulario, botones y filtros, uno debajo del otro
        JPanel panelSuperior = new JPanel(new BorderLayout(0, 6));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));
        panelSuperior.add(panelFormulario, BorderLayout.NORTH);
        panelSuperior.add(panelBotones, BorderLayout.CENTER);
        panelSuperior.add(panelFiltros, BorderLayout.SOUTH);

        // --- Tabla de entregas ---
        String[] columnas = {"ID", "Pedido", "Repartidor", "Fecha", "Hora"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // el usuario no puede escribir dentro de las celdas
            }
        };
        tblEntregas = new JTable(modeloTabla);
        tblEntregas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblEntregas.setRowHeight(24);
        tblEntregas.getColumnModel().getColumn(0).setMaxWidth(60);
        tblEntregas.getColumnModel().getColumn(1).setPreferredWidth(300);
        tblEntregas.getColumnModel().getColumn(2).setPreferredWidth(180);

        // Cuando el usuario selecciona una fila, cargamos esa entrega en el formulario
        tblEntregas.getSelectionModel().addListSelectionListener(e -> cargarSeleccion());

        add(panelSuperior, BorderLayout.NORTH);
        add(new JScrollPane(tblEntregas), BorderLayout.CENTER);

        // --- Eventos ---
        btnRegistrar.addActionListener(e -> registrar());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnRefrescar.addActionListener(e -> refrescarTodo());

        // Al cambiar un filtro, recargamos la tabla (salvo que estemos recargando los combos)
        cbxFiltroPedido.addActionListener(e -> {
            if (!cargandoCombos) {
                cargarTabla();
            }
        });
        cbxFiltroRepartidor.addActionListener(e -> {
            if (!cargandoCombos) {
                cargarTabla();
            }
        });

        refrescarTodo(); // cargamos combos y tabla apenas se abre la ventana
    }

    // Recarga TODO desde la base de datos: combos, tabla y formulario.
    // Así los combos siempre reflejan los pedidos y repartidores que existen en este momento.
    private void refrescarTodo() {
        cargarCombos();
        cargarTabla();
        limpiarFormulario();
    }

    // Llena los combos del formulario y de los filtros con los pedidos y repartidores de la BD
    private void cargarCombos() {
        cargandoCombos = true;

        pedidosCargados = controladorPedidos.listarPedidos();
        repartidoresCargados = controladorRepartidores.listarRepartidores();

        cbxPedido.removeAllItems();
        cbxFiltroPedido.removeAllItems();
        cbxFiltroPedido.addItem(TODOS);
        for (Pedido p : pedidosCargados) {
            cbxPedido.addItem(p);
            cbxFiltroPedido.addItem(p.toString());
        }

        cbxRepartidor.removeAllItems();
        cbxFiltroRepartidor.removeAllItems();
        cbxFiltroRepartidor.addItem(TODOS);
        for (Repartidor r : repartidoresCargados) {
            cbxRepartidor.addItem(r);
            cbxFiltroRepartidor.addItem(r.toString());
        }

        cargandoCombos = false;
    }

    // Pide las entregas al controlador (con los filtros elegidos) y las dibuja en la tabla
    private void cargarTabla() {
        // Si en el filtro está elegida la opción 0 ("TODOS"), usamos 0 = sin filtro.
        // Si no, la opción N del combo corresponde al elemento N-1 de la lista, y de él sacamos el id.
        int indicePedido = cbxFiltroPedido.getSelectedIndex();
        int idPedidoFiltro = (indicePedido <= 0) ? 0 : pedidosCargados.get(indicePedido - 1).getId();

        int indiceRepartidor = cbxFiltroRepartidor.getSelectedIndex();
        int idRepartidorFiltro = (indiceRepartidor <= 0) ? 0 : repartidoresCargados.get(indiceRepartidor - 1).getId();

        modeloTabla.setRowCount(0); // borra las filas anteriores
        entregasEnTabla = controladorEntregas.listarEntregas(idPedidoFiltro, idRepartidorFiltro);

        for (Entrega ent : entregasEnTabla) {
            modeloTabla.addRow(new Object[]{
                    ent.getId(),
                    "#" + ent.getIdPedido() + " - " + ent.getDireccionPedido(),
                    ent.getNombreRepartidor(),
                    ent.getFecha(),
                    ent.getHora()
            });
        }
        idSeleccionado = -1; // al recargar la tabla se pierde la selección
    }

    // Copia los datos de la entrega seleccionada al formulario
    private void cargarSeleccion() {
        int fila = tblEntregas.getSelectedRow();
        if (fila == -1 || fila >= entregasEnTabla.size()) {
            return; // no hay fila seleccionada
        }

        Entrega ent = entregasEnTabla.get(fila);
        idSeleccionado = ent.getId();
        seleccionarPedidoEnCombo(ent.getIdPedido());
        seleccionarRepartidorEnCombo(ent.getIdRepartidor());
        txtFecha.setText(ent.getFecha().toString());
        txtHora.setText(ent.getHora().toString());
    }

    // Busca en el combo el pedido con ese id y lo deja seleccionado
    private void seleccionarPedidoEnCombo(int idPedido) {
        for (int i = 0; i < cbxPedido.getItemCount(); i++) {
            if (cbxPedido.getItemAt(i).getId() == idPedido) {
                cbxPedido.setSelectedIndex(i);
                return;
            }
        }
    }

    // Busca en el combo el repartidor con ese id y lo deja seleccionado
    private void seleccionarRepartidorEnCombo(int idRepartidor) {
        for (int i = 0; i < cbxRepartidor.getItemCount(); i++) {
            if (cbxRepartidor.getItemAt(i).getId() == idRepartidor) {
                cbxRepartidor.setSelectedIndex(i);
                return;
            }
        }
    }

    // Deja el formulario listo para una entrega nueva, con la fecha y hora de este momento
    private void limpiarFormulario() {
        idSeleccionado = -1;
        tblEntregas.clearSelection();
        txtFecha.setText(LocalDate.now().toString());                      // ej: 2026-10-04
        txtHora.setText(LocalTime.now().withSecond(0).withNano(0).toString()); // ej: 16:45
    }

    private void registrar() {
        Pedido pedido = (Pedido) cbxPedido.getSelectedItem();
        Repartidor repartidor = (Repartidor) cbxRepartidor.getSelectedItem();

        // Si un combo está vacío, getSelectedItem() devuelve null
        if (pedido == null || repartidor == null) {
            JOptionPane.showMessageDialog(this,
                    "Necesitas al menos un pedido y un repartidor registrados para crear una entrega.",
                    "Faltan datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            controladorEntregas.registrarEntrega(pedido.getId(), repartidor.getId(),
                    txtFecha.getText().trim(), txtHora.getText().trim());
            JOptionPane.showMessageDialog(this, "Entrega registrada correctamente.\n"
                            + "El pedido #" + pedido.getId() + " quedó EN_REPARTO.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            refrescarTodo(); // el estado del pedido cambió, así que recargamos también los combos
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void editar() {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una entrega de la tabla para editarla.",
                    "Ninguna entrega seleccionada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Pedido pedido = (Pedido) cbxPedido.getSelectedItem();
        Repartidor repartidor = (Repartidor) cbxRepartidor.getSelectedItem();
        if (pedido == null || repartidor == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un pedido y un repartidor.",
                    "Faltan datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            controladorEntregas.actualizarEntrega(idSeleccionado, pedido.getId(), repartidor.getId(),
                    txtFecha.getText().trim(), txtHora.getText().trim());
            JOptionPane.showMessageDialog(this, "Entrega actualizada correctamente.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            refrescarTodo();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminar() {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una entrega de la tabla para eliminarla.",
                    "Ninguna entrega seleccionada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Pedimos confirmación porque borrar no se puede deshacer
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Seguro que quieres eliminar la entrega #" + idSeleccionado + "?\n"
                        + "Si el pedido estaba EN_REPARTO, volverá a PENDIENTE.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controladorEntregas.eliminarEntrega(idSeleccionado);
            JOptionPane.showMessageDialog(this, "Entrega eliminada correctamente.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            refrescarTodo();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
        }
    }
}
