package com.rc.readcompass.exception.domain;

import com.rc.readcompass.exception.base.CustomException;
import com.rc.readcompass.exception.ErrorCode;

public class CommentException extends CustomException {

    public CommentException(ErrorCode errorCode) {
        super(errorCode);
        addDetail("Comment Domain Exception");
    }
}