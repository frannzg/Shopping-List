package com.frannzg.shopping_list;

import android.os.Bundle;
import android.util.Log;
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

    private ListView listViewProducts;
    private ArrayList<Product> productList;
    private ProductAdapter adapter;
    private DatabaseReference productRef;
    private String listId;
    private EditText editTextProductName;
    private final String logTag = "ShoppingListApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

        try {
            // Inicializar elementos
            listViewProducts = findViewById(R.id.listViewProducts);
            editTextProductName = findViewById(R.id.editTextProductName);
            productList = new ArrayList<>();

            // Obtener ID de usuario y lista
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            listId = getIntent().getStringExtra("LIST_ID");

            productRef = FirebaseDatabase.getInstance().getReference("shopping_list")
                    .child(userId)
                    .child(listId)
                    .child("products");

            // Configurar adaptador personalizado
            adapter = new ProductAdapter(this, productList, listId, userId);
            listViewProducts.setAdapter(adapter);

            loadProducts();

            // Botón para añadir un nuevo producto
            findViewById(R.id.btnAddProduct).setOnClickListener(v -> addProduct());

        } catch (Exception e) {
            Log.e(logTag, e.getMessage(), e);
        }
    }

    private void loadProducts() {
        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }
                adapter.notifyDataSetChanged(); // Actualizar lista en la vista
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ManageProductsActivity.this, "Error al cargar los productos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addProduct() {
        String productName = editTextProductName.getText().toString();

        if (!productName.isEmpty()) {
            String productId = productRef.push().getKey();

            if (productId != null) {
                Product newProduct = new Product(productId, productName, false);
                productRef.child(productId).setValue(newProduct)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Producto añadido", Toast.LENGTH_SHORT).show();
                            editTextProductName.setText(""); // Limpiar campo
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Error al añadir el producto", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Error al generar ID para el producto", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Por favor, ingresa un nombre de producto", Toast.LENGTH_SHORT).show();
        }
    }
}
