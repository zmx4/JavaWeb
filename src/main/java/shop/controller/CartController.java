package shop.controller;

import jakarta.servlet.http.HttpSession;
import shop.entity.CartItem;
import shop.entity.User;
import shop.service.CartService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@org.springframework.stereotype.Controller
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * 购物车页面
     */
    @GetMapping("/cart")
    public String showCartPage(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        return "cart";
    }

    /**
     * API：获取购物车列表
     */
    @GetMapping("/api/cart")
    @ResponseBody
    public Map<String, Object> getCart(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }

        List<CartItem> items = cartService.getCartItems(currentUser.getId());
        List<Map<String, Object>> list = items.stream().map(this::cartItemToMap).collect(Collectors.toList());

        // 计算总价
        double total = items.stream()
                .mapToDouble(i -> i.getProduct().getPrice().doubleValue() * i.getQuantity())
                .sum();

        Map<String, Object> result = new HashMap<>();
        result.put("items", list);
        result.put("count", items.size());
        result.put("total", String.format("%.2f", total));
        return result;
    }

    /**
     * API：添加商品到购物车
     */
    @PostMapping("/api/cart/add")
    @ResponseBody
    public Map<String, Object> addToCart(@RequestParam Long productId,
                                         @RequestParam(defaultValue = "1") int quantity,
                                         HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }

        CartItem item = cartService.addToCart(currentUser.getId(), productId, quantity);
        long count = cartService.getCartCount(currentUser.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "已添加到购物车");
        result.put("cartCount", count);
        return result;
    }

    /**
     * API：更新购物车商品数量
     */
    @PutMapping("/api/cart/{cartItemId}")
    @ResponseBody
    public Map<String, Object> updateQuantity(@PathVariable Long cartItemId,
                                              @RequestParam int quantity,
                                              HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }

        Map<String, Object> result = new HashMap<>();
        CartItem item = cartService.updateQuantity(currentUser.getId(), cartItemId, quantity);
        if (item == null) {
            result.put("removed", true);
            result.put("message", "商品已从购物车移除");
        } else {
            result.put("removed", false);
            result.put("item", cartItemToMap(item));
        }
        result.put("cartCount", cartService.getCartCount(currentUser.getId()));
        return result;
    }

    /**
     * API：移除购物车商品
     */
    @DeleteMapping("/api/cart/{cartItemId}")
    @ResponseBody
    public Map<String, Object> removeFromCart(@PathVariable Long cartItemId,
                                              HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }

        cartService.removeFromCart(currentUser.getId(), cartItemId);
        long count = cartService.getCartCount(currentUser.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "已移除");
        result.put("cartCount", count);
        return result;
    }

    private Map<String, Object> cartItemToMap(CartItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", item.getId());
        map.put("productId", item.getProduct().getId());
        map.put("productName", item.getProduct().getName());
        map.put("productPrice", item.getProduct().getPrice().toPlainString());
        map.put("productStock", item.getProduct().getStock());
        map.put("quantity", item.getQuantity());
        double subtotal = item.getProduct().getPrice().doubleValue() * item.getQuantity();
        map.put("subtotal", String.format("%.2f", subtotal));
        map.put("createTime", item.getCreateTime() != null ? item.getCreateTime().toString() : "");
        return map;
    }
}
