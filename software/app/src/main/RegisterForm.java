package com.christo.moneyplant.models.api;

public class RegisterForm {
    private final String aadhaar;
    private final String email;
    private final String name;
    private final String password;

    public RegisterForm(String aadhaar, String email, String name, String password) {
        this.aadhaar = aadhaar;
        this.email = email;
        this.name = name;
        this.password = password;
    }
}
