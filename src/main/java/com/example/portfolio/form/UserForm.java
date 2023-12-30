package com.example.portfolio.form;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import com.example.portfolio.validation.constraints.PasswordEquals;
import lombok.Data;

@Data
@PasswordEquals
public class UserForm {

	@NotEmpty
    @Size(min = 1, max = 20)
    private String name;
    
	@NotEmpty
	@Email
    private String email;

    @NotEmpty
    @Size(min = 4, max = 20)
    private String password;

    @NotEmpty
    @Size(min = 4, max = 20)
    private String passwordConfirmation;

}