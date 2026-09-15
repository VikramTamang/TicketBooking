package com.example.ticketbooking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Temporary entity used only in Phase 2 to prove that Hibernate can
 * connect to MySQL, create a table, and persist/read a row.
 * This class and its repository/endpoint will be deleted once the
 * checkpoint passes and before Phase 3 introduces the real User entity.
 */
@Entity
public class Ping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    public Ping() {
    }

    public Ping(String message) {
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}