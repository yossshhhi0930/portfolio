package com.example.portfolio.form;

import java.time.MonthDay;
import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.portfolio.form.UserForm;

import lombok.Data;

@Data
public class CropForm {

    private Long id;
    
    @NotEmpty
    private String name;
    
    @NotNull
    private MonthDay sowing_start;
    
    @NotNull
    private MonthDay sowing_end;
  
    @Min(value = 1, message = "値は1以上である必要があります。")
    private int cultivationp_period;
    
    private String manual;
    
}
