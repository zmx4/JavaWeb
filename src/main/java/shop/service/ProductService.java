package shop.service;

import shop.dao.ProductCategoryRepository;
import shop.dao.ProductRepository;
import shop.entity.Product;
import shop.entity.ProductCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, ProductCategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * 获取所有商品分类
     */
    public List<ProductCategory> findAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * 根据排序方式获取商品列表
     * @param categoryId 分类ID，null表示所有分类
     * @param sortBy 排序字段：price、createTime
     * @param sortOrder 排序方向：asc、desc
     */
    public List<Product> findProducts(Long categoryId, String sortBy, String sortOrder) {
        boolean hasCategory = categoryId != null;
        boolean isAsc = "asc".equalsIgnoreCase(sortOrder);

        if ("price".equalsIgnoreCase(sortBy)) {
            if (hasCategory) {
                return isAsc
                        ? productRepository.findByCategoryIdOrderByPriceAsc(categoryId)
                        : productRepository.findByCategoryIdOrderByPriceDesc(categoryId);
            } else {
                return isAsc
                        ? productRepository.findAllByOrderByPriceAsc()
                        : productRepository.findAllByOrderByPriceDesc();
            }
        } else if ("createTime".equalsIgnoreCase(sortBy)) {
            if (hasCategory) {
                return isAsc
                        ? productRepository.findByCategoryIdOrderByCreateTimeAsc(categoryId)
                        : productRepository.findByCategoryIdOrderByCreateTimeDesc(categoryId);
            } else {
                return isAsc
                        ? productRepository.findAllByOrderByCreateTimeAsc()
                        : productRepository.findAllByOrderByCreateTimeDesc();
            }
        } else {
            // 默认按创建时间降序
            if (hasCategory) {
                return productRepository.findByCategoryIdOrderByCreateTimeDesc(categoryId);
            } else {
                return productRepository.findAllByOrderByCreateTimeDesc();
            }
        }
    }

    /**
     * 添加商品分类
     */
    @Transactional
    public ProductCategory addCategory(String name, String description) {
        ProductCategory category = new ProductCategory();
        category.setName(name);
        category.setDescription(description);
        return categoryRepository.save(category);
    }

    /**
     * 添加商品
     */
    @Transactional
    public Product addProduct(Long categoryId, String name, String description,
                              java.math.BigDecimal price, Integer stock) {
        ProductCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("分类不存在"));

        Product product = new Product();
        product.setCategory(category);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        return productRepository.save(product);
    }

    /**
     * 补货 - 增加商品库存
     * @param productId 商品ID
     * @param quantity 补货数量（必须大于0）
     * @return 补货后的商品对象
     */
    @Transactional
    public Product restockProduct(Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("补货数量必须大于0");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        product.setStock(product.getStock() + quantity);
        return productRepository.save(product);
    }
}
