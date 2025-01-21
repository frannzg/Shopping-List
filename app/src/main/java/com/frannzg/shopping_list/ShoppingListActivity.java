package com.frannzg.shopping_list;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;

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

        // Usar el layout personalizado para la lista
        adapter = new ArrayAdapter<>(this, R.layout.list_item_white_text, shoppingListNames);
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
            showOptionsDialog(selectedListId, position); // Mostrar las opciones de la lista seleccionada
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

    private void showOptionsDialog(String listId, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opciones de lista");

        // Opciones del dialogo
        builder.setItems(new CharSequence[]{"Editar", "Eliminar", "Compartir por WhatsApp", "Atrás"},
                (dialog, which) -> {
                    switch (which) {
                        case 0: // Editar
                            // Redirigir a la actividad para gestionar los productos de la lista
                            Intent intent = new Intent(ShoppingListActivity.this, ManageProductsActivity.class);
                            intent.putExtra("LIST_ID", listId); // Pasar el ID de la lista
                            startActivity(intent);
                            break;
                        case 1: // Eliminar
                            deleteShoppingList(listId, position);
                            break;
                        case 2: // Compartir
                            shareShoppingList(listId);
                            break;
                        case 3: // Atrás
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

    // Compartir llistes

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            String listId = data.getLastPathSegment();
            loadShoppingList(listId);
        }
    }

    private void loadShoppingList(String listId) {
        DatabaseReference listRef = FirebaseDatabase.getInstance().getReference("shopping_list").child(listId);
        listRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String listName = dataSnapshot.child("name").getValue(String.class);
                    // Cargar los productos de la lista y actualizar la interfaz de usuario
                } else {
                    Toast.makeText(ShoppingListActivity.this, "Lista no encontrada", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ShoppingListActivity.this, "Error al cargar la lista", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareShoppingList(String listId) {
        // Obtener el nombre de la lista desde Firebase
        shoppingListRef.child(mAuth.getCurrentUser().getUid()).child(listId).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String listName = dataSnapshot.getValue(String.class);
                            String shareText = "Lista de compra: " + listName + "\n\n¡Compra lo necesario!\n" +
                                    "Para ver la lista, haz clic en el siguiente enlace: https://shoppinglist.com/lista/" + listId;

                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_TEXT, shareText);
                            intent.setPackage("com.whatsapp"); // Especificar WhatsApp
                            startActivity(Intent.createChooser(intent, "Compartir lista por"));
                        } else {
                            Toast.makeText(ShoppingListActivity.this, "Lista no encontrada", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(ShoppingListActivity.this, "Error al cargar el nombre de la lista", Toast.LENGTH_SHORT).show();
                    }
                });
    }





}
