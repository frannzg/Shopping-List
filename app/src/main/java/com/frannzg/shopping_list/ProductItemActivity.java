package com.frannzg.shopping_list;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProductItemActivity extends AppCompatActivity {

    private TextView textViewProductName;
    private CheckBox checkBoxBought;
    private Button btnDelete;
    private DatabaseReference productRef;
    private String productId, productName, listId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_item);

        // Obtener los datos del producto desde el Intent
        productName = getIntent().getStringExtra("PRODUCT_NAME");
        productId = getIntent().getStringExtra("PRODUCT_ID");
        listId = getIntent().getStringExtra("LIST_ID");

        // Inicializar los elementos de la interfaz
        textViewProductName = findViewById(R.id.productName);
        checkBoxBought = findViewById(R.id.checkboxBought);
        btnDelete = findViewById(R.id.btnDelete);

        // Mostrar el nombre del producto
        textViewProductName.setText(productName);

        // Referencia de la base de datos para el producto
        productRef = FirebaseDatabase.getInstance().getReference("shopping_list")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(listId)
                .child("products")
                .child(productId);

        // Acción para eliminar el producto
        btnDelete.setOnClickListener(v -> deleteProduct());
    }

    private void deleteProduct() {
        productRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ProductItemActivity.this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                finish();  // Volver a la actividad anterior
            } else {
                Toast.makeText(ProductItemActivity.this, "Error al eliminar el producto", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
