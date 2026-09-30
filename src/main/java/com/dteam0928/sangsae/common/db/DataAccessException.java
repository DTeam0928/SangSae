package com.dteam0928.sangsae.common.db;

/** SQLException 을 감싼 런타임 예외. Service 가 checked 예외에 묶이지 않게 한다. */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
