package shop.controller;

import jakarta.servlet.http.HttpSession;
import shop.entity.Order;
import shop.entity.OrderItem;
import shop.entity.User;
import shop.service.OrderService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@org.springframework.stereotype.Controller
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 结算/确认订单页面
     */
    @GetMapping("/checkout")
    public String showCheckoutPage(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        return "checkout";
    }

    /**
     * 订单历史页面
     */
    @GetMapping("/orders")
    public String showOrdersPage(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        return "orders";
    }

    /**
     * API：从购物车创建订单
     */
    @PostMapping("/api/orders")
    @ResponseBody
    public Map<String, Object> createOrder(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }

        Order order = orderService.createOrderFromCart(currentUser.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("orderId", order.getId());
        result.put("total", order.getTotalPrice().toPlainString());
        result.put("message", "订单创建成功");
        return result;
    }

    /**
     * API：模拟支付
     */
    @PostMapping("/api/orders/{orderId}/pay")
    @ResponseBody
    public Map<String, Object> payOrder(@PathVariable Long orderId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }

        Order order = orderService.payOrder(currentUser.getId(), orderId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("orderId", order.getId());
        result.put("status", order.getStatus());
        result.put("message", "支付成功");
        return result;
    }

    /**
     * API：取消订单
     */
    @PostMapping("/api/orders/{orderId}/cancel")
    @ResponseBody
    public Map<String, Object> cancelOrder(@PathVariable Long orderId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }

        orderService.cancelOrder(currentUser.getId(), orderId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "订单已取消");
        return result;
    }

    /**
     * API：获取订单详情
     */
    @GetMapping("/api/orders/{orderId}")
    @ResponseBody
    public Map<String, Object> getOrder(@PathVariable Long orderId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }

        Order order = orderService.getOrder(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权查看该订单");
        }

        return orderToMap(order);
    }

    /**
     * API：获取用户订单列表
     */
    @GetMapping("/api/orders")
    @ResponseBody
    public List<Map<String, Object>> getUserOrders(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }

        return orderService.getUserOrders(currentUser.getId())
                .stream().map(this::orderToMap).collect(Collectors.toList());
    }

    private Map<String, Object> orderToMap(Order order) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("totalPrice", order.getTotalPrice().toPlainString());
        map.put("status", order.getStatus());
        map.put("createTime", order.getCreateTime() != null ? order.getCreateTime().toString() : "");

        List<Map<String, Object>> items = order.getItems().stream().map(oi -> {
            Map<String, Object> m = new HashMap<>();
            m.put("productName", oi.getProduct().getName());
            m.put("quantity", oi.getQuantity());
            m.put("price", oi.getPrice().toPlainString());
            double subtotal = oi.getPrice().doubleValue() * oi.getQuantity();
            m.put("subtotal", String.format("%.2f", subtotal));
            return m;
        }).collect(Collectors.toList());

        map.put("items", items);
        return map;
    }
}
