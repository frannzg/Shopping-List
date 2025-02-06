package com.frannzg.shopping_list;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView registerNow;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar);
        registerNow = findViewById(R.id.registerNow);

        mAuth = FirebaseAuth.getInstance();

        // Redirigir a la pantalla de registro
        registerNow.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, Register.class));
            finish();
        });

        // Botón de inicio de sesión
        btnLogin.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Validaciones
            if (email.isEmpty()) {
                emailEditText.setError("Por favor, introduce un correo válido");
                emailEditText.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                passwordEditText.setError("Por favor, introduce tu contraseña");
                passwordEditText.requestFocus();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            // Intentar iniciar sesión
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null && user.isEmailVerified()) {
                                // Si el correo está verificado, permitir el acceso
                                Toast.makeText(Login.this, "Inicio de sesión exitoso", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(Login.this, MainActivity.class));
                                finish();
                            } else {
                                // Bloquear el acceso si no está verificado
                                if (user != null) {
                                    user.sendEmailVerification(); // Reenviar correo de verificación
                                }
                                Toast.makeText(Login.this, "Debes verificar tu correo electrónico antes de iniciar sesión", Toast.LENGTH_LONG).show();
                                FirebaseAuth.getInstance().signOut(); // Cerrar sesión automáticamente
                            }
                        } else {
                            // Error en la autenticación
                            Toast.makeText(Login.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
