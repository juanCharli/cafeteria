/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package interfaz;

/**
 *
 * @author Juan Carlos
 */
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWindow extends JFrame {

    public MainWindow() {
        setTitle("Sistema de Ventas Escolar");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.setLayout(null);

        JButton btnRegistrarOrden = new JButton("Registrar Orden");
        btnRegistrarOrden.setBounds(50, 50, 150, 30);
        panel.add(btnRegistrarOrden);

        JButton btnHistorialVentas = new JButton("Historial de Ventas");
        btnHistorialVentas.setBounds(50, 100, 150, 30);
        panel.add(btnHistorialVentas);

        JButton btnGestionProductos = new JButton("Gestión de Productos");
        btnGestionProductos.setBounds(200, 75, 150, 30);
        panel.add(btnGestionProductos);

        btnRegistrarOrden.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RegistrarOrden().setVisible(true);
            }
        });

//        btnHistorialVentas.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                new HistorialVentasWindow().setVisible(true);
//            }
//        });
//
//        btnGestionProductos.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                new GestionProductosWindow().setVisible(true);
//            }
//        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
