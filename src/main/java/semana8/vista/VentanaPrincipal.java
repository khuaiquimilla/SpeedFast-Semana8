package semana8.vista;

import semana8.controlador.ControladorEntregas;
import semana8.controlador.ControladorPedidos;
import semana8.controlador.ControladorRepartidores;
import semana8.modelo.EstadoPedido;
import semana8.modelo.Pedido;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// VISTA principal: es la primera ventana que se abre.
// Muestra los pedidos en una tabla (JTable), con filtros por estado y tipo,
// y tiene los botones para gestionar pedidos, repartidores y entregas.
public class VentanaPrincipal extends JFrame {

    // Posición de cada columna en la tabla. Usar constantes con nombre evita
    // escribir números "mágicos" como getValueAt(fila, 3), que no se entienden.
    private static final int COL_ID = 0;
    private static final int COL_DIRECCION = 1;
    private static final int COL_TIPO = 2;
    private static final int COL_ESTADO = 3;

    private final ControladorPedidos controladorPedidos;
    private final ControladorRepartidores controladorRepartidores;
    private final ControladorEntregas controladorEntregas;

    private JTable tblPedidos;
    private DefaultTableModel modeloTabla;
    private JButton btnEditarPedido;
    private JButton btnEliminarPedido;
    private JButton btnMarcarEntregado;
    private JComboBox<String> cbxFiltroEstado;
    private JComboBox<String> cbxFiltroTipo;
    private JLabel lblAyuda;

    // El constructor recibe los tres controladores ya creados en Main
    public VentanaPrincipal(ControladorPedidos controladorPedidos,
                            ControladorRepartidores controladorRepartidores,
                            ControladorEntregas controladorEntregas) {
        this.controladorPedidos = controladorPedidos;
        this.controladorRepartidores = controladorRepartidores;
        this.controladorEntregas = controladorEntregas;

        // --- Configuración general de la ventana ---
        setTitle("SpeedFast - Gestión de Entregas");
        setSize(950, 520);
        setMinimumSize(new Dimension(950, 400)); // no deja achicarla tanto que se corten los botones
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // al cerrar esta ventana, se cierra la app
        setLocationRelativeTo(null); // centrada en la pantalla

        // --- Tabla de pedidos ---
        String[] columnas = {"ID", "Dirección", "Tipo", "Estado", "Repartidor"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // el usuario no puede escribir dentro de las celdas
            }
        };
        tblPedidos = new JTable(modeloTabla);
        tblPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // solo una fila a la vez
        tblPedidos.setRowHeight(24); // filas un poco más altas, más fáciles de leer y clickear

        // Ancho de cada columna: la ID solo tiene un número, así que va angosta
        tblPedidos.getColumnModel().getColumn(COL_ID).setMaxWidth(60);
        tblPedidos.getColumnModel().getColumn(COL_DIRECCION).setPreferredWidth(320);
        tblPedidos.getColumnModel().getColumn(COL_TIPO).setPreferredWidth(110);
        tblPedidos.getColumnModel().getColumn(COL_ESTADO).setPreferredWidth(110);
        tblPedidos.getColumnModel().getColumn(4).setPreferredWidth(170); // Repartidor

        // Cada vez que el usuario selecciona (o deselecciona) una fila,
        // revisamos qué botones deben estar activos y cuáles grises
        tblPedidos.getSelectionModel().addListSelectionListener(e -> actualizarBotonesSegunSeleccion());

        // JScrollPane agrega barra de desplazamiento cuando hay muchos pedidos
        JScrollPane scrollPane = new JScrollPane(tblPedidos);

        // --- Fila 1 de botones: AGREGAR y GESTIONAR datos ---
        // "Asignar Repartidor" es la asignación rápida (fecha y hora actuales);
        // "Gestionar Entregas" abre el CRUD completo de entregas.
        JButton btnRegistrarPedido = new JButton("Registrar Pedido");
        JButton btnRepartidores = new JButton("Gestionar Repartidores");
        JButton btnAsignar = new JButton("Asignar Repartidor");
        JButton btnEntregas = new JButton("Gestionar Entregas");

        JPanel panelGestion = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        panelGestion.add(btnRegistrarPedido);
        panelGestion.add(btnRepartidores);
        panelGestion.add(btnAsignar);
        panelGestion.add(btnEntregas);

        // --- Fila 2 de botones: acciones sobre el pedido SELECCIONADO en la tabla ---
        btnEditarPedido = new JButton("Editar Pedido");
        btnEliminarPedido = new JButton("Eliminar Pedido");
        btnMarcarEntregado = new JButton("Marcar como Entregado");
        JButton btnRefrescar = new JButton("Refrescar");

        JPanel panelSeleccion = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        panelSeleccion.add(btnEditarPedido);
        panelSeleccion.add(btnEliminarPedido);
        panelSeleccion.add(btnMarcarEntregado);
        panelSeleccion.add(btnRefrescar);

        // --- Fila 3: filtros de la tabla (opcionales: "TODOS" = sin filtro) ---
        cbxFiltroEstado = new JComboBox<>(new String[]{
                ControladorPedidos.FILTRO_TODOS, "PENDIENTE", "EN_REPARTO", "ENTREGADO"});
        cbxFiltroTipo = new JComboBox<>(new String[]{
                ControladorPedidos.FILTRO_TODOS, "COMIDA", "ENCOMIENDA", "EXPRESS"});

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        panelFiltros.add(new JLabel("Filtrar por estado:"));
        panelFiltros.add(cbxFiltroEstado);
        panelFiltros.add(new JLabel("Tipo:"));
        panelFiltros.add(cbxFiltroTipo);

        // Las 3 filas, una debajo de la otra
        JPanel panelSuperior = new JPanel(new GridLayout(3, 1));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panelSuperior.add(panelGestion);
        panelSuperior.add(panelSeleccion);
        panelSuperior.add(panelFiltros);

        // --- Línea de ayuda (abajo) ---
        lblAyuda = new JLabel();
        lblAyuda.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        // --- Eventos (ActionListener escrito como lambda: e -> ...) ---
        btnRegistrarPedido.addActionListener(e -> {
            VentanaRegistroPedido v = new VentanaRegistroPedido(controladorPedidos, null); // null = pedido nuevo
            refrescarAlCerrar(v); // cuando se cierre, la tabla se actualiza sola
            v.setVisible(true);
        });

        btnEditarPedido.addActionListener(e -> editarPedido());
        btnEliminarPedido.addActionListener(e -> eliminarPedido());
        btnMarcarEntregado.addActionListener(e -> marcarComoEntregado());
        btnRefrescar.addActionListener(e -> refrescarTabla());

        btnRepartidores.addActionListener(e -> {
            VentanaRepartidores v = new VentanaRepartidores(controladorRepartidores);
            refrescarAlCerrar(v); // al cerrar, la tabla de pedidos se actualiza (por si cambió un nombre)
            v.setVisible(true);
        });

        btnAsignar.addActionListener(e -> {
            VentanaAsignarEntrega v = new VentanaAsignarEntrega(
                    controladorPedidos, controladorRepartidores, controladorEntregas);
            refrescarAlCerrar(v);
            v.setVisible(true);
        });

        btnEntregas.addActionListener(e -> {
            VentanaEntregas v = new VentanaEntregas(
                    controladorEntregas, controladorPedidos, controladorRepartidores);
            refrescarAlCerrar(v); // al cerrar, la tabla de pedidos se actualiza (los estados pueden cambiar)
            v.setVisible(true);
        });

        // Al cambiar un filtro, la tabla se vuelve a cargar con ese filtro aplicado
        cbxFiltroEstado.addActionListener(e -> refrescarTabla());
        cbxFiltroTipo.addActionListener(e -> refrescarTabla());

        // --- Armado final de la ventana ---
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(lblAyuda, BorderLayout.SOUTH);

        // Cargamos los pedidos desde MySQL apenas se abre la ventana
        refrescarTabla();
    }

    // Vuelve a pedir los pedidos a la base de datos (con los filtros elegidos) y los dibuja en la tabla
    private void refrescarTabla() {
        String filtroEstado = (String) cbxFiltroEstado.getSelectedItem();
        String filtroTipo = (String) cbxFiltroTipo.getSelectedItem();
        controladorPedidos.cargarTabla(modeloTabla, filtroEstado, filtroTipo);
        actualizarBotonesSegunSeleccion(); // al recargar se pierde la selección, así que actualizamos
    }

    // Le pide a Java que nos avise cuando se cierre una ventana secundaria, para refrescar la tabla
    private void refrescarAlCerrar(JFrame ventanaSecundaria) {
        ventanaSecundaria.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                refrescarTabla();
            }
        });
    }

    // Activa o desactiva los botones y actualiza la línea de ayuda,
    // según qué pedido esté seleccionado. Así el usuario siempre sabe qué puede hacer.
    private void actualizarBotonesSegunSeleccion() {
        int fila = tblPedidos.getSelectedRow(); // -1 significa "ninguna fila seleccionada"

        // Editar y eliminar solo tienen sentido si hay un pedido seleccionado
        btnEditarPedido.setEnabled(fila != -1);
        btnEliminarPedido.setEnabled(fila != -1);

        if (fila == -1) {
            btnMarcarEntregado.setEnabled(false);
            lblAyuda.setText("Total: " + modeloTabla.getRowCount() + " pedido(s). "
                    + "Selecciona un pedido para editarlo, eliminarlo o marcarlo como entregado.");
            return;
        }

        int idPedido = (Integer) modeloTabla.getValueAt(fila, COL_ID);
        EstadoPedido estado = (EstadoPedido) modeloTabla.getValueAt(fila, COL_ESTADO);

        // Solo un pedido EN_REPARTO puede pasar a ENTREGADO
        btnMarcarEntregado.setEnabled(estado == EstadoPedido.EN_REPARTO);

        // "switch" elige un mensaje distinto según el estado del pedido seleccionado
        switch (estado) {
            case PENDIENTE:
                lblAyuda.setText("Pedido #" + idPedido + " está PENDIENTE: primero asígnale un repartidor.");
                break;
            case EN_REPARTO:
                lblAyuda.setText("Pedido #" + idPedido + " va en camino: puedes marcarlo como entregado.");
                break;
            case ENTREGADO:
                lblAyuda.setText("Pedido #" + idPedido + " ya fue entregado.");
                break;
        }
    }

    // Se ejecuta al presionar "Editar Pedido": abre el formulario con los datos del pedido seleccionado
    private void editarPedido() {
        int fila = tblPedidos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Primero selecciona un pedido haciendo clic en su fila de la tabla.",
                    "Ningún pedido seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Armamos un objeto Pedido con los datos de la fila seleccionada
        int id = (Integer) modeloTabla.getValueAt(fila, COL_ID);
        String direccion = (String) modeloTabla.getValueAt(fila, COL_DIRECCION);
        String tipo = (String) modeloTabla.getValueAt(fila, COL_TIPO);
        EstadoPedido estado = (EstadoPedido) modeloTabla.getValueAt(fila, COL_ESTADO);

        Pedido pedido = new Pedido(id, direccion, tipo);
        pedido.setEstado(estado);

        // Abrimos el mismo formulario de registro, pero en modo "editar"
        VentanaRegistroPedido v = new VentanaRegistroPedido(controladorPedidos, pedido);
        refrescarAlCerrar(v);
        v.setVisible(true);
    }

    // Se ejecuta al presionar "Eliminar Pedido"
    private void eliminarPedido() {
        int fila = tblPedidos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Primero selecciona un pedido haciendo clic en su fila de la tabla.",
                    "Ningún pedido seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idPedido = (Integer) modeloTabla.getValueAt(fila, COL_ID);

        // Pedimos confirmación porque borrar no se puede deshacer
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Seguro que quieres eliminar el pedido #" + idPedido + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controladorPedidos.eliminarPedido(idPedido);
            JOptionPane.showMessageDialog(this, "Pedido eliminado correctamente.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            refrescarTabla();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Se ejecuta al presionar "Marcar como Entregado"
    private void marcarComoEntregado() {
        int fila = tblPedidos.getSelectedRow();

        // Revisión de seguridad: el botón ya está gris en estos casos,
        // pero validamos igual por si acaso.
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Primero selecciona un pedido haciendo clic en su fila de la tabla.",
                    "Ningún pedido seleccionado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idPedido = (Integer) modeloTabla.getValueAt(fila, COL_ID);
        EstadoPedido estadoActual = (EstadoPedido) modeloTabla.getValueAt(fila, COL_ESTADO);

        if (estadoActual != EstadoPedido.EN_REPARTO) {
            JOptionPane.showMessageDialog(this,
                    "Solo puedes marcar como entregado un pedido que está EN_REPARTO.",
                    "Acción no permitida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Pedimos confirmación antes de cambiar el estado
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Confirmas que el pedido #" + idPedido + " fue entregado?",
                "Confirmar entrega",
                JOptionPane.YES_NO_OPTION);

        if (respuesta != JOptionPane.YES_OPTION) {
            return; // el usuario dijo que no: no hacemos nada
        }

        try {
            controladorPedidos.actualizarEstado(idPedido, EstadoPedido.ENTREGADO);
            refrescarTabla(); // para ver el nuevo estado en la tabla altiro
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}
