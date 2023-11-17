package com.example.portfolio.form;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.example.portfolio.validation.constraints.PasswordEquals;

import lombok.Data;

@Data
@PasswordEquals
public class PasswordResetForm implements Serializable{

    private static final long serialVersionUID = 1L;

    
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
