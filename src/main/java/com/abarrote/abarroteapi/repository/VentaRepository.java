package com.abarrote.abarroteapi.repository;

import com.abarrote.abarroteapi.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

}
