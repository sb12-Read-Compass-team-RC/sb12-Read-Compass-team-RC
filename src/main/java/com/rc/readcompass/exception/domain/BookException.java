package com.rc.readcompass.exception.domain;

import com.rc.readcompass.exception.base.CustomException;
import com.rc.readcompass.exception.ErrorCode;

public class BookException extends CustomException {

    public BookException(ErrorCode errorCode) {
        super(errorCode);
        addDetail("Book Domain Exception");
    }
}