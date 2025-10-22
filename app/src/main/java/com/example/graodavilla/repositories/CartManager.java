package com.example.graodavilla.repositories;

import com.example.graodavilla.models.CartItem;
import com.example.graodavilla.models.Product;

import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static CartManager instance;
    private final List<CartItem> cartItems;
    private OnCartChangedListener listener;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void setOnCartChangedListener(OnCartChangedListener listener) {
        this.listener = listener;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    // Adiciona ao carrinho ou incrementa quantidade
    public void addToCart(Product product, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                notifyListener();
                return;
            }
        }
        cartItems.add(new CartItem(product, quantity));
        notifyListener();
    }

    // Novo: diminui quantidade de um produto no carrinho
    public void removeCartItemQuantity(Product product, int quantity) {
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            if (item.getProduct().getId().equals(product.getId())) {
                int newQty = item.getQuantity() - quantity;
                if (newQty > 0) {
                    item.setQuantity(newQty);
                } else {
                    cartItems.remove(i);
                }
                notifyListener();
                return;
            }
        }
    }

    // Remove apenas o item específico
    public void removeCartItem(CartItem cartItem) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getProduct().getId().equals(cartItem.getProduct().getId())) {
                cartItems.remove(i);
                notifyListener();
                break;
            }
        }
    }

    public void clearCart() {
        cartItems.clear();
        notifyListener();
    }

    public double getTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    public int getTotalQuantity() {
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getQuantity();
        }
        return total;
    }

    // 🔔 Notifica o listener se houver
    private void notifyListener() {
        if (listener != null) {
            listener.onCartChanged(getTotalQuantity());
        }
    }

    // 🔔 Interface do listener
    public interface OnCartChangedListener {
        void onCartChanged(int totalItems);
    }
}
