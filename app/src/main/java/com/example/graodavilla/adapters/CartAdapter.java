package com.example.graodavilla.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.graodavilla.R;
import com.example.graodavilla.models.CartItem;
import com.example.graodavilla.repositories.CartManager;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private final List<CartItem> cartItems;
    private final OnCartUpdatedListener listener;

    public interface OnCartUpdatedListener {
        void onCartUpdated();
    }

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartUpdatedListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.textName.setText(item.getProduct().getName());
        holder.textQuantity.setText(String.valueOf(item.getQuantity()));
        holder.textPriceTotal.setText(String.format("R$ %.2f", item.getTotalPrice()));
        holder.textPriceUnit.setText(String.format("R$ %.2f", item.getProduct().getPrice()));

        Glide.with(context)
                .load(item.getProduct().getImageUrl())
                .placeholder(R.drawable.defaultimage)
                .into(holder.imageProduct);

        // Aumentar quantidade
        holder.buttonIncrease.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            notifyItemChanged(position);
            listener.onCartUpdated();
        });

        // Diminuir quantidade
        holder.buttonDecrease.setOnClickListener(v -> {
            int qty = item.getQuantity() - 1;
            if (qty > 0) {
                item.setQuantity(qty);
                notifyItemChanged(position);
            } else {
                CartManager.getInstance().removeCartItem(item);
                cartItems.remove(position);
                notifyItemRemoved(position);
            }
            listener.onCartUpdated();
        });

        // Remover item com lixeira
        holder.buttonRemove.setOnClickListener(v -> {
            CartManager.getInstance().removeCartItem(item);
            cartItems.remove(position);
            notifyItemRemoved(position);
            listener.onCartUpdated();
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView textName, textPriceTotal, textPriceUnit, textQuantity;
        ImageButton buttonIncrease, buttonDecrease, buttonRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.imageProductCart);
            textName = itemView.findViewById(R.id.textNameCart);
            textQuantity = itemView.findViewById(R.id.textQuantityCart);
            textPriceTotal = itemView.findViewById(R.id.textPriceCart);
            textPriceUnit = itemView.findViewById(R.id.textPriceUnitCart);
            buttonIncrease = itemView.findViewById(R.id.buttonIncreaseCart);
            buttonDecrease = itemView.findViewById(R.id.buttonDecreaseCart);
            buttonRemove = itemView.findViewById(R.id.buttonRemoveCart);
        }
    }
}
