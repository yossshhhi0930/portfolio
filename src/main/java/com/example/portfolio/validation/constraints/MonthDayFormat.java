//package com.example.portfolio.validation.constraints;
//
//import java.lang.annotation.Documented;
//import java.lang.annotation.ElementType;
//import java.lang.annotation.Retention;
//import java.lang.annotation.RetentionPolicy;
//import java.lang.annotation.Target;
//import javax.validation.Constraint;
//import javax.validation.Payload;
//import javax.validation.ReportAsSingleViolation;
//
//@Documented
//@Constraint(validatedBy = MonthDayFormatValidator.class)
//@Target({ ElementType.FIELD })
//@Retention(RetentionPolicy.RUNTIME)
//@ReportAsSingleViolation
//public @interface MonthDayFormat {
//
//    String message() default "指定されたフォーマットで入力してください。";
//
//    Class<?>[] groups() default {};
//
//    Class<? extends Payload>[] payload() default {};
//
//    @Target({ ElementType.FIELD })
//    @Retention(RetentionPolicy.RUNTIME)
//    @Documented
//    public @interface List {
//        MonthDayFormat[] value();
//    }
//}
