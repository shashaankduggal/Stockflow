package com.stockflow.auth;

public class LoginResponse {

    private String message;
    private String token;
    private String fullName;
    private String email;
    private String role;

    public LoginResponse() {
    }

    public LoginResponse(String message) {
        this.message = message;
    }

    public LoginResponse(String message, String token, String fullName, String email, String role) {
        this.message = message;
        this.token = token;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
