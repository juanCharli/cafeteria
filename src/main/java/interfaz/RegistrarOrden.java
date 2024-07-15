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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.CallableStatement;
import java.sql.Types;

public class RegistrarOrden extends JFrame {
    
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtGrado;
    private JComboBox<String> cmbCategoria;
    private JComboBox<String> cmbProducto;
    private JTextField txtCantidad;
    private JTable tableOrden;
    private OrdenTableModel tableModel;
    private Connection connection;
    private JLabel lblTotal;
    
    public RegistrarOrden() {
        setTitle("Registrar Orden");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        JPanel panelTop = new JPanel();
        panelTop.setLayout(new GridLayout(4, 2, 0, 5));
        
        panelTop.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        panelTop.add(txtNombre);
        
        panelTop.add(new JLabel("Categoría:"));
        cmbCategoria = new JComboBox<>();
        panelTop.add(cmbCategoria);
        
        panelTop.add(new JLabel("Apellido:"));
        txtApellido = new JTextField();
        panelTop.add(txtApellido);
        
        panelTop.add(new JLabel("Producto:"));
        cmbProducto = new JComboBox<>();
        panelTop.add(cmbProducto);
        
        panelTop.add(new JLabel("Grado:"));
        txtGrado = new JTextField();
        panelTop.add(txtGrado);
        
        panelTop.add(new JLabel("Cantidad:"));
        txtCantidad = new JTextField();
        panelTop.add(txtCantidad);
        
        JButton btnAgregar = new JButton("Agregar a la Orden");
        panelTop.add(btnAgregar);
        
        add(panelTop, BorderLayout.NORTH);
        
        tableModel = new OrdenTableModel();
        tableOrden = new JTable(tableModel);
        add(new JScrollPane(tableOrden), BorderLayout.CENTER);
        
        JPanel panelBotones = new JPanel();
        
        JButton btnRegistrar = new JButton("Registrar Orden");
        panelBotones.add(btnRegistrar);
        
        JButton btnCancelar = new JButton("Cancelar orden");
        panelBotones.add(btnCancelar);
        
        lblTotal = new JLabel("Total de la venta: $0.00");
        panelBotones.add(lblTotal);
        
        add(panelBotones, BorderLayout.SOUTH);
        
        initializeDatabase();
        populateCategorias();
        
        cmbCategoria.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                populateProductos();
            }
        });
        
        btnAgregar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validarCampos()) {
                    agregarProducto();
                    actualizarTotal();
                }
                
            }
        });
        
        btnRegistrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarOrden();
            }
        });
        
        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelSelectedRow();
                actualizarTotal();
            }
        });
    }
    
    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/cafeteria", "juan", "juan123");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void populateCategorias() {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT nombre FROM Categorias");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cmbCategoria.addItem(rs.getString("nombre"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void populateProductos() {
        cmbProducto.removeAllItems();
        try {
            String categoria = (String) cmbCategoria.getSelectedItem();
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT nombre FROM Items WHERE categoriaID = (SELECT categoriaID FROM Categorias WHERE nombre = ?)");
            stmt.setString(1, categoria);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cmbProducto.addItem(rs.getString("nombre"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean validarCampos() {
        if (txtNombre.getText().trim().isEmpty() || txtApellido.getText().trim().isEmpty() || txtGrado.getText().trim().isEmpty() || cmbCategoria.getSelectedItem() == null || cmbProducto.getSelectedItem() == null || txtCantidad.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cantidad debe ser un número.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    private void agregarProducto() {
        String producto = (String) cmbProducto.getSelectedItem();
        int cantidad = Integer.parseInt(txtCantidad.getText());
        double precioUnitario = getItemPrecio(getItemID(producto));
        tableModel.addProducto(producto, cantidad, precioUnitario);
    }
    
    private void registrarOrden() {
        int rows = tableModel.getRowCount();
        if (rows == 0) {
            JOptionPane.showMessageDialog(this, "No hay productos para realizar la orden", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            String nombre = txtNombre.getText();
            String apellido = txtApellido.getText();
            String grado = txtGrado.getText();
            nombre.toUpperCase();
            apellido.toLowerCase();
            grado.toUpperCase();

            // Obtener alumnoID usando el procedimiento almacenado
            int alumnoID = getAlumnoID(nombre, apellido, grado);
            if (alumnoID == -1) {
                JOptionPane.showMessageDialog(this, "Alumno no encontrado.");
                return;
            }
            
            Date fecha = new Date(System.currentTimeMillis());
            JSONArray ventasArray = new JSONArray();
            
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String producto = (String) tableModel.getValueAt(i, 0);
                int cantidad = (int) tableModel.getValueAt(i, 1);
                int itemID = getItemID(producto);
                double precio = (double) tableModel.getValueAt(i, 2);
                
                JSONObject venta = new JSONObject();
                venta.put("itemID", itemID);
                venta.put("cantidad", cantidad);
                venta.put("precio", precio);
                ventasArray.put(venta);
            }

            // Llamar al procedimiento almacenado
            CallableStatement stmt = connection.prepareCall("{CALL RegistrarOrden(?, ?, ?, ?)}");
            stmt.setInt(1, alumnoID);
            stmt.setDate(2, fecha);
            stmt.setString(3, ventasArray.toString());
            stmt.registerOutParameter(4, Types.INTEGER);
            
            stmt.execute();
            int ordenID = stmt.getInt(4);
            // Limpiar la tabla después de registrar los ingresos
            tableModel.setRowCount(0);
            txtNombre.setText("");
            txtApellido.setText("");
            txtGrado.setText("");
            cmbCategoria.setSelectedIndex(0);
            cmbProducto.setSelectedIndex(0);
            txtCantidad.setText("");
            
            JOptionPane.showMessageDialog(this, "Orden registrada con ID: " + ordenID);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al registrar la orden: " + e.getMessage());
        }
    }
    
    private int getAlumnoID(String nombre, String apellido, String grado) {
        try {
            CallableStatement stmt = connection.prepareCall("{CALL RegistrarOBuscarAlumno(?, ?, ?, ?)}");
            stmt.setString(1, nombre);
            stmt.setString(2, apellido);
            stmt.setString(3, grado);
            stmt.registerOutParameter(4, Types.INTEGER);
            
            stmt.execute();
            return stmt.getInt(4);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    private int getItemID(String nombre) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT itemID FROM Items WHERE nombre = ?");
            stmt.setString(1, nombre);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("itemID");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    private double getItemPrecio(int itemID) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT precio FROM Items WHERE itemID = ?");
            stmt.setInt(1, itemID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("precio");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    
    private void cancelSelectedRow() {
        int selectedRow = tableOrden.getSelectedRow();
        if (selectedRow != -1) {
            tableModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione una fila para cancelar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void actualizarTotal() {
    double total = 0.0;
    for (int i = 0; i < tableModel.getRowCount(); i++) {
        double precioTotal = (double) tableModel.getValueAt(i, 3);
        total += precioTotal;
    }
    lblTotal.setText(String.format("Total de la venta: $%.2f", total));
}

}
