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

public class ManageProductsActivity extends AppCompatActivity {

    private Button btnAddProduct;
    private EditText edtProductName;
    private ListView listViewProducts;
    private ArrayList<String> productNames;
    private ArrayAdapter<String> adapter;
    private FirebaseAuth mAuth;
    private DatabaseReference productsRef;
    private String listId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

        // Inicializar los elementos de la interfaz
        btnAddProduct = findViewById(R.id.btnAddProduct);
        edtProductName = findViewById(R.id.edtProductName);
        listViewProducts = findViewById(R.id.listViewProducts);
        productNames = new ArrayList<>();

        // Usar el diseño personalizado para el texto de los productos
        adapter = new ArrayAdapter<>(this, R.layout.list_item_white_text, productNames);
        listViewProducts.setAdapter(adapter);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        productsRef = FirebaseDatabase.getInstance().getReference("shopping_list");

        // Obtener el ID de la lista desde el Intent
        listId = getIntent().getStringExtra("LIST_ID");

        // Cargar los productos de la lista seleccionada
        loadProducts();

        // Configurar la acción para agregar un producto
        btnAddProduct.setOnClickListener(v -> {
            String productName = edtProductName.getText().toString().trim();
            if (!productName.isEmpty()) {
                addProduct(productName);
            } else {
                Toast.makeText(ManageProductsActivity.this, "Por favor ingresa un nombre de producto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para cargar los productos de la lista seleccionada
    private void loadProducts() {
        if (listId != null) {
            productsRef.child(mAuth.getCurrentUser().getUid()).child(listId).child("products").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String productName = snapshot.getValue(String.class);
                            if (productName != null) {
                                productNames.add(productName);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ManageProductsActivity.this, "No tienes productos en esta lista", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ManageProductsActivity.this, "Error al cargar los productos", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Método para agregar un producto a la lista
    private void addProduct(String productName) {
        if (listId != null) {
            String productId = productsRef.push().getKey();
            if (productId != null) {
                productsRef.child(mAuth.getCurrentUser().getUid()).child(listId).child("products").child(productId).setValue(productName)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ManageProductsActivity.this, "Producto agregado", Toast.LENGTH_SHORT).show();
                                productNames.add(productName);
                                adapter.notifyDataSetChanged();
                                edtProductName.setText(""); // Limpiar el campo de texto
                            } else {
                                Toast.makeText(ManageProductsActivity.this, "Error al agregar el producto", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }
}
