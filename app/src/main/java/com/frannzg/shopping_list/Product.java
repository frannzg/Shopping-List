package com.frannzg.shopping_list;

public class Product {

    private String name;      // Nombre del producto
    private boolean isChecked; // Estado del producto (marcado o no)

    // Constructor vacío
    public Product() {
    }

    // Constructor con parámetros para inicializar el producto con su nombre y estado
    public Product(String name, boolean isChecked) {
        this.name = name;
        this.isChecked = isChecked;
    }

    // Getter para obtener el nombre del producto
    public String getName() {
        return name;
    }

    // Setter para establecer el nombre del producto
    public void setName(String name) {
        this.name = name;
    }

    // Getter para verificar si el producto está marcado (checked)
    public boolean isChecked() {
        return isChecked;
    }

    // Setter para actualizar el estado del producto (marcado o no marcado)
    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    // Método toString() para representar el producto como una cadena
    @Override
    public String toString() {
        return name + (isChecked ? " (Marcado)" : " (No marcado)");
    }
}
