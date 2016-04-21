package org.alxeg.meetings.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {
    
    @JsonProperty("error_message")
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

    