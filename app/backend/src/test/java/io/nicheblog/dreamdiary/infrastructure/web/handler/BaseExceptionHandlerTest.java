package io.nicheblog.dreamdiary.infrastructure.web.handler;

import io.nicheblog.dreamdiary.global.exception.BusinessException;
import io.nicheblog.dreamdiary.global.exception.DuplicateException;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.context.request.WebRequest;

import javax.persistence.EntityNotFoundException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseExceptionHandlerTest {

    @Mock
    private ApplicationEventPublisherWrapper publisher;

    private BaseExceptionHandler handler;
    private WebRequest request;

    @BeforeEach
    void setUp() {
        handler = new BaseExceptionHandler(publisher);
        request = mock(WebRequest.class);
        when(request.getHeader(anyString())).thenAnswer(invocation ->
                "Accept".equals(invocation.getArgument(0)) ? "application/json" : null
        );
    }

    @Test
    void businessExceptionReturnsBadRequest() {
        final BusinessException exception = new BusinessException("business rule");
        assertResponse(() -> handler.handleBadRequestException(exception, request), exception, HttpStatus.BAD_REQUEST, "business error");
    }

    @Test
    void entityNotFoundReturnsNotFound() {
        final EntityNotFoundException exception = new EntityNotFoundException("not found");
        assertResponse(() -> handler.handleEntityNotFoundException(exception, request), exception, HttpStatus.NOT_FOUND, "not found");
    }

    @Test
    void duplicateReturnsConflict() {
        final DuplicateException exception = new DuplicateException("duplicate");
        assertResponse(() -> handler.handleConflictException(exception, request), exception, HttpStatus.CONFLICT, "duplicate");
    }

    @Test
    void bindExceptionReturnsFirstValidationMessage() {
        final BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.reject("required", "required field");
        final BindException exception = new BindException(bindingResult);

        try (MockedStatic<MessageUtils> messages = mockStatic(MessageUtils.class)) {
            messages.when(() -> MessageUtils.getExceptionMsg(exception)).thenReturn("binding detail");
            messages.when(() -> MessageUtils.getMessage("required field")).thenReturn("required field");

            assertAjaxResponse(
                    handler.handleBingdingException(exception, request),
                    HttpStatus.BAD_REQUEST,
                    "required field"
            );
        }
    }

    @Test
    void unexpectedExceptionHidesInternalMessage() {
        final RuntimeException exception = new RuntimeException("database detail");

        try (MockedStatic<MessageUtils> messages = mockStatic(MessageUtils.class)) {
            messages.when(() -> MessageUtils.getExceptionMsg(exception)).thenReturn("database detail");
            messages.when(() -> MessageUtils.getMessage("msg.rslt.exception")).thenReturn("safe server error");

            assertAjaxResponse(
                    handler.generalException(exception, request),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "safe server error"
            );
        }
    }

    private void assertResponse(
            final Supplier<Object> resultSupplier,
            final Exception exception,
            final HttpStatus status,
            final String message
    ) {
        try (MockedStatic<MessageUtils> messages = mockStatic(MessageUtils.class)) {
            messages.when(() -> MessageUtils.getExceptionMsg(exception)).thenReturn(message);
            assertAjaxResponse(resultSupplier.get(), status, message);
        }
    }

    private void assertAjaxResponse(final Object result, final HttpStatus status, final String message) {
        assertThat(result).isInstanceOf(ResponseEntity.class);
        final ResponseEntity<?> response = (ResponseEntity<?>) result;
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isInstanceOf(AjaxResponse.class);
        final AjaxResponse body = (AjaxResponse) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getRslt()).isFalse();
        assertThat(body.getStatus()).isEqualTo(status.value());
        assertThat(body.getMessage()).isEqualTo(message);
    }
}
