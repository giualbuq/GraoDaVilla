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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Vincular componentes
        initializeViews();

        // Configurar Google Sign In
        configureGoogleSignIn();

        // Configurar listeners
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
        // Botão Cadastrar com Email/Senha
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptEmailRegistration();
            }
        });

        // Botão Cadastrar com Google
        buttonGoogleRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpWithGoogle();
            }
        });

        // Link para Login
        textGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLoginActivity();
            }
        });
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
        // Validar nome
        if (name.isEmpty()) {
            editRegisterName.setError("Digite seu nome completo");
            editRegisterName.requestFocus();
            return false;
        }

        if (name.length() < 2) {
            editRegisterName.setError("Nome deve ter pelo menos 2 caracteres");
            editRegisterName.requestFocus();
            return false;
        }

        // Validar email
        if (email.isEmpty()) {
            editRegisterEmail.setError("Digite seu e-mail");
            editRegisterEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editRegisterEmail.setError("Digite um e-mail válido");
            editRegisterEmail.requestFocus();
            return false;
        }

        // Validar senha
        if (password.isEmpty()) {
            editRegisterPassword.setError("Digite uma senha");
            editRegisterPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            editRegisterPassword.setError("Senha deve ter pelo menos 6 caracteres");
            editRegisterPassword.requestFocus();
            return false;
        }

        // Validar confirmação de senha
        if (confirmPassword.isEmpty()) {
            editRegisterConfirmPassword.setError("Confirme sua senha");
            editRegisterConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            editRegisterConfirmPassword.setError("Senhas não coincidem");
            editRegisterConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void registerWithEmail(String name, String email, String password) {
        showLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Cadastro bem-sucedido
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserToFirestore(user, name, email);
                        showToast("Cadastro realizado com sucesso!");
                        goToMainActivity();
                    } else {
                        showLoading(false);
                        String errorMessage = getErrorMessage(task.getException());
                        showToast(errorMessage);
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user, String name, String email) {
        if (user != null) {
            // Criar objeto do usuário
            Map<String, Object> userData = new HashMap<>();
            userData.put("uid", user.getUid());
            userData.put("name", name);
            userData.put("email", email);
            userData.put("createdAt", com.google.firebase.Timestamp.now());
            userData.put("profileCompleted", false);

            // Salvar no Firestore
            db.collection("users")
                    .document(user.getUid())
                    .set(userData)
                    .addOnSuccessListener(aVoid -> {
                        showLoading(false);
                        showToast("Perfil criado com sucesso!");
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        showToast("Conta criada, mas erro ao salvar perfil: " + e.getMessage());
                    });
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
                showToast("Falha no cadastro com Google: " + e.getStatusCode());
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Verificar se é um novo usuário
                        if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                            saveGoogleUserToFirestore(user, account);
                            showToast("Cadastro com Google realizado!");
                        } else {
                            showToast("Login com Google realizado!");
                        }
                        goToMainActivity();
                    } else {
                        showLoading(false);
                        showToast("Falha no cadastro com Google");
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
        userData.put("profileCompleted", false);
        userData.put("isGoogleUser", true);

        db.collection("users")
                .document(user.getUid())
                .set(userData)
                .addOnFailureListener(e -> {
                    showToast("Conta criada, mas erro ao salvar perfil no Firestore");
                });
    }

    private String getErrorMessage(Exception exception) {
        if (exception == null) return "Erro desconhecido";

        String error = exception.getMessage();
        if (error.contains("email address is already")) {
            return "E-mail já está em uso";
        } else if (error.contains("password is weak")) {
            return "Senha muito fraca";
        } else if (error.contains("network error")) {
            return "Erro de conexão. Verifique sua internet";
        } else if (error.contains("invalid email")) {
            return "E-mail inválido";
        } else {
            return "Erro: " + error;
        }
    }

    private void showLoading(boolean loading) {
        buttonRegister.setEnabled(!loading);
        buttonGoogleRegister.setEnabled(!loading);
        textGoToLogin.setEnabled(!loading);

        if (loading) {
            buttonRegister.setText("Cadastrando...");
        } else {
            buttonRegister.setText("Cadastrar");
        }
    }

    private void showToast(String message) {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Verificar se usuário já está logado (caso venha do login)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            goToMainActivity();
        }
    }
}