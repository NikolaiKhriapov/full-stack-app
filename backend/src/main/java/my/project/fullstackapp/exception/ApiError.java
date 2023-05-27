package my.project.fullstackapp.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ApiError(
        String path,
        String message,
        int statusCode,
        HttpStatus statusMessage,
        LocalDateTime localDateTime
) {
}
