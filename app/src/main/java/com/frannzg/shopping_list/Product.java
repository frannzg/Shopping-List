package com.frannzg.shopping_list;

public class Product {
    private String name;
    private boolean isBought;

    // Constructor
    public Product(String name, boolean isBought) {
        this.name = name;
        this.isBought = isBought;
    }

    // Getters y setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isBought() {
        return isBought;
    }

    public void setBought(boolean bought) {
        isBought = bought;
    }
}
