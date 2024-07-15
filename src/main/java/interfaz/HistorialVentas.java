/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package interfaz;

/**
 *
 * @author Juan Carlos
 */

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.Vector;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

public class HistorialVentas extends JFrame {
    private JTable table;
    private JTextField txtAlumno;
    private JComboBox<String> cbCategoria, cbTipo;
    private JDatePickerImpl datePickerInicio, datePickerFin;
    private DefaultTableModel tableModel;
    private Connection connection;

    public HistorialVentas() {
        setTitle("Historial de Ventas");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Establecer conexión
        establecerConexion();

        // Panel de filtros
        JPanel panelFiltros = new JPanel();
        panelFiltros.setLayout(new GridLayout(5, 2)); // Cambié a 4 filas por 2 columnas para ajustar todos los componentes

        panelFiltros.add(new JLabel("Alumno:"));
        txtAlumno = new JTextField();
        panelFiltros.add(txtAlumno);

        panelFiltros.add(new JLabel("Categoría:"));
        cbCategoria = new JComboBox<>();
        panelFiltros.add(cbCategoria);

        panelFiltros.add(new JLabel("Tipo de Item:"));
        cbTipo = new JComboBox<>();
        panelFiltros.add(cbTipo);

        // Configurar las propiedades para JDatePanelImpl
        Properties p = new Properties();
        p.put("text.today", "Hoy");
        p.put("text.month", "Mes");
        p.put("text.year", "Año");

        // Crear el modelo de fecha
        UtilDateModel modelInicio = new UtilDateModel();
        UtilDateModel modelFin = new UtilDateModel();

        // Crear el panel de fecha con las propiedades
        JDatePanelImpl datePanelInicio = new JDatePanelImpl(modelInicio, p);
        JDatePanelImpl datePanelFin = new JDatePanelImpl(modelFin, p);

        // Crear el picker de fecha
        datePickerInicio = new JDatePickerImpl(datePanelInicio, new DateLabelFormatter());
        datePickerFin = new JDatePickerImpl(datePanelFin, new DateLabelFormatter());

        panelFiltros.add(new JLabel("Fecha Inicio:"));
        panelFiltros.add(datePickerInicio);

        panelFiltros.add(new JLabel("Fecha Fin:"));
        panelFiltros.add(datePickerFin);

        add(panelFiltros, BorderLayout.NORTH);

        // Botones de acción
        JPanel panelBotones = new JPanel();
        JButton btnBuscar = new JButton("Buscar");
        JButton btnLimpiar = new JButton("Limpiar Filtros");
        JButton btnExportar = new JButton("Exportar");

        panelBotones.add(btnBuscar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnExportar);
        add(panelBotones, BorderLayout.SOUTH);

        // Tabla de ventas
        tableModel = new DefaultTableModel();
        tableModel.addColumn("ID Venta");
        tableModel.addColumn("Fecha");
        tableModel.addColumn("Alumno");
        tableModel.addColumn("Item");
        tableModel.addColumn("Categoría");
        tableModel.addColumn("Tipo");
        tableModel.addColumn("Cantidad");
        tableModel.addColumn("Precio Unitario");
        tableModel.addColumn("Total");

        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Acción de buscar
        btnBuscar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buscarVentas();
            }
        });

        // Acción de limpiar filtros
        btnLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiarFiltros();
            }
        });

        // Acción de exportar (deberías implementar la funcionalidad de exportar)
        btnExportar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    exportToPDF(tableModel);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error al exportar a PDF.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Poblar los combobox con datos de la base de datos
        populateCategorias();
        populateTipos();

        // Agregar acción al cambiar la categoría seleccionada
        cbCategoria.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                populateTipos();
            }
        });

        setVisible(true);
    }

    private void establecerConexion() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/cafeteria", "juan", "juan123");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateCategorias() {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT nombre FROM Categorias");
            ResultSet rs = stmt.executeQuery();
            cbCategoria.removeAllItems(); // Limpiar el combobox antes de poblarlo
            cbCategoria.addItem("Todas"); // Agregar opción predeterminada
            while (rs.next()) {
                cbCategoria.addItem(rs.getString("nombre"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateTipos() {
        try {
            String categoria = (String) cbCategoria.getSelectedItem();
            PreparedStatement stmt;
            if (categoria != null && !categoria.equals("Todas")) {
                stmt = connection.prepareStatement(
                        "SELECT DISTINCT nombre FROM Items WHERE categoriaID = (SELECT categoriaID FROM Categorias WHERE nombre = ?)");
                stmt.setString(1, categoria);
            } else {
                stmt = connection.prepareStatement("SELECT DISTINCT nombre FROM Items");
            }
            ResultSet rs = stmt.executeQuery();
            cbTipo.removeAllItems(); // Limpiar el combobox antes de poblarlo
            cbTipo.addItem("Todos"); // Agregar opción predeterminada
            while (rs.next()) {
                cbTipo.addItem(rs.getString("nombre"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void buscarVentas() {
        String alumno = txtAlumno.getText();
        String categoria = cbCategoria.getSelectedItem().toString();
        String tipo = cbTipo.getSelectedItem().toString();
        java.util.Date fechaInicio = (java.util.Date) datePickerInicio.getModel().getValue();
        java.util.Date fechaFin = (java.util.Date) datePickerFin.getModel().getValue();

        try {
            String query = "SELECT v.ventaID, o.fecha, a.nombre, i.nombre, c.nombre, i.tipo, v.cantidad, v.precio, (v.cantidad * v.precio) as total " +
                    "FROM Ventas v " +
                    "JOIN Ordenes o ON v.ordenID = o.ordenID " +
                    "JOIN Alumnos a ON o.alumnoID = a.alumnoID " +
                    "JOIN Items i ON v.itemID = i.itemID " +
                    "JOIN Categorias c ON i.categoriaID = c.categoriaID " +
                    "WHERE 1=1";

            if (!alumno.isEmpty()) {
                query += " AND a.nombre LIKE ?";
            }
            if (!categoria.equals("Todas")) {
                query += " AND c.nombre = ?";
            }
            if (!tipo.equals("Todos")) {
                query += " AND i.nombre = ?";
            }
            if (fechaInicio != null) {
                query += " AND o.fecha >= ?";
            }
            if (fechaFin != null) {
                query += " AND o.fecha <= ?";
            }

            PreparedStatement stmt = connection.prepareStatement(query);

            int paramIndex = 1;
            if (!alumno.isEmpty()) {
                stmt.setString(paramIndex++, "%" + alumno + "%");
            }
            if (!categoria.equals("Todas")) {
                stmt.setString(paramIndex++, categoria);
            }
            if (!tipo.equals("Todos")) {
                stmt.setString(paramIndex++, tipo);
            }
            if (fechaInicio != null) {
                stmt.setDate(paramIndex++, new java.sql.Date(fechaInicio.getTime()));
            }
            if (fechaFin != null) {
                stmt.setDate(paramIndex++, new java.sql.Date(fechaFin.getTime()));
            }

            ResultSet rs = stmt.executeQuery();
            tableModel.setRowCount(0);

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt(1));
                row.add(rs.getDate(2));
                row.add(rs.getString(3));
                row.add(rs.getString(4));
                row.add(rs.getString(5));
                row.add(rs.getString(6));
                row.add(rs.getInt(7));
                row.add(rs.getBigDecimal(8));
                row.add(rs.getBigDecimal(9));
                tableModel.addRow(row);
            }

            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void limpiarFiltros() {
        txtAlumno.setText("");
        cbCategoria.setSelectedIndex(0);
        cbTipo.setSelectedIndex(0);
        datePickerInicio.getModel().setValue(null);
        datePickerFin.getModel().setValue(null);
        buscarVentas(); // Para refrescar la tabla sin filtros
    }
    
    private void exportToPDF(DefaultTableModel historyTableModel) throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar PDF");
        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            PdfWriter writer = new PdfWriter(fileToSave.getAbsolutePath() + ".pdf");
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Historial de Ventas").setBold().setFontSize(18));
            addTableToDocument(historyTableModel, document);
            document.close();
        }
    }

    private void addTableToDocument(DefaultTableModel tableModel, Document document) {
        int numCols = tableModel.getColumnCount();
        Table table = new Table(UnitValue.createPercentArray(numCols)).useAllAvailableWidth();

        // Agregar encabezados de columna
        for (int col = 0; col < numCols; col++) {
            table.addHeaderCell(new Paragraph(tableModel.getColumnName(col)).setBold());
        }

        // Agregar filas de datos
        int numRows = tableModel.getRowCount();
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                table.addCell(new Paragraph(tableModel.getValueAt(row, col).toString()));
            }
        }

        document.add(table);
    }

}
