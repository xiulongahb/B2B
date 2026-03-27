package com.b2b.mall.member;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByPhone(String phone);

    Optional<Member> findByUsername(String username);

    boolean existsByPhone(String phone);

    boolean existsByUsername(String username);
}
