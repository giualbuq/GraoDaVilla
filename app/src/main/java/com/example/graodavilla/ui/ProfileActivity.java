package com.example.graodavilla.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.graodavilla.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private Button buttonLogout;
    private TextView textName, textEmail;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        FloatingActionButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());

        // Vincular componentes
        initializeViews();

        // Carregar dados do usuário
        loadUserData();

        // Configurar botão Sair
        setupLogoutButton();
    }

    private void initializeViews() {
        buttonLogout = findViewById(R.id.buttonLogout);
        textName = findViewById(R.id.textName);
        textEmail = findViewById(R.id.textEmail);
    }

    private void loadUserData() {
        if (currentUser == null) {
            // Se não tem usuário logado, vai para login
            goToLogin();
            return;
        }

        // Primeiro, mostrar dados básicos do Authentication
        displayBasicUserInfo();

        // Depois, buscar dados adicionais no Firestore
        fetchAdditionalUserData();
    }

    private void displayBasicUserInfo() {
        // Email sempre está disponível no Authentication
        textEmail.setText(currentUser.getEmail());

        // Nome pode vir do Authentication (se login com Google) ou do Firestore
        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
            textName.setText(currentUser.getDisplayName());
        } else {
            textName.setText("User");
        }
    }

    private void fetchAdditionalUserData() {
        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Atualizar com dados do Firestore
                            updateUIWithFirestoreData(document);
                        } else {
                            // Se não existe no Firestore, criar documento
                            createUserDocumentInFirestore();
                        }
                    } else {
                        showToast("Erro ao carregar dados do usuário");
                    }
                });
    }

    private void updateUIWithFirestoreData(DocumentSnapshot document) {
        // Nome do Firestore (prioridade sobre o do Authentication)
        if (document.contains("name") && document.getString("name") != null) {
            String name = document.getString("name");
            textName.setText(name);
        }

    }

    private void createUserDocumentInFirestore() {
        // Criar documento básico do usuário no Firestore
        java.util.Map<String, Object> userData = new java.util.HashMap<>();
        userData.put("uid", currentUser.getUid());
        userData.put("email", currentUser.getEmail());
        userData.put("name", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Usuário");
        userData.put("createdAt", com.google.firebase.Timestamp.now());
        userData.put("profileCompleted", false);

        db.collection("users")
                .document(currentUser.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    showToast("Perfil criado com sucesso!");
                })
                .addOnFailureListener(e -> {
                    showToast("Erro ao criar perfil: " + e.getMessage());
                });
    }

    private void setupLogoutButton() {
        buttonLogout.setOnClickListener(v -> {
            logoutUser();
        });
    }

    private void logoutUser() {
        // Fazer logout do Firebase
        mAuth.signOut();

        showToast("Logout realizado com sucesso!");

        // Ir para tela de login
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Verificar se usuário ainda está logado
        if (mAuth.getCurrentUser() == null) {
            goToLogin();
        }
    }
}