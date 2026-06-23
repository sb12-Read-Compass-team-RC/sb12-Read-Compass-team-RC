package com.rc.readcompass.exception.base;

import com.rc.readcompass.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;
    private final List<String> details = new ArrayList<>();

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CustomException addDetail(String detail) {
        this.details.add(detail);
        return this;
    }
}