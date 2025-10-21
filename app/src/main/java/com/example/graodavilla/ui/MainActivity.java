package com.example.graodavilla.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.graodavilla.R;
import com.example.graodavilla.adapters.ProductAdapter;
import com.example.graodavilla.models.Product;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PRODUCT_DETAIL = 201;

    private RecyclerView recyclerHotDrinks, recyclerColdDrinks ,recyclerSnacks, recyclerDesserts;
    private ProductAdapter adapterHot, adapterCold, adapterSnacks, adapterDesserts;
    private List<Product> hotDrinks = new ArrayList<>();
    private List<Product> coldDrinks = new ArrayList<>();
    private List<Product> snacks = new ArrayList<>();
    private List<Product> desserts = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        recyclerHotDrinks = findViewById(R.id.recyclerHotDrinks);
        recyclerColdDrinks = findViewById(R.id.recyclerColdDrinks);
        recyclerSnacks = findViewById(R.id.recyclerSnacks);
        recyclerDesserts = findViewById(R.id.recyclerDesserts);

        adapterHot = new ProductAdapter(this, hotDrinks, this::openProductDetail);
        adapterCold = new ProductAdapter(this, coldDrinks, this::openProductDetail);
        adapterSnacks = new ProductAdapter(this, snacks, this::openProductDetail);
        adapterDesserts = new ProductAdapter(this, desserts, this::openProductDetail);

        setupRecycler(recyclerHotDrinks, adapterHot);
        setupRecycler(recyclerColdDrinks, adapterCold);
        setupRecycler(recyclerSnacks, adapterSnacks);
        setupRecycler(recyclerDesserts, adapterDesserts);

        loadProducts();

        ImageView iconCart = findViewById(R.id.iconCart);

        iconCart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CartActivity.class);
            startActivity(intent);
        });

        ImageView iconUser = findViewById(R.id.iconUser);

        iconUser.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

    }

    private void setupRecycler(RecyclerView recyclerView, ProductAdapter adapter) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
    }

    private void loadProducts() {
        db.collection("products")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Erro ao carregar produtos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    hotDrinks.clear();
                    coldDrinks.clear();
                    snacks.clear();
                    desserts.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        Product p = doc.toObject(Product.class);
                        p.setId(doc.getId());

                        switch (p.getCategory()) {
                            case "bebida quente":
                                hotDrinks.add(p);
                                break;
                            case "bebida gelada":
                                coldDrinks.add(p);
                                break;
                            case "salgado":
                                snacks.add(p);
                                break;
                            case "doce":
                                desserts.add(p);
                                break;
                        }
                    }

                    adapterHot.notifyDataSetChanged();
                    adapterCold.notifyDataSetChanged();
                    adapterSnacks.notifyDataSetChanged();
                    adapterDesserts.notifyDataSetChanged();
                });
    }

    // Abrir detalhes do produto
    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product", product);
        startActivityForResult(intent, REQUEST_PRODUCT_DETAIL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PRODUCT_DETAIL && resultCode == RESULT_OK && data != null) {
            Product updatedProduct = (Product) data.getSerializableExtra("updatedProduct");
            if (updatedProduct != null) {
                // Atualiza o produto na lista correta
                updateProductInList(updatedProduct, hotDrinks, adapterHot);
                updateProductInList(updatedProduct, coldDrinks, adapterCold);
                updateProductInList(updatedProduct, snacks, adapterSnacks);
                updateProductInList(updatedProduct, desserts, adapterDesserts);
            }
        }
    }

    // Atualiza o produto em uma lista específica
    private void updateProductInList(Product updatedProduct, List<Product> list, ProductAdapter adapter) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(updatedProduct.getId())) {
                list.set(i, updatedProduct);
                adapter.notifyItemChanged(i);
                return;
            }
        }
    }
}
