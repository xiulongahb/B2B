package com.b2b.mvp;

import com.b2b.common.api.ApiException;
import com.b2b.domain.CartItem;
import com.b2b.domain.CartItemRepository;
import com.b2b.domain.Product;
import com.b2b.domain.ProductRepository;
import com.b2b.domain.ProductStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CartItem addOrUpdate(Long memberId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "数量须大于 0");
        }
        Product p =
                productRepository
                        .findById(productId)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "商品不存在"));
        if (p.getStatus() != ProductStatus.ON_SHELF) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "商品不可购买");
        }
        CartItem existing =
                cartItemRepository.findByMemberIdAndProductId(memberId, productId).orElse(null);
        if (existing == null) {
            if (quantity > p.getStock()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "库存不足");
            }
            return cartItemRepository.save(new CartItem(memberId, productId, quantity));
        }
        int newQty = existing.getQuantity() + quantity;
        if (newQty > p.getStock()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "库存不足");
        }
        existing.setQuantity(newQty);
        return cartItemRepository.save(existing);
    }

    @Transactional
    public CartItem setQuantity(Long memberId, Long cartItemId, int quantity) {
        if (quantity <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "数量须大于 0");
        }
        CartItem ci =
                cartItemRepository
                        .findById(cartItemId)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "购物车项不存在"));
        if (!ci.getMemberId().equals(memberId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "无权操作");
        }
        Product p =
                productRepository
                        .findById(ci.getProductId())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "商品不存在"));
        if (quantity > p.getStock()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "库存不足");
        }
        ci.setQuantity(quantity);
        return cartItemRepository.save(ci);
    }

    @Transactional
    public void remove(Long memberId, Long cartItemId) {
        CartItem ci =
                cartItemRepository
                        .findById(cartItemId)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "购物车项不存在"));
        if (!ci.getMemberId().equals(memberId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "无权操作");
        }
        cartItemRepository.delete(ci);
    }
}
