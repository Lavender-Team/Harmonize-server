package kr.ac.chungbuk.harmonize.utility;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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

    public static ErrorResult SimpleErrorReturn(String errorCode, MessageSource messageSource, Locale locale) {
        ErrorResult errorResult = new ErrorResult();
        errorResult.objectErrors = new ArrayList<>();
        errorResult.objectErrors.add(new ErrorDetail(
                new ObjectError("controller", new String[] { errorCode }, new Object[]{ }, null),
                messageSource, locale
        ));

        return errorResult;
    }
}
