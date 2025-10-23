package com.example.graodavilla.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.graodavilla.R;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class RegisterActivity extends AppCompatActivity {

    private EditText editRegisterName, editRegisterEmail, editRegisterPassword, editRegisterConfirmPassword;
    private Button buttonRegister;
    private LinearLayout buttonGoogleRegister;
    private TextView textGoToLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        configureGoogleSignIn();
        setupClickListeners();
    }

    private void initializeViews() {
        editRegisterName = findViewById(R.id.editRegisterName);
        editRegisterEmail = findViewById(R.id.editRegisterEmail);
        editRegisterPassword = findViewById(R.id.editRegisterPassword);
        editRegisterConfirmPassword = findViewById(R.id.editRegisterConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonGoogleRegister = findViewById(R.id.buttonGoogleRegister);
        textGoToLogin = findViewById(R.id.textGoToLogin);
    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupClickListeners() {
        buttonRegister.setOnClickListener(v -> attemptEmailRegistration());
        buttonGoogleRegister.setOnClickListener(v -> signUpWithGoogle());
        textGoToLogin.setOnClickListener(v -> goToLoginActivity());
    }

    private void attemptEmailRegistration() {
        String name = editRegisterName.getText().toString().trim();
        String email = editRegisterEmail.getText().toString().trim();
        String password = editRegisterPassword.getText().toString().trim();
        String confirmPassword = editRegisterConfirmPassword.getText().toString().trim();

        if (validateInputs(name, email, password, confirmPassword)) {
            registerWithEmail(name, email, password);
        }
    }

    private boolean validateInputs(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty()) { editRegisterName.setError("Digite seu nome completo"); return false; }
        if (email.isEmpty()) { editRegisterEmail.setError("Digite seu e-mail"); return false; }
        if (password.isEmpty()) { editRegisterPassword.setError("Digite uma senha"); return false; }
        if (!password.equals(confirmPassword)) { editRegisterConfirmPassword.setError("Senhas não coincidem"); return false; }
        return true;
    }

    private void registerWithEmail(String name, String email, String password) {
        showLoading(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserToFirestore(user, name, email);
                        showToast("Cadastro realizado com sucesso!");
                        goToMainActivity(false);
                    } else {
                        showToast("Erro: " + task.getException().getMessage());
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user, String name, String email) {
        if (user != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("uid", user.getUid());
            userData.put("name", name);
            userData.put("email", email);
            userData.put("createdAt", com.google.firebase.Timestamp.now());
            userData.put("isAdmin", false); // 🔹 Define padrão

            db.collection("users").document(user.getUid()).set(userData);
        }
    }

    private void signUpWithGoogle() {
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
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                showLoading(false);
                showToast("Falha no cadastro com Google");
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                            saveGoogleUserToFirestore(user, account);
                        }
                        goToMainActivity(false);
                    } else {
                        showToast("Erro no cadastro com Google");
                    }
                });
    }

    private void saveGoogleUserToFirestore(FirebaseUser user, GoogleSignInAccount account) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("name", account.getDisplayName() != null ? account.getDisplayName() : "Usuário");
        userData.put("email", account.getEmail());
        userData.put("photoUrl", account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "");
        userData.put("createdAt", com.google.firebase.Timestamp.now());
        userData.put("isAdmin", false); // 🔹 Sempre padrão
        db.collection("users").document(user.getUid()).set(userData);
    }

    private void showLoading(boolean loading) {
        buttonRegister.setEnabled(!loading);
        buttonGoogleRegister.setEnabled(!loading);
        buttonRegister.setText(loading ? "Cadastrando..." : "Cadastrar");
    }

    private void showToast(String message) {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void goToMainActivity(boolean isAdmin) {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.putExtra("isAdmin", isAdmin);
        startActivity(intent);
        finish();
    }

    private void goToLoginActivity() {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }
}
