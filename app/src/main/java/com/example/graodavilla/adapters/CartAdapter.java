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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private final List<CartItem> cartItems;
    private final OnCartUpdatedListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

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
        holder.textPriceUnit.setText(currencyFormat.format(item.getProduct().getPrice()));
        holder.textPriceTotal.setText(currencyFormat.format(item.getTotalPrice()));

        Glide.with(context)
                .load(item.getProduct().getImageUrl())
                .placeholder(R.drawable.defaultimage)
                .into(holder.imageProduct);

        // Aumentar quantidade
        holder.buttonIncrease.setOnClickListener(v -> {
            CartManager.getInstance().addToCart(item.getProduct(), 1);
            notifyDataSetChanged();
            listener.onCartUpdated();
        });

        // Diminuir quantidade
        holder.buttonDecrease.setOnClickListener(v -> {
            CartManager.getInstance().removeCartItemQuantity(item.getProduct(), 1);
            notifyDataSetChanged();
            listener.onCartUpdated();
        });

        // Remover item com lixeira
        holder.buttonRemove.setOnClickListener(v -> {
            CartManager.getInstance().removeCartItem(item);
            notifyDataSetChanged();
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
