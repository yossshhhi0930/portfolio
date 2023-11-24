package com.example.portfolio.form;

import java.util.Date;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class CropForm {

    private Long id;

    private Long userId;

    @NotEmpty
    private String name;
  
    
    private String manual;
    
   
    private Date sowing_start;
    
    
    private Date sowing_end;
    
   
    private Date harvest_start;
    
    
    private Date harvest_end;
    
   
    private int cultivationp_period;
    
    
}
