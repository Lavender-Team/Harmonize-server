package kr.ac.chungbuk.harmonize.exception;

import kr.ac.chungbuk.harmonize.controller.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.context.MessageSource;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import static kr.ac.chungbuk.harmonize.exception.ErrorResult.SimpleErrorReturn;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice(assignableTypes = {
        MusicController.class,
        MusicActionController.class,
        MusicAnalysisController.class,
        ArtistController.class,
        GroupController.class,
        LogController.class
})
public class ExControllerAdvice {

    private final MessageSource messageSource;

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResult handleException(MethodArgumentNotValidException ex) {
        return new ErrorResult(ex.getBindingResult(), messageSource, Locale.getDefault());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResult handleException(IllegalArgumentException ex, HandlerMethod handlerMethod) {
        return SimpleErrorReturn(
                getErrorCode("illegalArgument", getMethodName(handlerMethod), getClassName(handlerMethod)),
                messageSource,
                Locale.getDefault()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SizeLimitExceededException.class)
    public ErrorResult handleException(SizeLimitExceededException ex, HandlerMethod handlerMethod) {
        return SimpleErrorReturn(
                getErrorCode("sizeLimitFailed", getMethodName(handlerMethod), getClassName(handlerMethod)),
                messageSource,
                Locale.getDefault()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IncorrectResultSizeDataAccessException.class)
    public ErrorResult handleException(IncorrectResultSizeDataAccessException ex, HandlerMethod handlerMethod) {
        return SimpleErrorReturn(
                getErrorCode("duplicatedNameFailed", getMethodName(handlerMethod), getClassName(handlerMethod)),
                messageSource,
                Locale.getDefault()
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(TimeoutException.class)
    public ErrorResult handleException(TimeoutException ex, HandlerMethod handlerMethod) {
        return SimpleErrorReturn(
                getErrorCode("timeout", getMethodName(handlerMethod), getClassName(handlerMethod)),
                messageSource,
                Locale.getDefault()
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IOException.class)
    public ErrorResult handleException(IOException ex, HandlerMethod handlerMethod) {
        return SimpleErrorReturn(
                getErrorCode("ioFailed", getMethodName(handlerMethod), getClassName(handlerMethod)),
                messageSource,
                Locale.getDefault()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    public ErrorResult handleException(NoSuchElementException ex, HandlerMethod handlerMethod) {
        return SimpleErrorReturn(
                getErrorCode("notFound", getMethodName(handlerMethod), getClassName(handlerMethod)),
                messageSource,
                Locale.getDefault()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchFileException.class)
    public ErrorResult handleException(NoSuchFileException ex, HandlerMethod handlerMethod) {
        return SimpleErrorReturn(
                getErrorCode("notFound", getMethodName(handlerMethod), getClassName(handlerMethod)),
                messageSource,
                Locale.getDefault()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(FileNotFoundException.class)
    public ErrorResult handleException(FileNotFoundException ex, HandlerMethod handlerMethod) {
        return SimpleErrorReturn(
                getErrorCode("fileNotFound", getMethodName(handlerMethod), getClassName(handlerMethod)),
                messageSource,
                Locale.getDefault()
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResult handleException(Exception ex, HandlerMethod handlerMethod) {
        return SimpleErrorReturn(
                getErrorCode("exception", getMethodName(handlerMethod), getClassName(handlerMethod)),
                messageSource,
                Locale.getDefault()
        );
    }


    private String getClassName(HandlerMethod handlerMethod) {
        String packageClassName = handlerMethod.getBeanType().getName();
        return packageClassName.substring(packageClassName.lastIndexOf(".") + 1);
    }

    private String getMethodName(HandlerMethod handlerMethod) {
        return handlerMethod.getMethod().getName();
    }

    private String[] getErrorCode(String reason, String methodName, String className) {
        return new String[] {
                reason + "." + methodName + "." + className,
                reason + "." + className,
                methodName + "." + className,
                reason
        };
    }

}
