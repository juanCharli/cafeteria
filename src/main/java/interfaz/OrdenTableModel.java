/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package interfaz;

/**
 *
 * @author Juan Carlos
 */
import javax.swing.table.DefaultTableModel;

class OrdenTableModel extends DefaultTableModel {

    private final String[] columnNames = {"Producto", "Cantidad", "Precio Unitario", "Precio Total"};
    private final Class[] columnClasses = {String.class, Integer.class, Double.class, Double.class};

    public OrdenTableModel() {
        super(new Object[][]{}, new String[]{"Producto", "Cantidad", "Precio Unitario", "Precio Total"});
    }

    public void addProducto(String producto, int cantidad, double precioUnitario) {
        double precioTotal = cantidad * precioUnitario;
        addRow(new Object[]{producto, cantidad, precioUnitario, precioTotal});
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnClasses[columnIndex];
    }

    @Override
    public void removeRow(int row) {
        super.removeRow(row);
    }
    
}
