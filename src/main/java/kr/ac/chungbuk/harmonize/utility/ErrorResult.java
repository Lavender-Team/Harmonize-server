package kr.ac.chungbuk.harmonize.utility;

import lombok.Getter;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.Locale;

@Getter
public class ErrorResult {

    private List<ErrorDetail> fieldErrors;
    private List<ErrorDetail> objectErrors;

    public ErrorResult(Errors errors, MessageSource messageSource, Locale locale) {
        if (errors.hasFieldErrors()) {
            this.fieldErrors = errors.getFieldErrors()
                    .stream()
                    .map(error -> new ErrorDetail(error, messageSource, locale))
                    .toList();
        }

        if (errors.hasGlobalErrors()) {
            this.objectErrors = errors.getGlobalErrors()
                    .stream()
                    .map(error -> new ErrorDetail(error, messageSource, locale))
                    .toList();
        }
    }
}
