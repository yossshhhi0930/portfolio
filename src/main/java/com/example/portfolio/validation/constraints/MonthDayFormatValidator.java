//package com.example.portfolio.validation.constraints;
//
//import java.time.DateTimeException;
//import java.time.MonthDay;
//import java.time.format.DateTimeFormatter;
//
//import javax.validation.ConstraintValidator;
//import javax.validation.ConstraintValidatorContext;
//
//
//public class MonthDayFormatValidator implements ConstraintValidator<MonthDayFormat, Object> {
//
//	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");
//	 private static final String INVALID_FORMAT_MESSAGE = "日付の形式が正しくありません。例: MM-dd";
//    @Override
//    public void initialize(MonthDayFormat annotation) {
//   
//    }
//
//    @Override
//    public boolean isValid(Object value, ConstraintValidatorContext context) {
//    	if (value == null) {
//            return true;  // nullの場合はバリデートしない
//        }
//
//        try {
//            if (value instanceof MonthDay) {
//                MonthDay monthDay = (MonthDay) value;
//                String formattedValue = monthDay.format(DateTimeFormatter.ofPattern("MM-dd"));
//                MonthDay parsedMonthDay = MonthDay.parse(formattedValue, DateTimeFormatter.ofPattern("MM-dd"));
//                return monthDay.equals(parsedMonthDay);
//            } else {
//                throw new DateTimeException(INVALID_FORMAT_MESSAGE);
//            }
//        } catch (DateTimeException e) {
//            context.disableDefaultConstraintViolation();
//            context.buildConstraintViolationWithTemplate(INVALID_FORMAT_MESSAGE).addConstraintViolation();
//            return false;
//        }
//    }
//
//}
