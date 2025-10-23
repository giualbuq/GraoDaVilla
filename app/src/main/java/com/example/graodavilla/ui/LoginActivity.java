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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button buttonLogin;
    private LinearLayout buttonGoogleLogin;
    private TextView textRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        configureGoogleSignIn();
        setupClickListeners();
    }

    private void initializeViews() {
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonGoogleLogin = findViewById(R.id.buttonGoogleLogin);
        textRegister = findViewById(R.id.textRegister);
    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupClickListeners() {
        buttonLogin.setOnClickListener(view -> attemptEmailLogin());
        buttonGoogleLogin.setOnClickListener(v -> signInWithGoogle());
        textRegister.setOnClickListener(view -> goToRegisterActivity());
    }

    private void attemptEmailLogin() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (validateInputs(email, password)) {
            loginWithEmail(email, password);
        }
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            editEmail.setError("Digite seu e-mail");
            return false;
        }
        if (password.isEmpty()) {
            editPassword.setError("Digite sua senha");
            return false;
        }
        return true;
    }

    private void loginWithEmail(String email, String password) {
        showLoading(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        fetchUserRole(user);
                    } else {
                        showToast("Erro ao fazer login: " + task.getException().getMessage());
                    }
                });
    }

    private void signInWithGoogle() {
        showLoading(true);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                showLoading(false);
                showToast("Falha no login com Google: " + e.getStatusCode());
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        checkOrCreateUserInFirestore(user);
                    } else {
                        showToast("Falha na autenticação com Google");
                    }
                });
    }

    private void checkOrCreateUserInFirestore(FirebaseUser user) {
        if (user == null) return;

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        boolean isAdmin = document.getBoolean("isAdmin") != null && document.getBoolean("isAdmin");
                        goToMainActivity(isAdmin);
                    } else {
                        createNewUserDocument(user);
                    }
                })
                .addOnFailureListener(e -> showToast("Erro ao verificar usuário"));
    }

    private void createNewUserDocument(FirebaseUser user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getDisplayName() != null ? user.getDisplayName() : "Usuário Google");
        userData.put("email", user.getEmail());
        userData.put("isAdmin", false);
        userData.put("createdAt", System.currentTimeMillis());

        db.collection("users")
                .document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    showToast("Conta criada com sucesso!");
                    goToMainActivity(false);
                })
                .addOnFailureListener(e -> showToast("Erro ao criar usuário no Firestore"));
    }

    private void fetchUserRole(FirebaseUser user) {
        if (user == null) return;

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        boolean isAdmin = document.getBoolean("isAdmin") != null && document.getBoolean("isAdmin");
                        goToMainActivity(isAdmin);
                    } else {
                        // Usuário de e-mail/senha sem documento (raro, mas cobre esse caso)
                        createNewUserDocument(user);
                    }
                })
                .addOnFailureListener(e -> showToast("Erro ao buscar dados do usuário"));
    }

    private void showLoading(boolean loading) {
        buttonLogin.setEnabled(!loading);
        buttonGoogleLogin.setEnabled(!loading);
        textRegister.setEnabled(!loading);
        buttonLogin.setText(loading ? "Entrando..." : "Entrar");
    }

    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void goToMainActivity(boolean isAdmin) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("isAdmin", isAdmin);
        startActivity(intent);
        finish();
    }

    private void goToRegisterActivity() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            fetchUserRole(currentUser);
        }
    }
}
