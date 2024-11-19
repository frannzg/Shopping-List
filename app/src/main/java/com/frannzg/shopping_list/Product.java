package com.frannzg.shopping_list;

public class Product {

    private String name;
    private boolean isChecked;

    // Constructor vacío
    public Product() {
    }

    // Constructor con parámetros
    public Product(String name, boolean isChecked) {
        this.name = name;
        this.isChecked = isChecked;
    }

    // Getter para name
    public String getName() {
        return name;
    }

    // Setter para name
    public void setName(String name) {
        this.name = name;
    }

    // Getter para isChecked
    public boolean isChecked() {
        return isChecked;
    }

    // Setter para isChecked
    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
