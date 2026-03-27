package com.b2b.mvp;

import com.b2b.common.api.ApiException;
import com.b2b.common.util.OrderNoGenerator;
import com.b2b.domain.CartItem;
import com.b2b.domain.CartItemRepository;
import com.b2b.domain.OrderEntity;
import com.b2b.domain.OrderEntityRepository;
import com.b2b.domain.OrderItem;
import com.b2b.domain.OrderItemRepository;
import com.b2b.domain.OrderStatus;
import com.b2b.domain.Product;
import com.b2b.domain.ProductRepository;
import com.b2b.domain.ProductStatus;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderEntityRepository orderEntityRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            OrderEntityRepository orderEntityRepository,
            OrderItemRepository orderItemRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.orderEntityRepository = orderEntityRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public OrderEntity createOrderFromCart(
            Long memberId,
            String receiverName,
            String receiverPhone,
            String addressLine) {
        List<CartItem> items = cartItemRepository.findByMemberId(memberId);
        if (items.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "购物车为空");
        }
        List<Product> products = new ArrayList<>();
        Long supplierId = null;
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : items) {
            Product p =
                    productRepository
                            .findById(ci.getProductId())
                            .orElseThrow(
                                    () ->
                                            new ApiException(
                                                    HttpStatus.BAD_REQUEST, "商品不存在"));
            if (p.getStatus() != ProductStatus.ON_SHELF) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "商品未上架：" + p.getName());
            }
            if (p.getStock() < ci.getQuantity()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "库存不足：" + p.getName());
            }
            if (supplierId == null) {
                supplierId = p.getSupplierId();
            } else if (!supplierId.equals(p.getSupplierId())) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST, "MVP 仅支持同一供应商商品合并下单");
            }
            total =
                    total.add(
                            p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
            products.add(p);
        }
        if (supplierId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "无法确定供应商");
        }
        String orderNo = OrderNoGenerator.next();
        OrderEntity order =
                new OrderEntity(
                        orderNo,
                        memberId,
                        supplierId,
                        total,
                        receiverName,
                        receiverPhone,
                        addressLine);
        orderEntityRepository.saveAndFlush(order);
        for (int i = 0; i < items.size(); i++) {
            CartItem ci = items.get(i);
            Product p = products.get(i);
            p.setStock(p.getStock() - ci.getQuantity());
            p.touch();
            productRepository.save(p);
            orderItemRepository.save(
                    new OrderItem(
                            order.getId(),
                            p.getId(),
                            p.getName(),
                            p.getPrice(),
                            ci.getQuantity(),
                            p.getSupplierId()));
        }
        cartItemRepository.deleteByMemberId(memberId);
        return order;
    }

    @Transactional
    public OrderEntity mockPay(Long memberId, Long orderId) {
        OrderEntity o =
                orderEntityRepository
                        .findById(orderId)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "订单不存在"));
        if (!o.getMemberId().equals(memberId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "无权操作该订单");
        }
        if (o.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "订单状态不可支付");
        }
        o.setStatus(OrderStatus.PAID);
        return o;
    }

    @Transactional
    public OrderEntity shipBySupplier(Long supplierId, Long orderId) {
        OrderEntity o =
                orderEntityRepository
                        .findById(orderId)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "订单不存在"));
        if (!o.getSupplierId().equals(supplierId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "无权操作该订单");
        }
        if (o.getStatus() != OrderStatus.PAID) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "订单未处于待发货状态");
        }
        o.setStatus(OrderStatus.SHIPPED);
        return o;
    }

    @Transactional
    public OrderEntity completeByMember(Long memberId, Long orderId) {
        OrderEntity o =
                orderEntityRepository
                        .findById(orderId)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "订单不存在"));
        if (!o.getMemberId().equals(memberId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "无权操作该订单");
        }
        if (o.getStatus() != OrderStatus.SHIPPED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "订单未发货");
        }
        o.setStatus(OrderStatus.COMPLETED);
        return o;
    }
}
