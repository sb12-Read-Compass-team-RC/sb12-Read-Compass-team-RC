package com.rc.readcompass.exception;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private int status;
    private String message;
    private List<String> details;
}