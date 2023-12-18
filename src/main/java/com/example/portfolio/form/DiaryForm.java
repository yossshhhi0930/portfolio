package com.example.portfolio.form;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.example.portfolio.entity.DiaryImage;
import com.example.portfolio.form.UserForm;

import lombok.Data;

@Data
public class DiaryForm {
	
    private Long id;
    
    @NotNull
    private Long planId;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate record_date;
    
    private String description;
    
    
}
