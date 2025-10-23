package com.example.graodavilla.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.graodavilla.R;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PedidosAdapter extends RecyclerView.Adapter<PedidosAdapter.ViewHolder> {

    private final Context context;
    private final List<Map<String, Object>> pedidos;

    public PedidosAdapter(Context context, List<Map<String, Object>> pedidos) {
        this.context = context;
        this.pedidos = pedidos;
    }

    @NonNull
    @Override
    public PedidosAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_pedido, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidosAdapter.ViewHolder holder, int position) {
        Map<String, Object> pedido = pedidos.get(position);

        // Formatar data
        Timestamp ts = (Timestamp) pedido.get("createdAt");
        String data = "";
        if (ts != null) {
            data = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(ts.toDate());
        }

        holder.textData.setText(data);
        holder.textStatus.setText("Status: " + pedido.get("status"));
        double total = pedido.get("total") instanceof Number ? ((Number) pedido.get("total")).doubleValue() : 0;
        holder.textTotal.setText(String.format("Total: R$ %.2f", total));

        // Clique para abrir detalhes
        holder.itemView.setOnClickListener(v -> showPedidoDetails(pedido));
    }

    private void showPedidoDetails(Map<String, Object> pedido) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_pedido_details, null);
        LinearLayout layoutItems = dialogView.findViewById(R.id.layoutPedidoItems);
        TextView textTotal = dialogView.findViewById(R.id.textPedidoTotal);
        TextView buttonClose = dialogView.findViewById(R.id.buttonCloseDialog);

        // Adicionar os itens do pedido dinamicamente
        List<Map<String, Object>> items = (List<Map<String, Object>>) pedido.get("items");
        if (items != null) {
            for (Map<String, Object> item : items) {
                View itemView = LayoutInflater.from(context).inflate(R.layout.item_pedido_product, layoutItems, false);
                TextView textName = itemView.findViewById(R.id.textProductName);
                TextView textQuantity = itemView.findViewById(R.id.textProductQuantity);
                TextView textPrice = itemView.findViewById(R.id.textProductPrice);

                textName.setText(item.get("name").toString());
                textQuantity.setText("x" + item.get("quantity").toString());
                double price = item.get("price") instanceof Number ? ((Number) item.get("price")).doubleValue() : 0;
                textPrice.setText(String.format("R$ %.2f", price));

                layoutItems.addView(itemView);
            }
        }

        // Total
        double totalPedido = pedido.get("total") instanceof Number ? ((Number) pedido.get("total")).doubleValue() : 0;
        textTotal.setText(String.format("Total: R$ %.2f", totalPedido));

        // Criar dialog
        AlertDialog dialog = new AlertDialog.Builder(context, R.style.DarkAlertDialog)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        buttonClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textData, textStatus, textTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textData = itemView.findViewById(R.id.textPedidoData);
            textStatus = itemView.findViewById(R.id.textPedidoStatus);
            textTotal = itemView.findViewById(R.id.textPedidoTotal);
        }
    }
}
