package cart.dao;

import cart.domain.product.Product;

import java.util.List;
import java.util.Optional;

public interface ProductDao {

    Product insert(Product product);

    int update(Product product);

    Optional<Product> findById(Long id);

    List<Product> findAll();

    int deleteById(Long id);
}
