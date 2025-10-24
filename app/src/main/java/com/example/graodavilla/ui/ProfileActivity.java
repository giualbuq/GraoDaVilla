package com.example.graodavilla.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.graodavilla.R;
import com.example.graodavilla.adapters.PedidosAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private Button buttonLogout;
    private TextView textName, textEmail;
    private RecyclerView recyclerPedidos;
    private ProgressBar progressPedidos;
    private TextView textPedidosEmpty;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private PedidosAdapter pedidosAdapter;
    private List<Map<String, Object>> pedidosList = new ArrayList<>();

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

        // Configurar RecyclerView de pedidos
        recyclerPedidos.setLayoutManager(new LinearLayoutManager(this));
        pedidosAdapter = new PedidosAdapter(this, pedidosList);
        recyclerPedidos.setAdapter(pedidosAdapter);

        // Carregar dados do usuário
        loadUserData();

        // Carregar pedidos
        loadPedidosUsuario();

        // Configurar botão Sair
        setupLogoutButton();
    }

    private void initializeViews() {
        buttonLogout = findViewById(R.id.buttonLogout);
        textName = findViewById(R.id.textName);
        textEmail = findViewById(R.id.textEmail);
        recyclerPedidos = findViewById(R.id.recyclerPedidos);
        progressPedidos = findViewById(R.id.progressPedidos);
        textPedidosEmpty = findViewById(R.id.textPedidosEmpty);
    }

    private void loadUserData() {
        if (currentUser == null) {
            goToLogin();
            return;
        }

        displayBasicUserInfo();
        fetchAdditionalUserData();
    }

    private void displayBasicUserInfo() {
        textEmail.setText(currentUser.getEmail());
        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
            textName.setText(currentUser.getDisplayName());
        } else {
            textName.setText("Usuário");
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
                            updateUIWithFirestoreData(document);
                        } else {
                            createUserDocumentInFirestore();
                        }
                    } else {
                        showToast("Erro ao carregar dados do usuário");
                    }
                });
    }

    private void updateUIWithFirestoreData(DocumentSnapshot document) {
        if (document.contains("name") && document.getString("name") != null) {
            textName.setText(document.getString("name"));
        }
    }

    private void createUserDocumentInFirestore() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", currentUser.getUid());
        userData.put("email", currentUser.getEmail());
        userData.put("name", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Usuário");
        userData.put("createdAt", Timestamp.now());
        userData.put("profileCompleted", false);

        db.collection("users")
                .document(currentUser.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> showToast("Perfil criado com sucesso!"))
                .addOnFailureListener(e -> showToast("Erro ao criar perfil: " + e.getMessage()));
    }

    private void setupLogoutButton() {
        buttonLogout.setOnClickListener(v -> logoutUser());
    }

    private void logoutUser() {
        mAuth.signOut();
        showToast("Logout realizado com sucesso!");

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadPedidosUsuario() {
        if (currentUser == null) return;

        progressPedidos.setVisibility(View.VISIBLE);
        textPedidosEmpty.setVisibility(View.GONE);

        db.collection("pedidos")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    pedidosList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Map<String, Object> pedido = doc.getData();
                        pedido.put("id", doc.getId());
                        pedidosList.add(pedido);
                    }

                    pedidosAdapter.notifyDataSetChanged();

                    progressPedidos.setVisibility(View.GONE);
                    textPedidosEmpty.setVisibility(pedidosList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressPedidos.setVisibility(View.GONE);
                    textPedidosEmpty.setVisibility(View.VISIBLE);
                    textPedidosEmpty.setText("Erro ao carregar pedidos: " + e.getMessage());
                });
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
        if (mAuth.getCurrentUser() == null) {
            goToLogin();
        }
    }
}
