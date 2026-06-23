package com.rc.readcompass.exception.domain;

import com.rc.readcompass.exception.base.CustomException;
import com.rc.readcompass.exception.ErrorCode;

public class UserException extends CustomException {

    public UserException(ErrorCode errorCode) {
        super(errorCode);
        addDetail("User Domain Exception");
    }
}