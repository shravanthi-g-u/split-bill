package com.shravanthi.split_bill.repository;

import com.shravanthi.split_bill.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, Long> {
}