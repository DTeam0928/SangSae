package com.dteam0928.sangsae.common.db;

import jakarta.persistence.EntityManager;

@FunctionalInterface
public interface EmConsumer {
    void accept(EntityManager em);
}
