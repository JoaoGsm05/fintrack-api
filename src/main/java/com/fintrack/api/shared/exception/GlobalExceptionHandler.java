package com.fintrack.api.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI TYPE_VALIDATION   = URI.create("urn:fintrack:error:validation");
    private static final URI TYPE_BUSINESS     = URI.create("urn:fintrack:error:business");
    private static final URI TYPE_AUTH         = URI.create("urn:fintrack:error:auth");
    private static final URI TYPE_NOT_FOUND    = URI.create("urn:fintrack:error:not-found");
    private static final URI TYPE_FORBIDDEN    = URI.create("urn:fintrack:error:forbidden");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "O valor informado é inválido",
                        (a, b) -> a));

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        pd.setType(TYPE_VALIDATION);
        pd.setTitle("Quase lá! Alguns campos precisam de atenção");
        pd.setDetail("Verifique os campos abaixo para prosseguirmos com a sua solicitação.");
        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setType(TYPE_BUSINESS);
        pd.setTitle("Este e-mail já está em uso");
        pd.setDetail("Parece que você já tem uma conta conosco. Tente fazer login ou recupere sua senha.");
        return pd;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setType(TYPE_AUTH);
        pd.setTitle("Ops! Credenciais inválidas");
        pd.setDetail("E-mail ou senha incorretos. Verifique seus dados e tente novamente.");
        return pd;
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ProblemDetail handleInvalidToken(InvalidTokenException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setType(TYPE_AUTH);
        pd.setTitle("Sessão expirada ou inválida");
        pd.setDetail("Para sua segurança, sua sessão expirou. Por favor, faça login novamente.");
        return pd;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setType(TYPE_NOT_FOUND);
        pd.setTitle("Recurso não encontrado");
        pd.setDetail(ex.getMessage() + ". Verifique se o item ainda existe ou se você tem acesso a ele.");
        return pd;
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        pd.setType(TYPE_BUSINESS);
        pd.setTitle("Não foi possível concluir esta ação");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setType(TYPE_FORBIDDEN);
        pd.setTitle("Acesso Restrito");
        pd.setDetail("Você não possui as permissões necessárias para acessar este recurso.");
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setType(URI.create("urn:fintrack:error:internal"));
        pd.setTitle("Algo não saiu como o esperado");
        pd.setDetail("Tivemos um problema interno, mas nossa equipe já foi notificada. Tente novamente em alguns instantes.");
        return pd;
    }
}
