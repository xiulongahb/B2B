package com.b2b.mvp;

import com.b2b.domain.CartItem;
import com.b2b.domain.CartItemRepository;
import com.b2b.domain.Product;
import com.b2b.domain.ProductRepository;
import com.b2b.domain.ProductStatus;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MallShopService {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public MallShopService(ProductRepository productRepository, CartItemRepository cartItemRepository) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public Page<Product> listOnShelf(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.ON_SHELF, pageable);
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public List<CartLine> listCartLines(Long memberId) {
        List<CartItem> items = cartItemRepository.findByMemberId(memberId);
        List<CartLine> lines = new ArrayList<>();
        for (CartItem ci : items) {
            Product p = productRepository.findById(ci.getProductId()).orElse(null);
            if (p == null) {
                continue;
            }
            BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            lines.add(new CartLine(ci.getId(), p.getId(), p.getName(), p.getPrice(), ci.getQuantity(), subtotal, p.getStock()));
        }
        return lines;
    }

    public static final class CartLine {
        private final Long cartItemId;
        private final Long productId;
        private final String productName;
        private final BigDecimal unitPrice;
        private final int quantity;
        private final BigDecimal subtotal;
        private final int stock;

        public CartLine(
                Long cartItemId,
                Long productId,
                String productName,
                BigDecimal unitPrice,
                int quantity,
                BigDecimal subtotal,
                int stock) {
            this.cartItemId = cartItemId;
            this.productId = productId;
            this.productName = productName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.subtotal = subtotal;
            this.stock = stock;
        }

        public Long getCartItemId() {
            return cartItemId;
        }

        public Long getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public int getStock() {
            return stock;
        }
    }
}
