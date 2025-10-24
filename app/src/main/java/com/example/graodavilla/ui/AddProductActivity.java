package com.example.graodavilla.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.graodavilla.R;
import com.example.graodavilla.models.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddProductActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 100;

    private EditText editName, editDescription, editPrice;
    private Spinner spinnerCategory;
    private ImageView imageProduct;
    private Button buttonSelectImage, buttonSave, buttonCancel;
    private ProgressBar progressBar;
    private Uri selectedImageUri;

    private FirebaseFirestore db;

    // Cloudinary
    private final String CLOUD_NAME = "drt7lib2z";
    private final String UPLOAD_PRESET = "graoDaVilla";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        editName = findViewById(R.id.editName);
        editDescription = findViewById(R.id.editDescription);
        editPrice = findViewById(R.id.editPrice);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        imageProduct = findViewById(R.id.addImageProduct); // novo ImageView do FrameLayout
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);
        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();

        // Configura categorias do Spinner
        String[] categorias = {"bebida quente", "bebida gelada", "doce", "salgado"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item_dark, // layout customizado
                categorias
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark);
        spinnerCategory.setAdapter(adapter);

        // Frame clicável para selecionar imagem
        FrameLayout addProductSection = findViewById(R.id.addProductSection);
        addProductSection.setOnClickListener(v -> checkPermissionAndOpenGallery());

        buttonSave.setOnClickListener(v -> uploadImageToCloudinary());
        buttonCancel.setOnClickListener(v -> finish());

        FloatingActionButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());
    }


    private void checkPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            } else {
                openGallery();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imageProduct.setImageURI(selectedImageUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permissão negada para acessar a galeria", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToCloudinary() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Selecione uma imagem primeiro", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        buttonSave.setEnabled(false);
        buttonSave.setText("Enviando...");

        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            if (inputStream == null) {
                Toast.makeText(this, "Não foi possível abrir a imagem", Toast.LENGTH_SHORT).show();
                resetUploadState();
                return;
            }

            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                            "file",
                            "image_" + System.currentTimeMillis() + ".jpg",
                            RequestBody.create(bytes, MediaType.parse("image/*"))
                    )
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddProductActivity.this, "Erro ao enviar imagem", Toast.LENGTH_SHORT).show();
                        resetUploadState();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            String json = response.body().string();
                            String imageUrl = new JSONObject(json).getString("secure_url");
                            runOnUiThread(() -> saveProduct(imageUrl));
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(AddProductActivity.this, "Erro ao enviar imagem", Toast.LENGTH_SHORT).show();
                                resetUploadState();
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(AddProductActivity.this, "Erro ao processar resposta", Toast.LENGTH_SHORT).show();
                            resetUploadState();
                        });
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao ler a imagem", Toast.LENGTH_SHORT).show();
            resetUploadState();
        }
    }

    private void resetUploadState() {
        progressBar.setVisibility(View.GONE);
        buttonSave.setEnabled(true);
        buttonSave.setText("Salvar");
    }

    private void saveProduct(String imageUrl) {
        String name = editName.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String priceStr = editPrice.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            resetUploadState();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Preço inválido", Toast.LENGTH_SHORT).show();
            resetUploadState();
            return;
        }

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setImageUrl(imageUrl);
        product.setCategory(category);

        db.collection("products")
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    product.setId(documentReference.getId());
                    Toast.makeText(AddProductActivity.this, "Produto adicionado!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddProductActivity.this, "Erro ao adicionar produto: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    resetUploadState();
                });
    }
}
