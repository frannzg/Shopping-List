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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageProductsActivity extends AppCompatActivity {

    private EditText editTextProductName;
    private Button btnAddProduct;
    private ListView listViewProducts;
    private ArrayList<String> productNames;
    private ArrayAdapter<String> adapter;
    private DatabaseReference productRef;
    private String listId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

        // Inicializar elementos de la interfaz
        editTextProductName = findViewById(R.id.editTextProductName);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        listViewProducts = findViewById(R.id.listViewProducts);
        productNames = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productNames);
        listViewProducts.setAdapter(adapter);

        // Obtener el ID de la lista desde el Intent
        listId = getIntent().getStringExtra("LIST_ID");
        productRef = FirebaseDatabase.getInstance().getReference("shopping_list")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(listId)
                .child("products");

        // Cargar productos existentes
        loadProducts();

        // Añadir nuevo producto
        btnAddProduct.setOnClickListener(v -> addProduct());
    }

    private void addProduct() {
        String productName = editTextProductName.getText().toString().trim();
        if (!productName.isEmpty()) {
            String productId = productRef.push().getKey();
            if (productId != null) {
                productRef.child(productId).setValue(productName).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ManageProductsActivity.this, "Producto añadido", Toast.LENGTH_SHORT).show();
                        editTextProductName.setText("");
                        loadProducts(); // Actualizar la lista de productos
                    } else {
                        Toast.makeText(ManageProductsActivity.this, "Error al añadir producto", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            Toast.makeText(this, "Introduce un nombre de producto", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProducts() {
        productNames.clear();
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String productName = snapshot.getValue(String.class);
                    if (productName != null) {
                        productNames.add(productName);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ManageProductsActivity.this, "Error al cargar los productos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
