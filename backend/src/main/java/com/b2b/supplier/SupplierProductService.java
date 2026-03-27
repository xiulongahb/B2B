package com.b2b.supplier;

import com.b2b.common.api.ApiException;
import com.b2b.domain.Product;
import com.b2b.domain.ProductRepository;
import com.b2b.domain.ProductStatus;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierProductService {

    private final ProductRepository productRepository;

    public SupplierProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product create(
            Long supplierId,
            Long categoryId,
            Long brandId,
            String name,
            String description,
            BigDecimal price,
            int stock,
            String imageUrl) {
        if (categoryId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "类目不能为空");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "商品名称不能为空");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "价格须大于 0");
        }
        Product p =
                new Product(
                        supplierId,
                        categoryId,
                        brandId,
                        name.trim(),
                        description,
                        price,
                        stock);
        p.setStatus(ProductStatus.PENDING_SHELF);
        if (imageUrl != null) {
            p.setImageUrl(imageUrl);
        }
        return productRepository.save(p);
    }

    public List<Product> listMine(Long supplierId) {
        return productRepository.findBySupplierId(supplierId);
    }

    @Transactional
    public Product updateOffShelf(Long supplierId, Long productId) {
        Product p =
                productRepository
                        .findById(productId)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "商品不存在"));
        if (!p.getSupplierId().equals(supplierId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "无权操作");
        }
        p.setStatus(ProductStatus.OFF_SHELF);
        p.touch();
        return productRepository.save(p);
    }
}
