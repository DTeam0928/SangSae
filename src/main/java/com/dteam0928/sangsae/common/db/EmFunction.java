package com.dteam0928.sangsae.common.db;

import jakarta.persistence.EntityManager;

@FunctionalInterface
public interface EmFunction<T> {
    T apply(EntityManager em);
}
