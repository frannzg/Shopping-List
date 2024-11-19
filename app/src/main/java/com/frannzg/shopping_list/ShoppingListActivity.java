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

    private EditText editProductName, editListName;
    private Button btnAddProduct, btnCreateList, btnShareList;
    private ListView listViewProducts;
    private ArrayList<String> productList;
    private ArrayAdapter<String> adapter;
    private DatabaseReference productsRef;
    private FirebaseAuth mAuth;
    private String currentListId; // Para almacenar la ID de la lista actual

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        // Inicializa los elementos de la interfaz
        editProductName = findViewById(R.id.editProductName);
        editListName = findViewById(R.id.editListName);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnCreateList = findViewById(R.id.btnCreateList);
        btnShareList = findViewById(R.id.btnShareList);
        listViewProducts = findViewById(R.id.listViewProducts);

        // Inicializa Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        productsRef = database.getReference("shopping_list").child(userId);

        // Inicializa la lista de productos
        productList = new ArrayList<>();

        // Inicializa el ArrayAdapter y lo asigna al ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productList);
        listViewProducts.setAdapter(adapter);

        // Acción del botón "Crear Lista"
        btnCreateList.setOnClickListener(v -> {
            String listName = editListName.getText().toString().trim();

            if (!listName.isEmpty()) {
                // Crear una nueva lista en Firebase bajo el usuario actual
                String listId = productsRef.push().getKey();
                DatabaseReference newListRef = productsRef.child(listId);

                // Crear un objeto de lista con nombre y productos vacíos
                newListRef.child("name").setValue(listName);
                newListRef.child("products");  // No es necesario inicializar con productos vacíos ya que Firebase manejará eso
                newListRef.child("shared_with");  // Aquí se agregan los usuarios con los que se comparte

                // Establecer la lista actual
                currentListId = listId;

                // Limpiar el campo de nombre de lista
                editListName.setText("");
                Toast.makeText(ShoppingListActivity.this, "Lista creada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ShoppingListActivity.this, "Por favor ingresa un nombre para la lista", Toast.LENGTH_SHORT).show();
            }
        });

        // Acción del botón "Agregar Producto"
        btnAddProduct.setOnClickListener(v -> {
            String productName = editProductName.getText().toString().trim();

            if (!productName.isEmpty()) {
                if (currentListId != null) {
                    // Agregar el producto a la lista actual
                    DatabaseReference productRef = productsRef.child(currentListId).child("products").push();
                    productRef.setValue(productName)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(ShoppingListActivity.this, "Producto agregado", Toast.LENGTH_SHORT).show();
                                productList.add(productName);
                                adapter.notifyDataSetChanged(); // Actualiza el ListView
                                editProductName.setText(""); // Limpiar el campo de texto
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ShoppingListActivity.this, "Error al agregar el producto", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(ShoppingListActivity.this, "Por favor selecciona o crea una lista", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ShoppingListActivity.this, "Por favor ingresa un nombre de producto", Toast.LENGTH_SHORT).show();
            }
        });

        // Acción del botón "Compartir Lista"
        btnShareList.setOnClickListener(v -> {
            String otherUserId = "OtroUsuarioId"; // Obtén el ID del usuario con quien deseas compartir la lista

            if (currentListId != null) {
                // Compartir la lista con otro usuario
                shareList(otherUserId);
            } else {
                Toast.makeText(ShoppingListActivity.this, "Por favor selecciona o crea una lista", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Verificar que el usuario está autenticado antes de intentar leer la base de datos
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Por favor inicia sesión", Toast.LENGTH_SHORT).show();
            return;  // Si el usuario no está autenticado, salir de la actividad
        }

        String userId = mAuth.getCurrentUser().getUid();

        if (currentListId == null) {
            Toast.makeText(this, "Por favor crea o selecciona una lista", Toast.LENGTH_SHORT).show();
            return;  // Si no hay lista seleccionada, no hacer nada
        }

        // Obtener la referencia a la base de datos
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference listRef = database.getReference("shopping_list").child(userId).child(currentListId);

        // Leer los productos de la lista
        listRef.child("products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productList.clear();  // Limpiar la lista antes de agregar los nuevos productos

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String productName = snapshot.getValue(String.class);
                    productList.add(productName);  // Agregar el producto a la lista
                }

                adapter.notifyDataSetChanged();  // Actualizar el ListView
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Imprimir el error en Logcat para ver la razón
                Toast.makeText(ShoppingListActivity.this, "Error al cargar los productos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para compartir una lista con otro usuario
    public void shareList(String otherUserId) {
        if (currentListId != null) {
            // Referencia a la lista actual del usuario
            DatabaseReference sharedRef = productsRef.child(currentListId).child("shared_with").child(otherUserId);
            sharedRef.setValue(true) // Marcar la lista como compartida con el otro usuario
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ShoppingListActivity.this, "Lista compartida", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ShoppingListActivity.this, "Error al compartir la lista", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
