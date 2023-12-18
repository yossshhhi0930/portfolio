package com.example.portfolio.form;

import java.nio.file.Path;

import javax.validation.constraints.NotEmpty;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class DiaryImageForm {
	
	private Long id;

	private Long diaryId;

	private Path path;

	private MultipartFile image;

}
