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

    private static final int ITEM_PRODUCT = 0;
    private static final int ITEM_ADD = 1;

    // Interface para callback
    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> list, OnProductClickListener clickListener) {
        this.context = context;
        this.list = list;
        this.clickListener = clickListener;
    }

    @Override
    public int getItemViewType(int position) {
        // O último item será o card de "+"
        return position == list.size() ? ITEM_ADD : ITEM_PRODUCT;
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
            vh.textPrice.setText("R$ " + p.getPrice());

            String url = p.getImageUrl();
            if (url != null && !url.isEmpty()) {
                Glide.with(context)
                        .load(url)
                        .placeholder(R.drawable.defaultimage)
                        .into(vh.imageProduct);
            } else {
                vh.imageProduct.setImageResource(R.drawable.defaultimage);
            }

            // Clique usa o callback
            vh.itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onProductClick(p);
                }
            });

        } else if (holder instanceof AddViewHolder) {
            AddViewHolder avh = (AddViewHolder) holder;
            avh.itemView.setOnClickListener(v -> {
                // Mantém abrindo AddProductActivity diretamente
                context.startActivity(
                        new android.content.Intent(context, com.example.graodavilla.ui.AddProductActivity.class)
                );
            });
        }
    }

    @Override
    public int getItemCount() {
        // +1 para o card "Adicionar"
        return list.size() + 1;
    }

    // ViewHolder de produtos
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

    // ViewHolder do card "+"
    static class AddViewHolder extends RecyclerView.ViewHolder {
        public AddViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
