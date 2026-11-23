package in.jansetu.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.jansetu.common.response.ApiError;
import in.jansetu.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/** Writes the standard error envelope from filters (which run before GlobalExceptionHandler can help). */
@Component
@RequiredArgsConstructor
public class JsonErrorWriter {
    private final ObjectMapper objectMapper;

    public void write(HttpServletRequest req, HttpServletResponse res, HttpStatus status, String code, String message)
            throws IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(res.getOutputStream(),
                ApiResponse.fail(new ApiError(code, message, req.getRequestURI(), List.of())));
    }
}
