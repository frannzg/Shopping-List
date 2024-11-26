package com.frannzg.shopping_list;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditListActivity extends AppCompatActivity {

    private EditText editTextListName;
    private Button btnSaveList;
    private String listId;
    private DatabaseReference listRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_list);

        // Inicializar elementos de la interfaz
        editTextListName = findViewById(R.id.editTextListName);
        btnSaveList = findViewById(R.id.btnSaveList);

        // Obtener el ID de la lista desde el Intent
        listId = getIntent().getStringExtra("LIST_ID");

        // Referencia a la base de datos de Firebase
        listRef = FirebaseDatabase.getInstance().getReference("shopping_list")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(listId);

        // Obtener el nombre de la lista desde la base de datos
        loadListName();

        // Acción para guardar los cambios en el nombre de la lista
        btnSaveList.setOnClickListener(v -> saveListName());
    }

    private void loadListName() {
        // Cargar el nombre de la lista desde la base de datos (se asume que ya existe en Firebase)
        listRef.child("name").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String listName = task.getResult().getValue(String.class);
                if (listName != null) {
                    editTextListName.setText(listName);
                }
            } else {
                Toast.makeText(EditListActivity.this, "Error al cargar el nombre de la lista", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveListName() {
        String newListName = editTextListName.getText().toString().trim();
        if (!newListName.isEmpty()) {
            // Actualizar el nombre de la lista en la base de datos de Firebase
            listRef.child("name").setValue(newListName).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(EditListActivity.this, "Nombre de la lista actualizado", Toast.LENGTH_SHORT).show();
                    finish(); // Finaliza la actividad y regresa a la anterior
                } else {
                    Toast.makeText(EditListActivity.this, "Error al actualizar el nombre", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(EditListActivity.this, "Introduce un nombre válido", Toast.LENGTH_SHORT).show();
        }
    }
}