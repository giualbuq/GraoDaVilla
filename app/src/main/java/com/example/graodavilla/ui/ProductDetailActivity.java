package com.example.graodavilla.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.graodavilla.R;
import com.example.graodavilla.repositories.CartManager;
import com.example.graodavilla.models.Product;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private static final int REQUEST_EDIT_PRODUCT = 101;

    private ImageView imageProduct, buttonEdit, buttonDelete;
    private TextView textName, textDescription, textPrice, textQuantity, textTotal;
    private ImageView buttonIncrease, buttonDecrease;
    private Button buttonAddToCart;

    private FirebaseFirestore db;
    private Product product;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        FloatingActionButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());

        // findViewById
        imageProduct = findViewById(R.id.imageProductDetail);
        textName = findViewById(R.id.textNameDetail);
        textDescription = findViewById(R.id.textDescriptionDetail);
        textPrice = findViewById(R.id.textPriceDetail);
        textQuantity = findViewById(R.id.textQuantity);
        textTotal = findViewById(R.id.textTotal);
        buttonIncrease = findViewById(R.id.buttonIncrease);
        buttonDecrease = findViewById(R.id.buttonDecrease);
        buttonAddToCart = findViewById(R.id.buttonAddToCart);
        buttonEdit = findViewById(R.id.buttonEditProduct);
        buttonDelete = findViewById(R.id.buttonDeleteProduct);

        db = FirebaseFirestore.getInstance();

        // Receber o produto
        product = (Product) getIntent().getSerializableExtra("product");

        if (product == null) {
            Toast.makeText(this, "Erro: produto não encontrado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Atualiza UI
        updateProductUI(product);
        updateTotal();

        // Carregar se o usuário é admin e ajustar visibilidade dos botões
        checkAdminStatus();

        // Incrementar quantidade
        buttonIncrease.setOnClickListener(v -> {
            quantity++;
            textQuantity.setText(String.valueOf(quantity));
            updateTotal();
        });

        // Diminuir quantidade
        buttonDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                textQuantity.setText(String.valueOf(quantity));
                updateTotal();
            }
        });

        // Adicionar ao carrinho
        buttonAddToCart.setOnClickListener(v -> {
            CartManager.getInstance().addToCart(product, quantity);
            Toast.makeText(this, product.getName() + " adicionado ao carrinho!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // Editar produto
        buttonEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProductActivity.class);
            intent.putExtra("product", product);
            intent.putExtra("productId", product.getId());
            startActivityForResult(intent, REQUEST_EDIT_PRODUCT);
        });

        // Excluir produto
        buttonDelete.setOnClickListener(v -> {
            if (product.getId() == null || product.getId().isEmpty()) {
                Toast.makeText(this, "Erro: ID do produto não definido", Toast.LENGTH_LONG).show();
                return;
            }

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Excluir produto")
                    .setMessage("Tem certeza que deseja excluir \"" + product.getName() + "\"?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        db.collection("products").document(product.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Produto excluído!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Erro ao excluir: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    })
                    .setNegativeButton("Não", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void checkAdminStatus() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Boolean isAdmin = snapshot.getBoolean("isAdmin");
                        if (isAdmin != null && isAdmin) {
                            // Usuário é admin → mostra botões
                            buttonEdit.setVisibility(View.VISIBLE);
                            buttonDelete.setVisibility(View.VISIBLE);
                        } else {
                            // Usuário não é admin → oculta botões
                            buttonEdit.setVisibility(View.GONE);
                            buttonDelete.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao verificar permissões", Toast.LENGTH_SHORT).show());
    }

    private void updateProductUI(Product product) {
        textName.setText(product.getName());
        textDescription.setText(product.getDescription());

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        textPrice.setText(format.format(product.getPrice()));

        String url = product.getImageUrl();
        if (url != null && !url.isEmpty()) {
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.defaultimage)
                    .into(imageProduct);
        } else {
            imageProduct.setImageResource(R.drawable.defaultimage);
        }

        textQuantity.setText(String.valueOf(quantity));
    }

    private void updateTotal() {
        double total = product.getPrice() * quantity;
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        textTotal.setText("Total: " + format.format(total));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EDIT_PRODUCT && resultCode == RESULT_OK && data != null) {
            Product updatedProduct = (Product) data.getSerializableExtra("updatedProduct");
            if (updatedProduct != null) {
                product = updatedProduct;
                updateProductUI(product);
                updateTotal();
            }
        }
    }
}
