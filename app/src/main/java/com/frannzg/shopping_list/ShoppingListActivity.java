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
import java.util.HashMap;

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
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, shoppingListNames);
        listViewShoppingLists.setAdapter(adapter);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        shoppingListRef = FirebaseDatabase.getInstance().getReference("shopping_list");

        // Acciones del botón para crear una nueva lista
        btnCreateList.setOnClickListener(v -> {
            // Crear una nueva lista
            String listName = "Lista de compra " + (shoppingListNames.size() + 1); // Ejemplo de nombre para la lista
            String listId = shoppingListRef.push().getKey();

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
            intent.putExtra("LIST_ID", selectedListId);
            startActivity(intent); // Redirigir a la nueva actividad
        });

        // Cargar las listas de compras del usuario autenticado
        loadShoppingLists();
    }

    private void loadShoppingLists() {
        shoppingListNames.clear();
        shoppingListIds.clear();
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
                    adapter.notifyDataSetChanged();
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
