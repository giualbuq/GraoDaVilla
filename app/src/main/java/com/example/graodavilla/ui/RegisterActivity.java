package com.example.graodavilla.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.graodavilla.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
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

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Views
        editRegisterName = findViewById(R.id.editRegisterName);
        editRegisterEmail = findViewById(R.id.editRegisterEmail);
        editRegisterPassword = findViewById(R.id.editRegisterPassword);
        editRegisterConfirmPassword = findViewById(R.id.editRegisterConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonGoogleRegister = findViewById(R.id.buttonGoogleRegister);
        textGoToLogin = findViewById(R.id.textGoToLogin);

        configureGoogleSignIn();

        buttonRegister.setOnClickListener(v -> attemptEmailRegistration());
        buttonGoogleRegister.setOnClickListener(v -> signUpWithGoogle());
        textGoToLogin.setOnClickListener(v -> goToLoginActivity());
    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void attemptEmailRegistration() {
        String name = editRegisterName.getText().toString().trim();
        String email = editRegisterEmail.getText().toString().trim();
        String password = editRegisterPassword.getText().toString();
        String confirmPassword = editRegisterConfirmPassword.getText().toString();

        if (!validateInputs(name, email, password, confirmPassword)) return;

        showLoading(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), name, email, user);
                        }
                        Toast.makeText(RegisterActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                        // Após criar o documento, abrir MainActivity. isAdmin por padrão = false.
                        goToMainActivity(false);
                    } else {
                        String msg = task.getException() != null ? task.getException().getMessage() : "Erro no cadastro";
                        Toast.makeText(RegisterActivity.this, "Erro: " + msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInputs(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty()) {
            editRegisterName.setError("Digite seu nome completo");
            editRegisterName.requestFocus();
            return false;
        }
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
        if (!password.equals(confirmPassword)) {
            editRegisterConfirmPassword.setError("Senhas não coincidem");
            editRegisterConfirmPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void saveUserToFirestore(String uid, String name, String email, FirebaseUser firebaseUser) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("name", name != null && !name.isEmpty() ? name : (firebaseUser != null && firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Usuário"));
        userData.put("email", email);
        userData.put("photoUrl", firebaseUser != null && firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "");
        userData.put("isAdmin", false);
        userData.put("createdAt", Timestamp.now());

        db.collection("users")
                .document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // ok
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Conta criada, mas erro ao salvar perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void signUpWithGoogle() {
        showLoading(true);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Recebe resultado do intent do Google
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        showLoading(false);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                } else {
                    Toast.makeText(this, "Falha no cadastro com Google", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Erro GoogleSignIn: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        showLoading(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // se for novo usuário, task.getResult().getAdditionalUserInfo().isNewUser() pode ser verificado;
                        boolean isNewUser = false;
                        try {
                            isNewUser = task.getResult().getAdditionalUserInfo() != null && task.getResult().getAdditionalUserInfo().isNewUser();
                        } catch (Exception ignored) {}

                        if (user != null && isNewUser) {
                            // salva doc na collection users com isAdmin = false
                            saveUserToFirestore(user.getUid(),
                                    account.getDisplayName() != null ? account.getDisplayName() : "Usuário",
                                    account.getEmail(),
                                    user);
                        }

                        // Após login com Google, abrir Main (por padrão não admin)
                        goToMainActivity(false);
                    } else {
                        String msg = task.getException() != null ? task.getException().getMessage() : "Erro na autenticação com Google";
                        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                });
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

    @Override
    protected void onStart() {
        super.onStart();
        // Se já estiver logado, pular para Main
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Você pode buscar isAdmin no Firestore aqui se quiser decidir rota
            goToMainActivity(false);
        }
    }
}
