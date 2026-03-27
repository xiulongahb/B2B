package com.b2b.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByMemberId(Long memberId);

    Optional<CartItem> findByMemberIdAndProductId(Long memberId, Long productId);

    void deleteByMemberId(Long memberId);
}
