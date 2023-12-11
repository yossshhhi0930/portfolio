package com.example.portfolio.form;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data

public class CreateReissueInfoForm{

 @NotEmpty
 private String Email;
}