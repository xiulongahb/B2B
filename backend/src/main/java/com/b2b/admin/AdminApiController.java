package com.b2b.admin;

import com.b2b.domain.Brand;
import com.b2b.domain.Category;
import com.b2b.domain.OrderEntity;
import com.b2b.domain.Product;
import com.b2b.domain.Supplier;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    private final AdminMvpService adminMvpService;

    public AdminApiController(AdminMvpService adminMvpService) {
        this.adminMvpService = adminMvpService;
    }

    @GetMapping("/categories")
    public List<Category> categories() {
        return adminMvpService.listCategories();
    }

    @PostMapping("/categories")
    public Category createCategory(@RequestBody CategoryBody body) {
        return adminMvpService.createCategory(body.getName(), body.getSortOrder() == null ? 0 : body.getSortOrder());
    }

    @PutMapping("/categories/{id}")
    public Category updateCategory(@PathVariable Long id, @RequestBody CategoryBody body) {
        return adminMvpService.updateCategory(id, body.getName(), body.getSortOrder());
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        adminMvpService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/brands")
    public List<Brand> brands() {
        return adminMvpService.listBrands();
    }

    @PostMapping("/brands")
    public Brand createBrand(@RequestBody BrandBody body) {
        return adminMvpService.createBrand(body.getName());
    }

    @PutMapping("/brands/{id}")
    public Brand updateBrand(@PathVariable Long id, @RequestBody BrandBody body) {
        return adminMvpService.updateBrand(id, body.getName());
    }

    @DeleteMapping("/brands/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        adminMvpService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/suppliers")
    public List<Supplier> suppliers() {
        return adminMvpService.listSuppliers();
    }

    @PostMapping("/suppliers/{id}/approve")
    public Supplier approveSupplier(@PathVariable Long id) {
        return adminMvpService.approveSupplier(id);
    }

    @PostMapping("/suppliers/{id}/reject")
    public Supplier rejectSupplier(@PathVariable Long id) {
        return adminMvpService.rejectSupplier(id);
    }

    @GetMapping("/products/pending")
    public List<Product> pendingProducts() {
        return adminMvpService.listPendingProducts();
    }

    @PostMapping("/products/{id}/approve")
    public Product approveProduct(@PathVariable Long id) {
        return adminMvpService.approveProduct(id);
    }

    @PostMapping("/products/{id}/reject")
    public Product rejectProduct(@PathVariable Long id) {
        return adminMvpService.rejectProduct(id);
    }

    @GetMapping("/orders")
    public List<OrderEntity> orders() {
        return adminMvpService.listOrders();
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return adminMvpService.dashboard();
    }

    public static class CategoryBody {
        private String name;
        private Integer sortOrder;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }
    }

    public static class BrandBody {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
