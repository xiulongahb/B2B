package com.b2b.config;

import com.b2b.domain.AdminUser;
import com.b2b.domain.AdminUserRepository;
import com.b2b.domain.Brand;
import com.b2b.domain.BrandRepository;
import com.b2b.domain.Category;
import com.b2b.domain.CategoryRepository;
import com.b2b.domain.Product;
import com.b2b.domain.ProductRepository;
import com.b2b.domain.ProductStatus;
import com.b2b.domain.Supplier;
import com.b2b.domain.SupplierRepository;
import com.b2b.domain.SupplierStatus;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 阶段 A MVP 演示数据：默认运营账号、类目/品牌、已审核供应商与上架商品。 使用 {@code dev} 或默认 profile（非 test）时加载。
 */
@Component
@Profile("!test")
public class MvpDataLoader implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    public MvpDataLoader(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (adminUserRepository.count() == 0) {
            adminUserRepository.save(
                    new AdminUser("admin", passwordEncoder.encode("Admin1234a")));
        }
        Category cat =
                categoryRepository
                        .findAll()
                        .stream()
                        .findFirst()
                        .orElseGet(() -> categoryRepository.save(new Category("默认类目", 0)));
        Brand brand =
                brandRepository
                        .findAll()
                        .stream()
                        .findFirst()
                        .orElseGet(() -> brandRepository.save(new Brand("默认品牌")));

        Supplier demo =
                supplierRepository
                        .findByUsername("demo_supplier")
                        .orElseGet(
                                () -> {
                                    Supplier s =
                                            new Supplier(
                                                    "demo_supplier",
                                                    passwordEncoder.encode("Demo1234a"),
                                                    "演示供应商有限公司");
                                    s.setStatus(SupplierStatus.APPROVED);
                                    return supplierRepository.save(s);
                                });

        if (productRepository.findBySupplierId(demo.getId()).isEmpty()) {
            Product p =
                    new Product(
                            demo.getId(),
                            cat.getId(),
                            brand.getId(),
                            "MVP演示商品",
                            "阶段A演示用商品，可在运营端审核后上架",
                            new BigDecimal("99.00"),
                            999);
            p.setStatus(ProductStatus.ON_SHELF);
            productRepository.save(p);
        }
    }
}
