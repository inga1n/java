package com.triton.triton.backend.model.dao;

import java.util.List;

import com.triton.triton.backend.model.LocalUser;
import com.triton.triton.backend.model.WebOrder;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

/**
 * Data Access Object to access WebOrder data.
 */
public interface WebOrderDAO extends ListCrudRepository<WebOrder, Long> {

    List<WebOrder> findByUser(LocalUser user);

}