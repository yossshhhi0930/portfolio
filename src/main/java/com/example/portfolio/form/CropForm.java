package com.example.portfolio.form;

import java.time.MonthDay;
import java.util.Date;

import javax.validation.constraints.NotEmpty;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.portfolio.form.UserForm;

import lombok.Data;

@Data
public class CropForm {

    private Long id;

    private Long userId;

 
    private String name;
  

    private String manual;
    
  
    private MonthDay sowing_start;
    
    private MonthDay sowing_end;
    
   
    private MonthDay harvest_start;
    
    
    private MonthDay harvest_end;
    
    private UserForm user;
    
   
    private int cultivationp_period;
    
    
}
