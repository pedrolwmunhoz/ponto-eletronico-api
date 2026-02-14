package com.pontoeletronico.api.exception;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static String currentPath() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            if (request != null) {
                return request.getRequestURI();
            }
        }
        return null;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var bindingResult = ex.getBindingResult();
        var fieldMsgs = bindingResult.getFieldErrors().stream()
                .map(e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "");
        var globalMsgs = bindingResult.getGlobalErrors().stream()
                .map(e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "");
        var mensagem = Stream.concat(fieldMsgs, globalMsgs)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining("; "));
        int status = HttpStatus.BAD_REQUEST.value();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErroResponse.of(mensagem.isBlank() ? "Erro de validação" : mensagem, status, currentPath()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String mensagem = "Corpo da requisição inválido ou ausente. Verifique o JSON enviado.";
        if (ex.getCause() != null && ex.getCause().getMessage() != null && ex.getCause().getMessage().contains("Required request body is missing")) {
            mensagem = "Corpo da requisição é obrigatório.";
        }
        int status = HttpStatus.BAD_REQUEST.value();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErroResponse.of(mensagem, status, currentPath()));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErroResponse> handleApiException(ApiException ex) {
        int status = ex.getStatus().value();
        return ResponseEntity.status(ex.getStatus()).body(ErroResponse.of(ex.getMensagem(), status, currentPath()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErroResponse> handleRuntimeException(RuntimeException ex) {
        var message = ex.getMessage();
        if (message != null && message.contains("não encontrado")) {
            int status = HttpStatus.UNAUTHORIZED.value();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErroResponse.of(message, status, currentPath()));
        }
        int status = HttpStatus.BAD_REQUEST.value();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErroResponse.of(message != null ? message : MensagemErro.ERRO.getMensagem(), status, currentPath()));
    }
}
