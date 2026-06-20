package shop.controller;

import jakarta.servlet.http.HttpSession;
import shop.entity.Product;
import shop.entity.ProductCategory;
import shop.entity.User;
import shop.service.ProductService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@org.springframework.stereotype.Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 商品浏览页面
     */
    @GetMapping("/products")
    public String showProductsPage(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        List<ProductCategory> categories = productService.findAllCategories();
        model.addAttribute("categories", categories);
        return "products";
    }

    /**
     * API：获取商品列表（支持分类筛选、排序）
     */
    @GetMapping("/api/products")
    @ResponseBody
    public List<Map<String, Object>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "createTime") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {

        List<Product> products = productService.findProducts(categoryId, sortBy, sortOrder);
        return products.stream().map(this::productToMap).collect(Collectors.toList());
    }

    /**
     * API：添加商品分类
     */
    @PostMapping("/api/products/categories")
    @ResponseBody
    public Map<String, Object> addCategory(@RequestParam String name,
                                           @RequestParam(required = false) String description,
                                           HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }
        ProductCategory category = productService.addCategory(name, description);
        return Map.of("id", category.getId(), "name", category.getName());
    }

    /**
     * API：添加商品
     */
    @PostMapping("/api/products")
    @ResponseBody
    public Map<String, Object> addProduct(@RequestParam Long categoryId,
                                          @RequestParam String name,
                                          @RequestParam(required = false) String description,
                                          @RequestParam BigDecimal price,
                                          @RequestParam Integer stock,
                                          HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            throw new RuntimeException("请先登录");
        }
        Product product = productService.addProduct(categoryId, name, description, price, stock);
        return productToMap(product);
    }

    private Map<String, Object> productToMap(Product product) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", product.getId());
        map.put("name", product.getName());
        map.put("description", product.getDescription());
        map.put("price", product.getPrice().toPlainString());
        map.put("stock", product.getStock());
        map.put("createTime", product.getCreateTime() != null ? product.getCreateTime().toString() : "");
        map.put("categoryId", product.getCategory() != null ? product.getCategory().getId() : null);
        map.put("categoryName", product.getCategory() != null ? product.getCategory().getName() : "");
        return map;
    }
}
