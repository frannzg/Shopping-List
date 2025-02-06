package com.frannzg.shopping_list;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShoppingListActivity extends AppCompatActivity {

    private Button btnCreateList, btnImportList;
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
        btnImportList = findViewById(R.id.btnImportList);
        listViewShoppingLists = findViewById(R.id.listViewShoppingLists);
        shoppingListNames = new ArrayList<>();
        shoppingListIds = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, R.layout.list_item_white_text, shoppingListNames);
        listViewShoppingLists.setAdapter(adapter);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        shoppingListRef = FirebaseDatabase.getInstance().getReference("shopping_list");

        // Acción del botón para crear una nueva lista
        btnCreateList.setOnClickListener(v -> showCreateListDialog());

        // Acción del botón para importar una lista
        btnImportList.setOnClickListener(v -> showImportListDialog());

        // Cargar listas existentes
        loadShoppingLists();

        // Configurar clic en las listas
        listViewShoppingLists.setOnItemClickListener((parent, view, position, id) -> {
            String selectedListId = shoppingListIds.get(position);
            showOptionsDialog(selectedListId, shoppingListNames.get(position), position);
        });
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
                        String listId = snapshot.getKey();
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

    private void showCreateListDialog() {
        final EditText input = new EditText(this);
        input.setHint("Nombre de la lista");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nueva lista de compras")
                .setView(input)
                .setPositiveButton("Crear", (dialog, which) -> {
                    String listName = input.getText().toString().trim();
                    if (!listName.isEmpty()) {
                        createNewShoppingList(listName);
                    } else {
                        Toast.makeText(ShoppingListActivity.this, "Por favor, ingresa un nombre válido", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void createNewShoppingList(String listName) {
        String listId = shoppingListRef.push().getKey();
        if (listId != null) {
            shoppingListRef.child(mAuth.getCurrentUser().getUid()).child(listId).child("name").setValue(listName)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ShoppingListActivity.this, "Lista creada", Toast.LENGTH_SHORT).show();
                            loadShoppingLists();
                        } else {
                            Toast.makeText(ShoppingListActivity.this, "Error al crear la lista", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showOptionsDialog(String listId, String listName, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opciones de lista: " + listName);

        builder.setItems(new CharSequence[]{"Editar Productos", "Eliminar", "Compartir por Texto", "Atrás"},
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            Intent intent1 = new Intent(ShoppingListActivity.this, ManageProductsActivity.class);
                            intent1.putExtra("LIST_ID", listId);
                            startActivity(intent1);
                            break;
                        case 1:
                            deleteShoppingList(listId, position);
                            break;
                        case 2:
                            shareShoppingList(listId);
                            break;
                        case 3:
                            dialog.dismiss();
                            break;
                    }
                });

        builder.show();
    }

    private void deleteShoppingList(String listId, int position) {
        shoppingListRef.child(mAuth.getCurrentUser().getUid()).child(listId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        shoppingListNames.remove(position);
                        shoppingListIds.remove(position);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(ShoppingListActivity.this, "Lista eliminada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ShoppingListActivity.this, "Error al eliminar la lista", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void shareShoppingList(String listId) {
        String currentUserId = mAuth.getCurrentUser().getUid();

        // Añadir el ID del usuario actual en la lista compartida
        shoppingListRef.child(currentUserId).child(listId).child("shared_with").child(currentUserId).setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String shareText = "ID de la lista: " + listId + "\n" +
                                "Copia este ID y pégalo en la opción de 'Importar Lista' para verla.";

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, shareText);
                        startActivity(Intent.createChooser(intent, "Compartir lista"));
                        Toast.makeText(ShoppingListActivity.this, "Lista compartida", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ShoppingListActivity.this, "Error al compartir la lista", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showImportListDialog() {
        final EditText input = new EditText(this);
        input.setHint("ID de la lista");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Importar lista de compras")
                .setView(input)
                .setPositiveButton("Importar", (dialog, which) -> {
                    String listId = input.getText().toString().trim();
                    if (!listId.isEmpty()) {
                        importShoppingList(listId);
                    } else {
                        Toast.makeText(ShoppingListActivity.this, "Por favor, ingresa un ID válido", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void importShoppingList(String listId) {
        String currentUserId = mAuth.getCurrentUser().getUid();

        // Buscar la lista en la base de datos por el ID
        shoppingListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean listFound = false;

                // Recorrer todos los usuarios para buscar la lista compartida
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    if (userSnapshot.hasChild(listId)) {
                        DataSnapshot listSnapshot = userSnapshot.child(listId);
                        String listName = listSnapshot.child("name").getValue(String.class);

                        // Verificar si la lista está compartida con el usuario actual
                        if (listName != null && listSnapshot.child("shared_with").hasChild(currentUserId)) {
                            // Si la lista está compartida, importar la lista
                            shoppingListRef.child(currentUserId).child(listId)
                                    .setValue(listSnapshot.getValue())
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ShoppingListActivity.this, "Lista importada: " + listName, Toast.LENGTH_SHORT).show();
                                            loadShoppingLists();
                                        } else {
                                            Toast.makeText(ShoppingListActivity.this, "Error al importar la lista", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            listFound = true;
                            break;
                        }
                    }
                }

                if (!listFound) {
                    Toast.makeText(ShoppingListActivity.this, "ID de lista no encontrado o no tienes permiso para importarla", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ShoppingListActivity.this, "Error al buscar la lista", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
