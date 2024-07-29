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
import database.Conexion;
import java.io.File;
import java.io.IOException;

public class ConsumosPanel extends JPanel {

    private JTextField productField;
    private JTextField quantityField;
    private JButton addButton;
    private JTable consumptionTable;
    private DefaultTableModel consumptionTableModel;
    private JButton registerButton;
    private JButton cancelButton;
    private JButton historyButton;

    public ConsumosPanel() {
        setLayout(new BorderLayout());

        // Panel para el registro de consumo
        JPanel registerPanel = new JPanel();
        registerPanel.setLayout(new GridBagLayout());
        registerPanel.setBorder(BorderFactory.createTitledBorder("Registrar Consumo"));

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

        addButton = new JButton("Añadir al Registro");
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        registerPanel.add(addButton, gbc);

        add(registerPanel, BorderLayout.NORTH);

        // Panel para el registro de consumos
        JPanel consumptionPanel = new JPanel();
        consumptionPanel.setLayout(new BorderLayout());
        consumptionPanel.setBorder(BorderFactory.createTitledBorder("Registro de Consumos"));

        consumptionTableModel = new DefaultTableModel(new Object[]{"Producto", "Cantidad"}, 0);
        consumptionTable = new JTable(consumptionTableModel);
        consumptionPanel.add(new JScrollPane(consumptionTable), BorderLayout.CENTER);

        add(consumptionPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        registerButton = new JButton("Registrar Consumos");
        registerButton.setBackground(Color.GREEN);
        bottomPanel.add(registerButton);
        cancelButton = new JButton("Cancelar Consumo");
        cancelButton.setBackground(Color.RED);
        bottomPanel.add(cancelButton);
        historyButton = new JButton("Historial de Consumos");
        bottomPanel.add(historyButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Acción del botón de añadir al registro
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addToRegister();
            }
        });

        // Acción del botón de registrar consumos
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerAllConsumptions();
            }
        });

        // Acción del botón de cancelar consumos
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelSelectedRow();
            }
        });

        // Acción del botón de historial de consumos
        historyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showConsumptionHistory();
            }
        });
    }

    private void addToRegister() {
        String product = productField.getText();
        String quantity = quantityField.getText();

        if (product.isEmpty() || quantity.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Añadir registro a la tabla de consumos
        Object[] newRow = {product, quantity};
        consumptionTableModel.addRow(newRow);

        // Limpiar campos
        productField.setText("");
        quantityField.setText("");
    }

    private void registerAllConsumptions() {
        int rows = consumptionTableModel.getRowCount();
        if (rows == 0) {
            JOptionPane.showMessageDialog(this, "No hay registros para ingresar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = Conexion.getConnection()) {
            String sql = "INSERT INTO Consumos (producto, cantidad_consumida) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i = 0; i < rows; i++) {
                    String product = (String) consumptionTableModel.getValueAt(i, 0);
                    String quantity = (String) consumptionTableModel.getValueAt(i, 1);

                    statement.setString(1, product);
                    statement.setString(2, quantity);

                    statement.addBatch();
                }
                statement.executeBatch();
            }

            // Limpiar la tabla después de registrar los consumos
            consumptionTableModel.setRowCount(0);

            JOptionPane.showMessageDialog(this, "Consumos registrados exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al registrar los consumos en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelSelectedRow() {
        int selectedRow = consumptionTable.getSelectedRow();
        if (selectedRow != -1) {
            consumptionTableModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione una fila para cancelar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showConsumptionHistory() {
        JFrame historyFrame = new JFrame("HISTORIAL DE CONSUMOS");
        historyFrame.setSize(800, 600);
        historyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        historyFrame.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new BorderLayout());

        // Panel del historial de consumos
        DefaultTableModel historyTableModel = new DefaultTableModel(new Object[]{"Producto", "Cantidad", "Fecha y Hora"}, 0);
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

        // Cargar el historial de consumos
        try (Connection connection = Conexion.getConnection()) {
            String sql = "SELECT producto, cantidad_consumida, fecha " +
                         "FROM Consumos " +
                         "WHERE MONTH(fecha) = MONTH(CURDATE()) AND YEAR(fecha) = YEAR(CURDATE())";
            try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    String productName = resultSet.getString("producto");
                    String quantity = resultSet.getString("cantidad_consumida");
                    Timestamp timestamp = resultSet.getTimestamp("fecha");

                    Object[] row = {productName, quantity, timestamp.toString()};
                    historyTableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(historyFrame, "Error al cargar el historial de consumos.", "Error", JOptionPane.ERROR_MESSAGE);
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

            document.add(new Paragraph("Historial de Consumos del Mes Actual").setBold().setFontSize(18));
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

