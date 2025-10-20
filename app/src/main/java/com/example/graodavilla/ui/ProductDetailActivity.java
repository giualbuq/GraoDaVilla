package com.example.graodavilla.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.graodavilla.R;
import com.example.graodavilla.repositories.CartManager;
import com.example.graodavilla.models.CartItem;
import com.example.graodavilla.models.Product;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProductDetailActivity extends AppCompatActivity {

    private static final int REQUEST_EDIT_PRODUCT = 101;

    private ImageView imageProduct;
    private TextView textName, textDescription, textPrice, textQuantity;
    private ImageButton buttonIncrease, buttonDecrease;
    private Button buttonAddToCart;
    private ImageView buttonEdit, buttonDelete;

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

        // Preencher dados
        updateProductUI(product);

        // Incrementar quantidade
        buttonIncrease.setOnClickListener(v -> {
            quantity++;
            textQuantity.setText(String.valueOf(quantity));
        });

        // Diminuir quantidade (mínimo 1)
        buttonDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                textQuantity.setText(String.valueOf(quantity));
            }
        });

        // Adicionar ao carrinho
        buttonAddToCart.setOnClickListener(v -> {
            CartManager.getInstance().addToCart(product, quantity);
            Toast.makeText(this, product.getName() + " adicionado ao carrinho!", Toast.LENGTH_SHORT).show();
            // Volta para a MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Editar produto
        buttonEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProductActivity.class);
            intent.putExtra("product", product);
            intent.putExtra("productId", product.getId()); // garante que o EditActivity tenha o ID
            startActivityForResult(intent, REQUEST_EDIT_PRODUCT);
        });

        buttonDelete.setOnClickListener(v -> {
            if (product.getId() == null || product.getId().isEmpty()) {
                Toast.makeText(this, "Erro: ID do produto não definido", Toast.LENGTH_LONG).show();
                return;
            }

            // Material AlertDialog com estilo do app
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

    // Atualiza a UI com os dados do produto
    private void updateProductUI(Product product) {
        textName.setText(product.getName());
        textDescription.setText(product.getDescription());
        textPrice.setText("R$ " + product.getPrice());

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

    // Receber resultado do EditProductActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EDIT_PRODUCT && resultCode == RESULT_OK && data != null) {
            Product updatedProduct = (Product) data.getSerializableExtra("updatedProduct");
            if (updatedProduct != null) {
                product = updatedProduct;
                updateProductUI(product);
            }
        }
    }
}
