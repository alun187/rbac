package com.cuidl.rbacstudy.common.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author cuidl
 */
public class UserCountLockException extends AuthenticationException {
    public UserCountLockException(String msg, Throwable t) {
        super(msg, t);
    }

    public UserCountLockException(String msg) {
        super(msg);
    }
}
