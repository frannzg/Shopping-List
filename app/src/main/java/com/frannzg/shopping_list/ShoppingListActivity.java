package com.frannzg.shopping_list;

import android.content.Context;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ShoppingListActivity extends AppCompatActivity {

    private Button btnCreateList, btnImportList, btnExportList;
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
        btnExportList = findViewById(R.id.btnExportList);
        listViewShoppingLists = findViewById(R.id.listViewShoppingLists);
        shoppingListNames = new ArrayList<>();
        shoppingListIds = new ArrayList<>();

        // Configurar el adaptador para mostrar las listas en la ListView
        adapter = new ArrayAdapter<>(this, R.layout.list_item_white_text, shoppingListNames);
        listViewShoppingLists.setAdapter(adapter);

        // Inicializar Firebase Auth y la referencia a la base de datos
        mAuth = FirebaseAuth.getInstance();
        shoppingListRef = FirebaseDatabase.getInstance().getReference("shopping_list");

        // Acción del botón para crear una nueva lista
        btnCreateList.setOnClickListener(v -> showCreateListDialog());

        // Acción del botón para exportar listas
        btnExportList.setOnClickListener(v -> {
            if (shoppingListIds.isEmpty()) {
                Toast.makeText(ShoppingListActivity.this, "No hay listas para exportar", Toast.LENGTH_SHORT).show();
            } else {
                showSelectListDialog();
            }
        });

        // Cargar listas existentes desde Firebase
        loadShoppingLists();

        // Configurar clic en las listas para mostrar opciones
        listViewShoppingLists.setOnItemClickListener((parent, view, position, id) -> {
            String selectedListId = shoppingListIds.get(position);
            showOptionsDialog(selectedListId, shoppingListNames.get(position), position);
        });
    }

    /**
     * Carga las listas de compras desde Firebase para el usuario actual.
     */
    private void loadShoppingLists() {
        shoppingListNames.clear();
        shoppingListIds.clear();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Por favor inicia sesión para ver tus listas", Toast.LENGTH_SHORT).show();
            return;
        }

        // Referencia al nodo del usuario actual en Firebase
        shoppingListRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String listName = snapshot.child("name").getValue(String.class);
                        String listId = snapshot.getKey();

                        // Verificar que los datos no sean nulos
                        if (listName != null && listId != null) {
                            shoppingListNames.add(listName);
                            shoppingListIds.add(listId);
                        } else {
                            Log.e("ShoppingListActivity", "Lista inválida en Firebase: " + snapshot.toString());
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ShoppingListActivity.this, "No tienes listas de compra", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ShoppingListActivity", "Error al cargar las listas: " + databaseError.getMessage());
                Toast.makeText(ShoppingListActivity.this, "Error al cargar las listas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Muestra un cuadro de diálogo para crear una nueva lista de compras.
     */
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

    /**
     * Crea una nueva lista de compras y la guarda en Firebase.
     */
    private void createNewShoppingList(String listName) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String listId = shoppingListRef.child(userId).push().getKey();

        if (listId != null) {
            shoppingListRef.child(userId).child(listId).child("name").setValue(listName)
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

    /**
     * Muestra un cuadro de diálogo con opciones para la lista seleccionada.
     */
    private void showOptionsDialog(String listId, String listName, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opciones de lista: " + listName);

        builder.setItems(new CharSequence[]{"Ver productos", "Eliminar lista", "Atrás"},
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // Ir a la actividad de productos
                            Intent intent1 = new Intent(ShoppingListActivity.this, ManageProductsActivity.class);
                            intent1.putExtra("LIST_ID", listId);
                            startActivity(intent1);
                            break;
                        case 1:
                            // Eliminar la lista seleccionada
                            deleteShoppingList(listId, position);
                            break;
                        case 2:
                            dialog.dismiss();
                            break;
                    }
                });

        builder.show();
    }

    /**
     * Elimina una lista de compras de Firebase.
     */
    private void deleteShoppingList(String listId, int position) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        shoppingListRef.child(userId).child(listId).removeValue()
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

    /* *************************************************************** FASE EN BETA ***********************************************************************/
    /**
     * Muestra un cuadro de diálogo para seleccionar una lista y exportarla a otro usuario.
     */
    private void showSelectListDialog() {
        ArrayList<String> listNames = new ArrayList<>();
        ArrayList<String> listIds = new ArrayList<>();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        shoppingListRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot listSnapshot : dataSnapshot.getChildren()) {
                        String listName = listSnapshot.child("name").getValue(String.class);
                        String listId = listSnapshot.getKey();

                        if (listName != null && listId != null) {
                            listNames.add(listName);
                            listIds.add(listId);
                        }
                    }

                    if (!listNames.isEmpty()) {
                        showListSelectionDialog(listNames, listIds);
                    } else {
                        Toast.makeText(ShoppingListActivity.this, "No tienes listas para compartir", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ShoppingListActivity.this, "No se encontraron listas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ShoppingListActivity", "Error al cargar listas: " + databaseError.getMessage());
                Toast.makeText(ShoppingListActivity.this, "Error al cargar listas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Muestra un cuadro de diálogo para seleccionar una lista específica.
     */
    private void showListSelectionDialog(ArrayList<String> listNames, ArrayList<String> listIds) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona una lista");

        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listNames);

        builder.setAdapter(listAdapter, (dialog, which) -> {
            String selectedListId = listIds.get(which);
            String selectedListName = listNames.get(which);

            showSelectUserDialog(selectedListId, selectedListName);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /**
     * Muestra un cuadro de diálogo para seleccionar un usuario al cual exportar la lista.
     */
    private void showSelectUserDialog(String listaaa, String email) {
        getAllUsersJson();
    }

    private void getAllUsersJson() {
        // Crear un AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¡ FASE EN ALPHA !"); // Título del diálogo
        builder.setMessage("La fase de compartir una lista a un usuario está en Alfa."); // Mensaje
        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            // Acción al hacer clic en "Aceptar"
            dialog.dismiss(); // Cierra el diálogo
        });

        // Mostrar el AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
