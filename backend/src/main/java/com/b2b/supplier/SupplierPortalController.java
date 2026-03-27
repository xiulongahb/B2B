package com.b2b.supplier;

import com.b2b.domain.OrderEntity;
import com.b2b.domain.Product;
import com.b2b.domain.OrderEntityRepository;
import com.b2b.mvp.OrderService;
import com.b2b.security.SupplierPrincipal;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/supplier")
public class SupplierPortalController {

    private final SupplierProductService supplierProductService;
    private final OrderEntityRepository orderEntityRepository;
    private final OrderService orderService;

    public SupplierPortalController(
            SupplierProductService supplierProductService,
            OrderEntityRepository orderEntityRepository,
            OrderService orderService) {
        this.supplierProductService = supplierProductService;
        this.orderEntityRepository = orderEntityRepository;
        this.orderService = orderService;
    }

    @PostMapping("/products")
    public Product createProduct(
            @AuthenticationPrincipal SupplierPrincipal principal, @RequestBody ProductCreateBody body) {
        return supplierProductService.create(
                principal.getSupplierId(),
                body.getCategoryId(),
                body.getBrandId(),
                body.getName(),
                body.getDescription(),
                body.getPrice(),
                body.getStock() == null ? 0 : body.getStock(),
                body.getImageUrl());
    }

    @GetMapping("/products")
    public List<Product> myProducts(@AuthenticationPrincipal SupplierPrincipal principal) {
        return supplierProductService.listMine(principal.getSupplierId());
    }

    @PostMapping("/products/{id}/off-shelf")
    public Product offShelf(
            @AuthenticationPrincipal SupplierPrincipal principal, @PathVariable Long id) {
        return supplierProductService.updateOffShelf(principal.getSupplierId(), id);
    }

    @GetMapping("/orders")
    public List<OrderEntity> orders(@AuthenticationPrincipal SupplierPrincipal principal) {
        return orderEntityRepository.findBySupplierIdOrderByCreatedAtDesc(principal.getSupplierId());
    }

    @PostMapping("/orders/{id}/ship")
    public OrderEntity ship(
            @AuthenticationPrincipal SupplierPrincipal principal, @PathVariable Long id) {
        return orderService.shipBySupplier(principal.getSupplierId(), id);
    }

    public static class ProductCreateBody {
        private Long categoryId;
        private Long brandId;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stock;
        private String imageUrl;

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }

        public Long getBrandId() {
            return brandId;
        }

        public void setBrandId(Long brandId) {
            this.brandId = brandId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Integer getStock() {
            return stock;
        }

        public void setStock(Integer stock) {
            this.stock = stock;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}
