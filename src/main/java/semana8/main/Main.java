package semana8.main;

import semana8.conexion.ConexionBD;
import semana8.controlador.ControladorEntregas;
import semana8.controlador.ControladorPedidos;
import semana8.controlador.ControladorRepartidores;
import semana8.vista.VentanaPrincipal;

import javax.swing.*;


public class Main {
    public static void main(String[] args) {
        // Swing exige que las ventanas se creen en su propio hilo (el "hilo de eventos").
        // invokeLater(...) se encarga de eso.
        SwingUtilities.invokeLater(() -> {

            // 1. Antes de abrir nada, probamos que MySQL responda.
            if (!ConexionBD.probarConexion()) {
                JOptionPane.showMessageDialog(null,
                        "No se pudo conectar a la base de datos speedfast_db.\n\n"
                                + "Revisa que:\n"
                                + " - El servicio MySQL80 esté encendido\n"
                                + " - La base de datos speedfast_db exista (script en resources/speedfast_db.sql)\n"
                                + " - El usuario y la contraseña en ConexionBD.java sean correctos",
                        "Error de conexión",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1); // cierra el programa (el 1 indica que terminó por un error)
                return;
            }

            // 2. Creamos los tres controladores (cada uno crea su propio DAO por dentro)
            ControladorPedidos controladorPedidos = new ControladorPedidos();
            ControladorRepartidores controladorRepartidores = new ControladorRepartidores();
            ControladorEntregas controladorEntregas = new ControladorEntregas();

            // 3. Creamos la ventana principal pasándole los tres controladores y la mostramos
            VentanaPrincipal ventanaPrincipal = new VentanaPrincipal(
                    controladorPedidos, controladorRepartidores, controladorEntregas);
            ventanaPrincipal.setVisible(true);
        });
    }
}
