package com.frannzg.shopping_list;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private TextView userDetails;
    private Button btnLogout;
    private Button goToShoppingListButton; // Botón para ir a la lista de compras

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        userDetails = findViewById(R.id.user_details);
        btnLogout = findViewById(R.id.logout);
        goToShoppingListButton = findViewById(R.id.goToShoppingList); // Inicializar el botón

        // Mostrar detalles del usuario
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            userDetails.setText("Bienvenido, " + mAuth.getCurrentUser().getEmail());
        }

        // Acción de logout
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        });

        // Acción de ir a la lista de compras
        goToShoppingListButton.setOnClickListener(v -> {
            // Iniciar ShoppingListActivity
            startActivity(new Intent(MainActivity.this, ShoppingListActivity.class));
        });
    }
}
