package com.example.portfolio.form;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.example.portfolio.validation.constraints.PasswordEquals;

import lombok.Data;

@Data
@PasswordEquals
public class PasswordResetForm{

    private String email;

    
    private String token;

    @NotNull
    private String secret;

    @NotNull
    @Size(min = 4,max = 20)
    private String password;

    @NotNull
    @Size(min = 4, max = 20)
    private String passwordConfirmation;
}
