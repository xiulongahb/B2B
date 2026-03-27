package com.b2b.mall.shop;

import com.b2b.common.api.ApiException;
import com.b2b.domain.OrderEntity;
import com.b2b.domain.OrderEntityRepository;
import com.b2b.domain.OrderItem;
import com.b2b.domain.OrderItemRepository;
import com.b2b.domain.Product;
import com.b2b.domain.ProductStatus;
import com.b2b.mvp.CartService;
import com.b2b.mvp.MallShopService;
import com.b2b.mvp.OrderService;
import com.b2b.security.MallMemberPrincipal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mall")
public class MallShopController {

    private final MallShopService mallShopService;
    private final CartService cartService;
    private final OrderService orderService;
    private final OrderEntityRepository orderEntityRepository;
    private final OrderItemRepository orderItemRepository;

    public MallShopController(
            MallShopService mallShopService,
            CartService cartService,
            OrderService orderService,
            OrderEntityRepository orderEntityRepository,
            OrderItemRepository orderItemRepository) {
        this.mallShopService = mallShopService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.orderEntityRepository = orderEntityRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @GetMapping("/products")
    public Page<Product> products(
            @PageableDefault(size = 20) Pageable pageable) {
        return mallShopService.listOnShelf(pageable);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> product(@PathVariable Long id) {
        Product p = mallShopService.getProduct(id);
        if (p == null || p.getStatus() != ProductStatus.ON_SHELF) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(p);
    }

    @GetMapping("/cart")
    public List<MallShopService.CartLine> cart(@AuthenticationPrincipal MallMemberPrincipal principal) {
        return mallShopService.listCartLines(principal.getMemberId());
    }

    @PostMapping("/cart/items")
    public ResponseEntity<?> addCart(
            @AuthenticationPrincipal MallMemberPrincipal principal, @RequestBody CartMutateBody body) {
        return ResponseEntity.ok(
                cartService.addOrUpdate(
                        principal.getMemberId(), body.getProductId(), body.getQuantity()));
    }

    @PutMapping("/cart/items/{cartItemId}")
    public ResponseEntity<?> updateCart(
            @AuthenticationPrincipal MallMemberPrincipal principal,
            @PathVariable Long cartItemId,
            @RequestBody CartQtyBody body) {
        return ResponseEntity.ok(
                cartService.setQuantity(principal.getMemberId(), cartItemId, body.getQuantity()));
    }

    @DeleteMapping("/cart/items/{cartItemId}")
    public ResponseEntity<Void> deleteCart(
            @AuthenticationPrincipal MallMemberPrincipal principal, @PathVariable Long cartItemId) {
        cartService.remove(principal.getMemberId(), cartItemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/orders")
    public OrderEntity createOrder(
            @AuthenticationPrincipal MallMemberPrincipal principal, @RequestBody CheckoutBody body) {
        return orderService.createOrderFromCart(
                principal.getMemberId(),
                body.getReceiverName(),
                body.getReceiverPhone(),
                body.getAddressLine());
    }

    @GetMapping("/orders")
    public List<OrderEntity> orders(@AuthenticationPrincipal MallMemberPrincipal principal) {
        return orderEntityRepository.findByMemberIdOrderByCreatedAtDesc(principal.getMemberId());
    }

    @GetMapping("/orders/{id}")
    public Map<String, Object> orderDetail(
            @AuthenticationPrincipal MallMemberPrincipal principal, @PathVariable Long id) {
        OrderEntity o =
                orderEntityRepository
                        .findById(id)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "订单不存在"));
        if (!o.getMemberId().equals(principal.getMemberId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "无权查看");
        }
        List<OrderItem> items = orderItemRepository.findByOrderId(id);
        Map<String, Object> m = new HashMap<>();
        m.put("order", o);
        m.put("items", items);
        return m;
    }

    @PostMapping("/orders/{id}/pay")
    public OrderEntity pay(
            @AuthenticationPrincipal MallMemberPrincipal principal, @PathVariable Long id) {
        return orderService.mockPay(principal.getMemberId(), id);
    }

    @PostMapping("/orders/{id}/confirm")
    public OrderEntity confirm(
            @AuthenticationPrincipal MallMemberPrincipal principal, @PathVariable Long id) {
        return orderService.completeByMember(principal.getMemberId(), id);
    }

    public static class CartMutateBody {
        private Long productId;
        private int quantity;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    public static class CartQtyBody {
        private int quantity;

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    public static class CheckoutBody {
        private String receiverName;
        private String receiverPhone;
        private String addressLine;

        public String getReceiverName() {
            return receiverName;
        }

        public void setReceiverName(String receiverName) {
            this.receiverName = receiverName;
        }

        public String getReceiverPhone() {
            return receiverPhone;
        }

        public void setReceiverPhone(String receiverPhone) {
            this.receiverPhone = receiverPhone;
        }

        public String getAddressLine() {
            return addressLine;
        }

        public void setAddressLine(String addressLine) {
            this.addressLine = addressLine;
        }
    }
}
