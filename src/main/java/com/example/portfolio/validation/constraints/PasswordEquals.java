package com.example.portfolio.validation.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;

@Documented
@Constraint(validatedBy = PasswordEqualsValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@ReportAsSingleViolation
public @interface PasswordEquals {

    String message() default "パスワードと確認用パスワードが一致しません。";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
