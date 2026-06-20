package shop.dao;

import shop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByCategoryIdOrderByPriceAsc(Long categoryId);

    List<Product> findByCategoryIdOrderByPriceDesc(Long categoryId);

    List<Product> findByCategoryIdOrderByCreateTimeDesc(Long categoryId);

    List<Product> findByCategoryIdOrderByCreateTimeAsc(Long categoryId);

    List<Product> findAllByOrderByPriceAsc();

    List<Product> findAllByOrderByPriceDesc();

    List<Product> findAllByOrderByCreateTimeDesc();

    List<Product> findAllByOrderByCreateTimeAsc();
}
