package com.frannzg.shopping_list;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShoppingListActivity extends AppCompatActivity {

    private EditText editProductName;
    private Button btnAddProduct;
    private ListView listViewProducts;
    private ArrayList<String> productList;
    private FirebaseDatabase database;
    private DatabaseReference productsRef;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        // Inicializa los elementos de la interfaz
        editProductName = findViewById(R.id.editProductName);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        listViewProducts = findViewById(R.id.listViewProducts);

        // Inicializa Firebase Database
        database = FirebaseDatabase.getInstance();

        // **Obtenemos el UID del usuario autenticado**
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid(); // Aquí obtenemos el UID del usuario autenticado

        // **Referencia a la lista de compras del usuario (utilizando su UID)**
        productsRef = database.getReference("shopping_list").child(uid).child("products");

        // Inicializa la lista de productos
        productList = new ArrayList<>();

        // Inicializa el ArrayAdapter y lo asigna al ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productList);
        listViewProducts.setAdapter(adapter);

        // Acción del botón "Agregar Producto"
        btnAddProduct.setOnClickListener(v -> {
            String productName = editProductName.getText().toString().trim();

            if (!productName.isEmpty()) {
                // **Guarda el producto en Firebase bajo el nodo del usuario actual**
                productsRef.push().setValue(productName)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ShoppingListActivity.this, "Producto agregado", Toast.LENGTH_SHORT).show();

                            // Agregar el producto a la lista en la interfaz de usuario
                            productList.add(productName);
                            adapter.notifyDataSetChanged(); // Actualiza el ListView
                            editProductName.setText(""); // Limpiar el campo de texto
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ShoppingListActivity.this, "Error al agregar el producto", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(ShoppingListActivity.this, "Por favor ingresa un nombre de producto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // **Leer los datos de Firebase (productos ya guardados)**
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid(); // Obtener el UID del usuario
        productsRef = FirebaseDatabase.getInstance().getReference("shopping_list").child(uid).child("products");

        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productList.clear(); // Limpiar la lista antes de agregar los nuevos productos

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String productName = snapshot.getValue(String.class);
                    productList.add(productName); // Agregar el producto a la lista
                }
                adapter.notifyDataSetChanged(); // Actualiza el ListView
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ShoppingListActivity.this, "Error al cargar los productos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
