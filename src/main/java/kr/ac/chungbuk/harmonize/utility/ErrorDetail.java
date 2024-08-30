package kr.ac.chungbuk.harmonize.utility;

import lombok.Getter;
import org.springframework.context.MessageSource;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.Locale;

@Getter
public class ErrorDetail {

    private String objectName;
    private String field;
    private String code;
    private String message;

    public ErrorDetail(FieldError fieldError, MessageSource messageSource, Locale locale) {
        this.objectName = fieldError.getObjectName();
        this.field = fieldError.getField();
        this.code = fieldError.getCode();
        this.message = messageSource.getMessage(fieldError, locale);
    }

    public ErrorDetail(ObjectError objectError, MessageSource messageSource, Locale locale) {
        this.objectName = objectError.getObjectName();
        this.code = objectError.getCode();
        this.message = messageSource.getMessage(objectError, locale);
    }
}
