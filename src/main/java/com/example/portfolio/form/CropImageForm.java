package com.example.portfolio.form;

import java.nio.file.Path;


import javax.validation.constraints.NotEmpty;

import org.springframework.web.multipart.MultipartFile;



import lombok.Data;

@Data
public class CropImageForm {
private Long id;
	
	@NotEmpty
	private Long cropId;
	
	@NotEmpty
    private Path path;
	
	@NotEmpty
    private MultipartFile image;
	
    private CropForm cropform;
}
