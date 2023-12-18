package com.example.portfolio.form;

import java.time.MonthDay;
import java.util.Date;

import javax.validation.constraints.NotEmpty;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.portfolio.form.UserForm;

import lombok.Data;

@Data
public class SectionForm {

    private Long id;

    @NotEmpty
    private String name;
    
    private String description;

}
