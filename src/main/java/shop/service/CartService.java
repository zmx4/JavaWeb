package shop.service;

import shop.dao.CartRepository;
import shop.dao.ProductRepository;
import shop.entity.CartItem;
import shop.entity.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    /**
     * 添加商品到购物车（若已存在则增加数量）
     */
    @Transactional
    public CartItem addToCart(Long userId, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }

        Optional<CartItem> existing = cartRepository.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + quantity;
            if (newQty > product.getStock()) {
                throw new RuntimeException("库存不足，当前购物车已有 " + item.getQuantity() + " 件");
            }
            item.setQuantity(newQty);
            return cartRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setUser(new shop.entity.User());
            item.getUser().setId(userId);
            item.setProduct(product);
            item.setQuantity(quantity);
            return cartRepository.save(item);
        }
    }

    /**
     * 获取用户购物车列表
     */
    public List<CartItem> getCartItems(Long userId) {
        return cartRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    /**
     * 更新购物车商品数量
     */
    @Transactional
    public CartItem updateQuantity(Long userId, Long cartItemId, int quantity) {
        CartItem item = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("购物车项不存在"));

        if (!item.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }

        if (quantity <= 0) {
            cartRepository.delete(item);
            return null;
        }

        if (quantity > item.getProduct().getStock()) {
            throw new RuntimeException("库存不足");
        }

        item.setQuantity(quantity);
        return cartRepository.save(item);
    }

    /**
     * 移除购物车商品
     */
    @Transactional
    public void removeFromCart(Long userId, Long cartItemId) {
        CartItem item = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("购物车项不存在"));

        if (!item.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }

        cartRepository.delete(item);
    }

    /**
     * 获取购物车商品数量（种类数）
     */
    public long getCartCount(Long userId) {
        return cartRepository.countByUserId(userId);
    }
}
