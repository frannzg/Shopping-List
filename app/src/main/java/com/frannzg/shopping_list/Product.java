package com.frannzg.shopping_list;

public class Product {
    private String id; // Identificador único del producto en Firebase
    private String name;
    private boolean isBought;

    public Product() {}

    // Constructor
    public Product(String id, String name, boolean isBought) {
        this.id = id;
        this.name = name;
        this.isBought = isBought;
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
        return isBought;
    }

    public void setBought(boolean isBought) {
        this.isBought = isBought;
    }
}
