package com.triton.triton.backend.model.repository;

import java.util.List;

import com.triton.triton.backend.model.LocalUser;
import com.triton.triton.backend.model.WebOrder;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data Access Object to access WebOrder data.
 */
public interface WebOrderRepository extends JpaRepository<WebOrder, Long> {

    List<WebOrder> findByUser(LocalUser user);

}