package com.example.portfolio.form;

import java.time.MonthDay;
import java.util.Date;

import javax.validation.constraints.NotEmpty;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class CropForm {

    private Long id;

    private Long userId;

    
    private String name;
  
    
    private String manual;
    
   
    private String sowing_start;
    
    
    private Date sowing_end;
    
   
    private Date harvest_start;
    
    
    private Date harvest_end;
    
   
    private int cultivationp_period;
    
    
}
