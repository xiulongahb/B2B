package com.b2b.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderEntityRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByOrderNo(String orderNo);

    List<OrderEntity> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<OrderEntity> findBySupplierIdOrderByCreatedAtDesc(Long supplierId);

    List<OrderEntity> findByCreatedAtBetween(Instant start, Instant end);

    long countByCreatedAtBetween(Instant start, Instant end);
}
