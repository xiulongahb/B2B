package com.b2b.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByUsername(String username);

    long countByStatus(SupplierStatus status);
}
