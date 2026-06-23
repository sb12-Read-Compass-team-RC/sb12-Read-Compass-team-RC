package com.rc.readcompass.exception.domain;

import com.rc.readcompass.exception.base.CustomException;
import com.rc.readcompass.exception.ErrorCode;

public class ReviewException extends CustomException {

    public ReviewException(ErrorCode errorCode) {
        super(errorCode);
        addDetail("Review Domain Exception");
    }
}