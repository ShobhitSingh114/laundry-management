package com.laundry.laundry_management.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.laundry.laundry_management.entity.LaundryService;
import com.laundry.laundry_management.enums.ServiceType;


@Repository
public interface LaundryServiceRepository extends JpaRepository<LaundryService, Long> {
    List<LaundryService> findByActiveTrue();
    List<LaundryService> findByType(ServiceType type);
}
