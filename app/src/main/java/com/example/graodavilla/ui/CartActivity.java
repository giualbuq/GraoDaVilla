package com.example.graodavilla.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.graodavilla.R;
import com.example.graodavilla.adapters.CartAdapter;
import com.example.graodavilla.models.CartItem;
import com.example.graodavilla.models.Product;
import com.example.graodavilla.repositories.CartManager;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartUpdatedListener {

    private RecyclerView recyclerCart;
    private CartAdapter adapter;
    private TextView textTotal, textEmpty;
    private Button buttonPay;
    private ImageView buttonBack;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private int mesaAtual = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerCart = findViewById(R.id.recyclerCart);
        textTotal = findViewById(R.id.textCartTotal);
        buttonPay = findViewById(R.id.buttonCartPay);
        textEmpty = findViewById(R.id.textCartEmpty);
        buttonBack = findViewById(R.id.buttonBack);

        buttonBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(this, CartManager.getInstance().getCartItems(), this);
        recyclerCart.setAdapter(adapter);

        updateTotal();
        checkCartEmpty();

        buttonPay.setOnClickListener(v -> showPaymentDialog());
    }

    private void checkCartEmpty() {
        boolean isEmpty = CartManager.getInstance().isEmpty();
        recyclerCart.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        textTotal.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        textEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        buttonPay.setEnabled(!isEmpty);
        buttonPay.setAlpha(isEmpty ? 0.5f : 1f);
    }

    private void updateTotal() {
        textTotal.setText(String.format("Total: R$ %.2f", CartManager.getInstance().getTotal()));
    }

    @Override
    public void onCartUpdated() {
        updateTotal();
        checkCartEmpty();
    }

    private void showPaymentDialog() {
        if (CartManager.getInstance().isEmpty()) {
            Toast.makeText(this, "O carrinho está vazio!", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_payment, null);

        TextView textTotalDialog = dialogView.findViewById(R.id.textTotalDialog);
        TextView textMesaNumber = dialogView.findViewById(R.id.textMesaNumber);
        Button buttonPlusMesa = dialogView.findViewById(R.id.buttonPlusMesa);
        Button buttonMinusMesa = dialogView.findViewById(R.id.buttonMinusMesa);
        Button buttonDinheiro = dialogView.findViewById(R.id.buttonDinheiro);
        Button buttonDebito = dialogView.findViewById(R.id.buttonDebito);
        Button buttonPix = dialogView.findViewById(R.id.buttonPix);

        textTotalDialog.setText(String.format("Total: R$ %.2f", CartManager.getInstance().getTotal()));

        int[] mesa = {mesaAtual};
        textMesaNumber.setText(String.valueOf(mesa[0]));

        buttonPlusMesa.setOnClickListener(v -> {
            mesa[0]++;
            textMesaNumber.setText(String.valueOf(mesa[0]));
        });

        buttonMinusMesa.setOnClickListener(v -> {
            if (mesa[0] > 1) {
                mesa[0]--;
                textMesaNumber.setText(String.valueOf(mesa[0]));
            }
        });

        final String[] pagamentoSelecionado = {"Dinheiro"};

        View.OnClickListener paymentClick = v -> {
            Button clicked = (Button) v;
            pagamentoSelecionado[0] = clicked.getText().toString();

            buttonDinheiro.setBackgroundResource(R.drawable.bg_button_payment_unselected);
            buttonDebito.setBackgroundResource(R.drawable.bg_button_payment_unselected);
            buttonPix.setBackgroundResource(R.drawable.bg_button_payment_unselected);

            clicked.setBackgroundResource(R.drawable.bg_button_payment_selected);
        };

        buttonDinheiro.setOnClickListener(paymentClick);
        buttonDebito.setOnClickListener(paymentClick);
        buttonPix.setOnClickListener(paymentClick);

        buttonDinheiro.setBackgroundResource(R.drawable.bg_button_payment_selected);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.DarkAlertDialog)
                .setTitle("Finalizar Pedido")
                .setView(dialogView)
                .setPositiveButton("Enviar", (d, which) -> {
                    String mesaStr = textMesaNumber.getText().toString().trim();
                    if (mesaStr.isEmpty()) {
                        Toast.makeText(this, "Informe o número da mesa", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sendOrder(mesaStr, pagamentoSelecionado[0]);
                })
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.show();
    }

    private void sendOrder(String mesa, String pagamento) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (CartItem cartItem : CartManager.getInstance().getCartItems()) {
            Product product = cartItem.getProduct();
            Map<String, Object> item = new HashMap<>();
            item.put("productId", product.getId());
            item.put("name", product.getName());
            item.put("price", product.getPrice());
            item.put("quantity", cartItem.getQuantity());
            items.add(item);
        }

        double total = CartManager.getInstance().getTotal();

        Map<String, Object> pedido = new HashMap<>();
        pedido.put("userId", auth.getCurrentUser().getUid());
        pedido.put("userName", auth.getCurrentUser().getDisplayName());
        pedido.put("mesa", mesa);
        pedido.put("paymentMethod", pagamento);
        pedido.put("items", items);
        pedido.put("total", total);
        pedido.put("status", "pendente");
        pedido.put("createdAt", Timestamp.now());

        db.collection("pedidos")
                .add(pedido)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Pedido enviado com sucesso!", Toast.LENGTH_SHORT).show();
                    CartManager.getInstance().clearCart();
                    adapter.notifyDataSetChanged();
                    updateTotal();
                    checkCartEmpty();
                    mesaAtual++;

                    showSuccessDialog();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao enviar pedido: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
    private void showSuccessDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_success, null);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.DarkAlertDialog)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialog.show();

        // Fecha o diálogo e volta para a MainActivity após 2 segundos
        dialogView.postDelayed(() -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }, 2000); // 2000ms = 2s
    }

}
