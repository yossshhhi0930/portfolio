package com.example.portfolio.form;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data

public class CreateReissueInfoForm implements Serializable {

 private static final long serialVersionUID = 1L;

private static final RetentionPolicy RUNTIME = null;


 @NotEmpty
 private String Email;
}