package com.b2b.domain;

import java.math.BigDecimal;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ord_order")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 40)
    private String orderNo;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Column(nullable = false, length = 64)
    private String receiverName;

    @Column(nullable = false, length = 20)
    private String receiverPhone;

    @Column(nullable = false, length = 512)
    private String addressLine;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected OrderEntity() {}

    public OrderEntity(
            String orderNo,
            Long memberId,
            Long supplierId,
            BigDecimal totalAmount,
            String receiverName,
            String receiverPhone,
            String addressLine) {
        this.orderNo = orderNo;
        this.memberId = memberId;
        this.supplierId = supplierId;
        this.totalAmount = totalAmount;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.addressLine = addressLine;
    }

    public Long getId() {
        return id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
