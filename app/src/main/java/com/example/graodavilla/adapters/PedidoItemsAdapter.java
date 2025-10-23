package com.example.graodavilla.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.graodavilla.R;

import java.util.List;
import java.util.Map;

public class PedidoItemsAdapter extends RecyclerView.Adapter<PedidoItemsAdapter.ViewHolder> {

    private final Context context;
    private final List<Map<String, Object>> items;

    public PedidoItemsAdapter(Context context, List<Map<String, Object>> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public PedidoItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_pedido_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoItemsAdapter.ViewHolder holder, int position) {
        Map<String, Object> item = items.get(position);

        String name = (String) item.get("name");
        double price = item.get("price") instanceof Number ? ((Number) item.get("price")).doubleValue() : 0;
        long quantity = item.get("quantity") instanceof Number ? ((Number) item.get("quantity")).longValue() : 0;

        holder.textName.setText(name);
        holder.textQuantity.setText("Qtd: " + quantity);
        holder.textPrice.setText(String.format("R$ %.2f", price));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textQuantity, textPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textProductName);
            textQuantity = itemView.findViewById(R.id.textProductQuantity);
            textPrice = itemView.findViewById(R.id.textProductPrice);
        }
    }
}
