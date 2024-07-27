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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import database.Conexion;
import java.io.File;
import java.io.IOException;

public class InventarioPanel extends JPanel {

    private JTextField productField;
    private JTextField quantityField;
    private JTextField moneyField;
    private JButton addButton;
    private JTable inventoryTable;
    private DefaultTableModel inventoryTableModel;
    private JButton registerButton;
    private JButton cancelButton;
    private JButton historyButton;

    public InventarioPanel() {
        setLayout(new BorderLayout());
        // Panel para el registro de ingreso
        JPanel registerPanel = new JPanel();
        registerPanel.setLayout(new GridBagLayout());
        registerPanel.setBorder(BorderFactory.createTitledBorder("Registrar Ingreso"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel productLabel = new JLabel("Producto:");
        gbc.gridx = 1;
        gbc.gridy = 0;
        registerPanel.add(productLabel, gbc);

        productField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        productField.setPreferredSize(new Dimension(150, 20));
        registerPanel.add(productField, gbc);

        JLabel quantityLabel = new JLabel("Cantidad:");
        gbc.gridx = 2;
        gbc.gridy = 0;
        registerPanel.add(quantityLabel, gbc);

        quantityField = new JTextField();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        quantityField.setPreferredSize(new Dimension(150, 20));
        registerPanel.add(quantityField, gbc);

        JLabel moneyLabel = new JLabel("Dinero Invertido:");
        gbc.gridx = 3;
        gbc.gridy = 0;
        registerPanel.add(moneyLabel, gbc);

        moneyField = new JTextField();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        moneyField.setPreferredSize(new Dimension(150, 20));
        registerPanel.add(moneyField, gbc);

        addButton = new JButton("Añadir al Registro");
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        registerPanel.add(addButton, gbc);

        add(registerPanel, BorderLayout.NORTH);

        // Panel para el registro de inventario
        JPanel inventoryPanel = new JPanel();
        inventoryPanel.setLayout(new BorderLayout());
        inventoryPanel.setBorder(BorderFactory.createTitledBorder("Registro de Inventario"));

        inventoryTableModel = new DefaultTableModel(new Object[]{"Producto", "Cantidad", "Dinero Invertido"}, 0);
        inventoryTable = new JTable(inventoryTableModel);
        inventoryPanel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        add(inventoryPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(); // Agregar el botón de registrar ingresos al final del panel
        registerButton = new JButton("Registrar Ingresos");
        registerButton.setBackground(Color.GREEN);
        bottomPanel.add(registerButton);
        cancelButton = new JButton("Cancelar Ingreso");
        cancelButton.setBackground(Color.RED);
        bottomPanel.add(cancelButton);
        historyButton = new JButton("Historial de Ingresos");
        bottomPanel.add(historyButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Acción del botón de añadir al registro
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addToRegister();
            }
        });

        // Acción del botón de registrar ingresos
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerAllInventory();
            }
        });

        // Acción del botón de cancelar ingresos
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelSelectedRow();
            }
        });

        // Acción del botón de historial de ingresos
        historyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showIncomeHistory();
            }
        });
    }

    private void addToRegister() {
        String product = productField.getText();
        String quantityStr = quantityField.getText();
        String moneyStr = moneyField.getText();

        // Validar campos
        if (product.isEmpty() || quantityStr.isEmpty() || moneyStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double money;
        try {
            money = Double.parseDouble(moneyStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Dinero invertido debe ser un número.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Añadir registro a la tabla de inventario
        Object[] newRow = {product, quantityStr, money};
        inventoryTableModel.addRow(newRow);

        // Limpiar campos
        productField.setText("");
        quantityField.setText("");
        moneyField.setText("");
    }

    private void registerAllInventory() {
        int rows = inventoryTableModel.getRowCount();
        if (rows == 0) {
            JOptionPane.showMessageDialog(this, "No hay registros para ingresar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = Conexion.getConnection()) {
            String sql = "INSERT INTO Ingresos (producto, cantidad, dinero_invertido) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i = 0; i < rows; i++) {
                    String product = (String) inventoryTableModel.getValueAt(i, 0);
                    String quantity = (String) inventoryTableModel.getValueAt(i, 1);
                    double money = (double) inventoryTableModel.getValueAt(i, 2);

                    statement.setString(1, product);
                    statement.setString(2, quantity);
                    statement.setDouble(3, money);

                    statement.addBatch();
                }
                statement.executeBatch();
            }

            // Limpiar la tabla después de registrar los ingresos
            inventoryTableModel.setRowCount(0);

            JOptionPane.showMessageDialog(this, "Ingresos registrados exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al registrar los ingresos en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelSelectedRow() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow != -1) {
            inventoryTableModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione una fila para cancelar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showIncomeHistory() {
    JFrame historyFrame = new JFrame("HISTORIAL DE INGRESOS");
    historyFrame.setSize(800, 600);
    historyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    historyFrame.setLayout(new BorderLayout());

    JPanel contentPanel = new JPanel(new BorderLayout());

    // Panel del historial de ingresos
    DefaultTableModel historyTableModel = new DefaultTableModel(new Object[]{"Producto", "Cantidad", "Dinero Invertido", "Fecha y Hora"}, 0);
    JTable historyTable = new JTable(historyTableModel);
    contentPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);

    historyFrame.add(contentPanel, BorderLayout.CENTER);

    // Botón para exportar a PDF
    JPanel buttonPanel = new JPanel();
    JButton exportButton = new JButton("Exportar a PDF");
    buttonPanel.add(exportButton);
    historyFrame.add(buttonPanel, BorderLayout.SOUTH);

    // Acción del botón de exportación
    exportButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                exportToPDF(historyTableModel);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(historyFrame, "Error al exportar a PDF.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    });

    // Cargar el historial de ingresos
    try (Connection connection = Conexion.getConnection()) {
        String sql = "SELECT producto, cantidad, dinero_invertido, fecha " +
                     "FROM Ingresos " +
                     "WHERE MONTH(fecha) = MONTH(CURDATE()) AND YEAR(fecha) = YEAR(CURDATE())";
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String productName = resultSet.getString("producto");
                String quantity = resultSet.getString("cantidad");
                double moneyInvested = resultSet.getDouble("dinero_invertido");
                Timestamp timestamp = resultSet.getTimestamp("fecha");

                Object[] row = {productName, quantity, moneyInvested, timestamp.toString()};
                historyTableModel.addRow(row);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(historyFrame, "Error al cargar el historial de ingresos.", "Error", JOptionPane.ERROR_MESSAGE);
    }

    historyFrame.setVisible(true);
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

            document.add(new Paragraph("Historial de Ingresos del Mes Actual").setBold().setFontSize(18));
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
