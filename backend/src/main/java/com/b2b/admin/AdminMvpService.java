package com.b2b.admin;

import com.b2b.common.api.ApiException;
import com.b2b.domain.Brand;
import com.b2b.domain.BrandRepository;
import com.b2b.domain.Category;
import com.b2b.domain.CategoryRepository;
import com.b2b.domain.OrderEntity;
import com.b2b.domain.OrderEntityRepository;
import com.b2b.domain.Product;
import com.b2b.domain.ProductRepository;
import com.b2b.domain.ProductStatus;
import com.b2b.domain.Supplier;
import com.b2b.domain.SupplierRepository;
import com.b2b.domain.SupplierStatus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminMvpService {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final OrderEntityRepository orderEntityRepository;

    public AdminMvpService(
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository,
            OrderEntityRepository orderEntityRepository) {
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.orderEntityRepository = orderEntityRepository;
    }

    public List<Category> listCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category createCategory(String name, int sortOrder) {
        return categoryRepository.save(new Category(name, sortOrder));
    }

    @Transactional
    public Category updateCategory(Long id, String name, Integer sortOrder) {
        Category c =
                categoryRepository
                        .findById(id)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "类目不存在"));
        if (name != null) c.setName(name);
        if (sortOrder != null) c.setSortOrder(sortOrder);
        return categoryRepository.save(c);
    }

    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    public List<Brand> listBrands() {
        return brandRepository.findAll();
    }

    @Transactional
    public Brand createBrand(String name) {
        return brandRepository.save(new Brand(name));
    }

    @Transactional
    public Brand updateBrand(Long id, String name) {
        Brand b =
                brandRepository
                        .findById(id)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "品牌不存在"));
        if (name != null) b.setName(name);
        return brandRepository.save(b);
    }

    @Transactional
    public void deleteBrand(Long id) {
        brandRepository.deleteById(id);
    }

    public List<Supplier> listSuppliers() {
        return supplierRepository.findAll();
    }

    @Transactional
    public Supplier approveSupplier(Long id) {
        Supplier s =
                supplierRepository
                        .findById(id)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "供应商不存在"));
        s.setStatus(SupplierStatus.APPROVED);
        return supplierRepository.save(s);
    }

    @Transactional
    public Supplier rejectSupplier(Long id) {
        Supplier s =
                supplierRepository
                        .findById(id)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "供应商不存在"));
        s.setStatus(SupplierStatus.REJECTED);
        return supplierRepository.save(s);
    }

    public List<Product> listPendingProducts() {
        return productRepository.findByStatus(ProductStatus.PENDING_SHELF);
    }

    @Transactional
    public Product approveProduct(Long id) {
        Product p =
                productRepository
                        .findById(id)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "商品不存在"));
        if (p.getStatus() != ProductStatus.PENDING_SHELF) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "商品不在待审核状态");
        }
        p.setStatus(ProductStatus.ON_SHELF);
        p.touch();
        return productRepository.save(p);
    }

    @Transactional
    public Product rejectProduct(Long id) {
        Product p =
                productRepository
                        .findById(id)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "商品不存在"));
        p.setStatus(ProductStatus.REJECTED);
        p.touch();
        return productRepository.save(p);
    }

    public List<OrderEntity> listOrders() {
        return orderEntityRepository.findAll();
    }

    public Map<String, Object> dashboard() {
        Instant now = Instant.now();
        Instant startOfDay = now.truncatedTo(ChronoUnit.DAYS);
        Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);
        long ordersToday = orderEntityRepository.countByCreatedAtBetween(startOfDay, endOfDay);
        long totalOrders = orderEntityRepository.count();
        long pendingProducts = productRepository.countByStatus(ProductStatus.PENDING_SHELF);
        long pendingSuppliers = supplierRepository.countByStatus(SupplierStatus.PENDING);
        Map<String, Object> m = new HashMap<>();
        m.put("ordersToday", ordersToday);
        m.put("totalOrders", totalOrders);
        m.put("pendingProducts", pendingProducts);
        m.put("pendingSuppliers", pendingSuppliers);
        return m;
    }
}
