package com.frannzg.shopping_list;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

    private Button btnCreateList;
    private ListView listViewShoppingLists;
    private ArrayList<String> shoppingListNames;
    private ArrayList<String> shoppingListIds;
    private ArrayAdapter<String> adapter;
    private FirebaseAuth mAuth;
    private DatabaseReference shoppingListRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        // Inicializar los elementos de la interfaz
        btnCreateList = findViewById(R.id.btnCreateList);
        listViewShoppingLists = findViewById(R.id.listViewShoppingLists);
        shoppingListNames = new ArrayList<>();
        shoppingListIds = new ArrayList<>();

        // Usar el diseño personalizado para el texto de las listas
        adapter = new ArrayAdapter<>(this, R.layout.list_item_white_text, shoppingListNames);
        listViewShoppingLists.setAdapter(adapter);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        shoppingListRef = FirebaseDatabase.getInstance().getReference("shopping_list");

        // Acciones del botón para crear una nueva lista
        btnCreateList.setOnClickListener(v -> {
            // Crear una nueva lista con un nombre predeterminado
            String listName = "Lista de compra " + (shoppingListNames.size() + 1); // Nombre de la lista
            String listId = shoppingListRef.push().getKey(); // Obtener el ID único para la lista

            if (listId != null) {
                shoppingListRef.child(mAuth.getCurrentUser().getUid()).child(listId).child("name").setValue(listName)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ShoppingListActivity.this, "Lista creada", Toast.LENGTH_SHORT).show();
                                loadShoppingLists(); // Cargar las listas después de crear una nueva
                            } else {
                                Toast.makeText(ShoppingListActivity.this, "Error al crear la lista", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Configurar clics en las listas
        listViewShoppingLists.setOnItemClickListener((parent, view, position, id) -> {
            String selectedListId = shoppingListIds.get(position); // Obtener el ID de la lista seleccionada
            Intent intent = new Intent(ShoppingListActivity.this, ManageProductsActivity.class);
            intent.putExtra("LIST_ID", selectedListId); // Pasar el ID de la lista a la siguiente actividad
            startActivity(intent); // Redirigir a la actividad para gestionar los productos
        });

        // Cargar las listas de compras del usuario autenticado
        loadShoppingLists();
    }

    // Método para cargar las listas de compra del usuario desde Firebase
    private void loadShoppingLists() {
        shoppingListNames.clear();  // Limpiar la lista de nombres
        shoppingListIds.clear();    // Limpiar la lista de IDs

        // Obtener las listas del usuario desde la base de datos
        shoppingListRef.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String listName = snapshot.child("name").getValue(String.class);
                        String listId = snapshot.getKey(); // Obtener el ID de la lista
                        if (listName != null && listId != null) {
                            shoppingListNames.add(listName);
                            shoppingListIds.add(listId);
                        }
                    }
                    adapter.notifyDataSetChanged(); // Actualizar la vista del ListView
                } else {
                    Toast.makeText(ShoppingListActivity.this, "No tienes listas de compra", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ShoppingListActivity.this, "Error al cargar las listas", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
