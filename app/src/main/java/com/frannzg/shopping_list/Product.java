package com.frannzg.shopping_list;

public class Product {

    private String id;  // ID único del producto
    private String name;  // Nombre del producto
    private boolean bought;  // Estado de si ha sido comprado

    // Constructor vacío necesario para Firebase
    public Product() {}

    // Constructor con parámetros
    public Product(String id, String name, boolean bought) {
        this.id = id;
        this.name = name;
        this.bought = bought;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isBought() {
        return bought;
    }

    public void setBought(boolean bought) {
        this.bought = bought;
    }
}
