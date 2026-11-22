package in.jansetu.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(String code, String message, String path, List<FieldError> fieldErrors) {
    public record FieldError(String field, String message) {}
}
