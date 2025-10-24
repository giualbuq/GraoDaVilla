package com.example.graodavilla.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
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

public class EditProductActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 100;

    private EditText editName, editDescription, editPrice;
    private Spinner spinnerCategory;
    private ImageView imageProduct;
    private Button buttonSelectImage, buttonSave, buttonCancel;
    private ProgressBar progressBar;
    private Uri selectedImageUri;
    private Product product;
    private FirebaseFirestore db;

    private final String CLOUD_NAME = "drt7lib2z";
    private final String UPLOAD_PRESET = "graoDaVilla";

    private final String[] categorias = {"bebida quente", "bebida gelada", "doce", "salgado"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        editName = findViewById(R.id.editName);
        editDescription = findViewById(R.id.editDescription);
        editPrice = findViewById(R.id.editPrice);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        imageProduct = findViewById(R.id.imageProduct);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);
        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();

        // Configura Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categorias
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Recebe produto
        product = (Product) getIntent().getSerializableExtra("product");
        if (product == null) {
            Toast.makeText(this, "Erro: produto não encontrado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        fillFormWithProduct();

        // Frame clicável para selecionar imagem
        FrameLayout productSection = findViewById(R.id.productSection);
        productSection.setOnClickListener(v -> checkPermissionAndOpenGallery());

        buttonSave.setOnClickListener(v -> uploadImageAndSaveProduct());
        buttonCancel.setOnClickListener(v -> finish());

        FloatingActionButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());
    }


    private void fillFormWithProduct() {
        editName.setText(product.getName());
        editDescription.setText(product.getDescription());
        editPrice.setText(String.valueOf(product.getPrice()));

        // Seleciona a categoria do produto no Spinner
        int pos = 0;
        for (int i = 0; i < categorias.length; i++) {
            if (categorias[i].equalsIgnoreCase(product.getCategory())) {
                pos = i;
                break;
            }
        }
        spinnerCategory.setSelection(pos);

        Glide.with(this)
                .load(product.getImageUrl())
                .placeholder(R.drawable.defaultimage)
                .into(imageProduct);
    }

    private void checkPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
            } else openGallery();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            } else openGallery();
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imageProduct.setImageURI(selectedImageUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) openGallery();
            else Toast.makeText(this, "Permissão negada para acessar a galeria", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageAndSaveProduct() {
        progressBar.setVisibility(android.view.View.VISIBLE);
        buttonSave.setEnabled(false);
        buttonSave.setText("Enviando...");

        if (selectedImageUri != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                if (inputStream == null) throw new IOException("Não foi possível abrir a imagem");

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
                            Toast.makeText(EditProductActivity.this, "Erro ao enviar imagem: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            resetUploadState();
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                String json = response.body().string();
                                String imageUrl = new JSONObject(json).getString("secure_url");
                                runOnUiThread(() -> saveProduct(imageUrl));
                            } else {
                                runOnUiThread(() -> {
                                    Toast.makeText(EditProductActivity.this, "Erro ao enviar imagem: " + response.message(), Toast.LENGTH_LONG).show();
                                    resetUploadState();
                                });
                            }
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(EditProductActivity.this, "Erro ao processar resposta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                resetUploadState();
                            });
                        }
                    }

                });

            } catch (Exception e) {
                Toast.makeText(this, "Erro ao ler a imagem: " + e.getMessage(), Toast.LENGTH_LONG).show();
                resetUploadState();
            }
        } else {
            saveProduct(product.getImageUrl());
        }
    }

    private void resetUploadState() {
        progressBar.setVisibility(android.view.View.GONE);
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
        try { price = Double.parseDouble(priceStr); }
        catch (NumberFormatException e) {
            Toast.makeText(this, "Preço inválido", Toast.LENGTH_SHORT).show();
            resetUploadState();
            return;
        }

        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        product.setImageUrl(imageUrl);

        if (product.getId() == null || product.getId().isEmpty()) {
            Toast.makeText(this, "Erro: ID do produto não definido", Toast.LENGTH_LONG).show();
            resetUploadState();
            return;
        }

        db.collection("products")
                .document(product.getId())
                .set(product)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProductActivity.this, "Produto atualizado!", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updatedProduct", product);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProductActivity.this, "Erro ao atualizar produto: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    resetUploadState();
                });
    }
}
