package com.substring.auth.auth_app_backend.dtos;

//public record ErrorResponse(String message,int status,String error) {
//}

import org.springframework.http.HttpStatus;

public record ErrorResponse(String message, HttpStatus status, int statusCode) {
}
