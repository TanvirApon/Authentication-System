package com.substring.auth.auth_app_backend.exceptions;

    /*
     Custom Exception this exception will trigger
     while if failed to find any Resource
     */
public class ResourceNotFoundException extends RuntimeException{

    // this method returns with a message
    public ResourceNotFoundException(String message){
        super(message);
    }
    // this method returns if any message is not parameterized
    public ResourceNotFoundException(){
        super("Resource not found !!!");
    }
}
