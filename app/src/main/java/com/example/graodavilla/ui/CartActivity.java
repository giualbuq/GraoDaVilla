package com.example.graodavilla.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.graodavilla.R;
import com.example.graodavilla.adapters.CartAdapter;
import com.example.graodavilla.repositories.CartManager;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartUpdatedListener {

    private TextView textTotal;
    private CartAdapter adapter;
    private RecyclerView recyclerCart;
    private Button buttonPay;
    private TextView textEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerCart = findViewById(R.id.recyclerCart);
        textTotal = findViewById(R.id.textCartTotal);
        buttonPay = findViewById(R.id.buttonCartPay);
        textEmpty = findViewById(R.id.textCartEmpty); // TextView "Carrinho vazio"

        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(this, CartManager.getInstance().getCartItems(), this);
        recyclerCart.setAdapter(adapter);

        checkCartEmpty(); // Verifica se o carrinho está vazio

        buttonPay.setOnClickListener(v -> showPaymentDialog());
    }

    // Atualiza a UI dependendo se o carrinho está vazio ou não
    private void checkCartEmpty() {
        boolean isEmpty = CartManager.getInstance().getCartItems().isEmpty();
        recyclerCart.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        textTotal.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        textEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        buttonPay.setEnabled(!isEmpty); // desabilita botão se vazio
        buttonPay.setAlpha(isEmpty ? 0.5f : 1f); // opcional: deixa o botão semi-transparente
    }

    private void showPaymentDialog() {
        if (CartManager.getInstance().getCartItems().isEmpty()) {
            Toast.makeText(this, "O carrinho está vazio!", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_payment, null);
        final EditText editMesa = dialogView.findViewById(R.id.editMesa);
        final RadioButton rbDinheiro = dialogView.findViewById(R.id.rbDinheiro);
        final RadioButton rbPix = dialogView.findViewById(R.id.rbPix);
        final RadioButton rbDebito = dialogView.findViewById(R.id.rbDebito);
        final RadioButton rbCredito = dialogView.findViewById(R.id.rbCredito);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Finalizar Pedido")
                .setView(dialogView)
                .setPositiveButton("Enviar", (dialog, which) -> {
                    String mesa = editMesa.getText().toString().trim();
                    if (mesa.isEmpty()) {
                        Toast.makeText(this, "Informe o número da mesa", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String pagamento;
                    if (rbDinheiro.isChecked()) pagamento = "Dinheiro";
                    else if (rbPix.isChecked()) pagamento = "Pix";
                    else if (rbDebito.isChecked()) pagamento = "Débito";
                    else if (rbCredito.isChecked()) pagamento = "Crédito";
                    else {
                        Toast.makeText(this, "Selecione uma forma de pagamento", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Limpa carrinho
                    CartManager.getInstance().clearCart();
                    adapter.notifyDataSetChanged();
                    updateTotal();
                    checkCartEmpty();

                    // Mostra dialog de sucesso
                    View successView = LayoutInflater.from(this).inflate(R.layout.dialog_success, null);
                    TextView textSuccess = successView.findViewById(R.id.textSuccess);
                    TextView textInfo = successView.findViewById(R.id.textInfo);

                    textSuccess.setText("Pedido da mesa " + mesa + " enviado!");
                    textInfo.setText("Forma de pagamento: " + pagamento + "\nO pedido está sendo preparado.");

                    androidx.appcompat.app.AlertDialog successDialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setView(successView)
                            .setCancelable(false)
                            .create();

                    successDialog.show();

                    new Handler().postDelayed(() -> {
                        successDialog.dismiss();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }, 2500);

                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onCartUpdated() {
        updateTotal();
        checkCartEmpty();
    }

    private void updateTotal() {
        textTotal.setText(String.format("Total: R$ %.2f", CartManager.getInstance().getTotal()));
    }
}
