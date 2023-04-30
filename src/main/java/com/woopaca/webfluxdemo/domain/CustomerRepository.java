package com.woopaca.webfluxdemo.domain;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;

public interface CustomerRepository extends R2dbcRepository<Customer, Long> {

    @Query("SELECT * FROM customer WHERE last_name = :lastName")
    Flux<Customer> findByLastName(@Param("lastName") String lastName);
}
