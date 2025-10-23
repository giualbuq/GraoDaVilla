package com.example.graodavilla.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.graodavilla.R;
import com.example.graodavilla.models.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Product> list;
    private OnProductClickListener clickListener;
    private boolean isAdmin;

    private static final int ITEM_PRODUCT = 0;
    private static final int ITEM_ADD = 1;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> list, OnProductClickListener clickListener, boolean isAdmin) {
        this.context = context;
        this.list = list;
        this.clickListener = clickListener;
        this.isAdmin = isAdmin;
    }

    @Override
    public int getItemViewType(int position) {
        // O último item é o "+" só se for admin
        if (isAdmin && position == list.size()) {
            return ITEM_ADD;
        }
        return ITEM_PRODUCT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_ADD) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_add_product, parent, false);
            return new AddViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
            return new ProductViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ProductViewHolder) {
            Product p = list.get(position);
            ProductViewHolder vh = (ProductViewHolder) holder;

            vh.textName.setText(p.getName());
            vh.textDescription.setText(p.getDescription());
            vh.textPrice.setText(String.format("R$%.2f", p.getPrice()));

            String url = p.getImageUrl();
            if (url != null && !url.isEmpty()) {
                Glide.with(context)
                        .load(url)
                        .placeholder(R.drawable.defaultimage)
                        .into(vh.imageProduct);
            } else {
                vh.imageProduct.setImageResource(R.drawable.defaultimage);
            }

            vh.itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onProductClick(p);
            });

        } else if (holder instanceof AddViewHolder) {
            holder.itemView.setOnClickListener(v -> {
                context.startActivity(
                        new android.content.Intent(context, com.example.graodavilla.ui.AddProductActivity.class)
                );
            });
        }
    }

    @Override
    public int getItemCount() {
        // ✅ se for admin, mostra o "+" também
        return isAdmin ? list.size() + 1 : list.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView textName, textDescription, textPrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            textName = itemView.findViewById(R.id.textName);
            textDescription = itemView.findViewById(R.id.textDescription);
            textPrice = itemView.findViewById(R.id.textPrice);
        }
    }

    static class AddViewHolder extends RecyclerView.ViewHolder {
        public AddViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
