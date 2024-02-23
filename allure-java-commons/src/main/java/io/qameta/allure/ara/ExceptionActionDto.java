package io.qameta.allure.ara;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

import static io.qameta.allure.ara.AbstractActionDto.TYPE_EXCEPTION;

@JsonTypeName(TYPE_EXCEPTION)
public class ExceptionActionDto extends AbstractActionDto {

    private String message;
    private String stacktrace64;

    public ExceptionActionDto() {
        super(TYPE_EXCEPTION);
    }

    public ExceptionActionDto(
            String message,
            String stacktrace64
    ) {
        super(TYPE_EXCEPTION);
        this.message = message;
        this.stacktrace64 = stacktrace64;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStacktrace64() {
        return stacktrace64;
    }

    public void setStacktrace64(String stacktrace64) {
        this.stacktrace64 = stacktrace64;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        final ExceptionActionDto that = (ExceptionActionDto) obj;
        return Objects.equals(this.message, that.message) &&
                Objects.equals(this.stacktrace64, that.stacktrace64);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, stacktrace64);
    }

    @Override
    public String toString() {
        return "ExceptionActionDto[" +
                "message=" + message + ", " +
                "stacktrace64=" + stacktrace64 + ']';
    }

}
