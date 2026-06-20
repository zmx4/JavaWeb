package shop.service;

import shop.dao.CartRepository;
import shop.dao.OrderRepository;
import shop.dao.ProductRepository;
import shop.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    /**
     * 从购物车创建订单
     */
    @Transactional
    public Order createOrderFromCart(Long userId) {
        List<CartItem> cartItems = cartRepository.findByUserIdOrderByCreateTimeDesc(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车为空，无法创建订单");
        }

        // 校验库存并计算总价
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            Product p = ci.getProduct();
            if (p.getStock() < ci.getQuantity()) {
                throw new RuntimeException("商品「" + p.getName() + "」库存不足");
            }
            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        // 创建订单
        Order order = new Order();
        order.setUser(new User());
        order.getUser().setId(userId);
        order.setTotalPrice(total);
        order.setStatus("PENDING");
        Order savedOrder = orderRepository.save(order);

        // 创建订单项
        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setOrder(savedOrder);
            oi.setProduct(ci.getProduct());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(ci.getProduct().getPrice());
            savedOrder.getItems().add(oi);
        }
        orderRepository.save(savedOrder);

        return savedOrder;
    }

    /**
     * 模拟支付（扣减库存、清空购物车、更新订单状态）
     */
    @Transactional
    public Order payOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作该订单");
        }

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("该订单状态无法支付");
        }

        // 扣减库存
        for (OrderItem oi : order.getItems()) {
            Product p = productRepository.findById(oi.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("商品不存在"));
            if (p.getStock() < oi.getQuantity()) {
                throw new RuntimeException("商品「" + p.getName() + "」库存不足，支付失败");
            }
            p.setStock(p.getStock() - oi.getQuantity());
            productRepository.save(p);
        }

        // 更新订单状态
        order.setStatus("PAID");
        orderRepository.save(order);

        // 清空购物车
        cartRepository.deleteByUserId(userId);

        return order;
    }

    /**
     * 取消订单
     */
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作该订单");
        }

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("只能取消待支付订单");
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }

    /**
     * 查询用户订单列表
     */
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    /**
     * 查询订单详情
     */
    public Optional<Order> getOrder(Long orderId) {
        return orderRepository.findById(orderId);
    }
}
