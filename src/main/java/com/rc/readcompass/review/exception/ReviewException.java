package com.rc.readcompass.review.exception;

import com.rc.readcompass.exception.base.CustomException;
import com.rc.readcompass.exception.ErrorCode;

public class ReviewException extends CustomException {

    public ReviewException(ErrorCode errorCode) {
        super(errorCode);
        addDetail("Review Domain Exception");
    }

    public ReviewException(ErrorCode errorCode, Throwable cause){
        super(errorCode, cause);
        addDetail("Review Domain Exception");
    }
}