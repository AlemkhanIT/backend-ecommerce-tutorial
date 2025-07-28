package com.example.demo.service;

import com.example.demo.dto.CartDTO;
import com.example.demo.exception.InsufficientStockException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.CartMapper;
import com.example.demo.model.Cart;
import com.example.demo.model.CartItem;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repositories.CartRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    public void createCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Cart cart = new Cart(null, user, new ArrayList<>());
        cartRepository.save(cart);
    }

    public CartDTO addToCart(Long userId, Long productId, Integer quantity){
        User user = userRepository.findById(userId)
                .orElseThrow(()->new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("Product not found"));

        if(product.getQuantity()<quantity){
            throw new InsufficientStockException("Not enough available");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElse(new Cart(null, user, new ArrayList<>()));
        Optional<CartItem> existingCartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if(existingCartItem.isPresent()){
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity()+quantity);
        }else{
            CartItem cartItem = new CartItem(null, cart, product, quantity);
            cart.getItems().add(cartItem);
        }
        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toDTO(savedCart);

    }

    public CartDTO getCart(Long userId){
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(()->new ResourceNotFoundException("Cart not found"));

        return cartMapper.toDTO(cart);
    }
    public void clearCart(Long userId){
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(()->new ResourceNotFoundException("Cart not found"));

        cart.getItems().clear();
        cartRepository.save(cart);
    }

    //update
    public void removeCartItem(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user"));

        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));

        cartRepository.save(cart);
    }
}
