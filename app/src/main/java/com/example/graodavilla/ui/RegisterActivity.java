package com.example.graodavilla.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.graodavilla.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText editName, editEmail, editPassword, editConfirmPassword;
    private Button buttonRegister;
    private LinearLayout buttonGoogleRegister;
    private TextView textGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Referências dos campos
        editName = findViewById(R.id.editRegisterName);
        editEmail = findViewById(R.id.editRegisterEmail);
        editPassword = findViewById(R.id.editRegisterPassword);
        editConfirmPassword = findViewById(R.id.editRegisterConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonGoogleRegister = findViewById(R.id.buttonGoogleRegister);
        textGoToLogin = findViewById(R.id.textGoToLogin);

        // Botão de cadastro
        buttonRegister.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            String confirmPassword = editConfirmPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show();
            } else {
                // Simulação de cadastro bem-sucedido
                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Botão "Cadastrar com Google" (placeholder)
        buttonGoogleRegister.setOnClickListener(v ->
                Toast.makeText(this, "Cadastro com Google ainda não implementado", Toast.LENGTH_SHORT).show()
        );

        // Link para tela de login
        textGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
