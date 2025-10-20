package com.example.graodavilla.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.example.graodavilla.R;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button buttonLogin, buttonGoogleLogin;
    private TextView textRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textRegister = findViewById(R.id.textRegister);

        // Botão Entrar
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editEmail.getText().toString().trim();
                String password = editPassword.getText().toString().trim();

                if(email.isEmpty() || password.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                } else {
                    // Navega para MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        // Botão Google (placeholder)
        findViewById(R.id.buttonGoogleLogin).setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Login com Google ainda não implementado", Toast.LENGTH_SHORT).show();
        });


        // Link Cadastrar
        textRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}