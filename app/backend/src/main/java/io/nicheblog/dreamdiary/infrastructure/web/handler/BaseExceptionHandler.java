package io.nicheblog.dreamdiary.infrastructure.web.handler;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.exception.BaseException;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import io.nicheblog.dreamdiary.global.exception.DuplicateException;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.event.LogEvent;
import io.nicheblog.dreamdiary.infrastructure.log.handler.LogEventListener;
import io.nicheblog.dreamdiary.infrastructure.log.model.LogParam;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import io.nicheblog.dreamdiary.infrastructure.web.util.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.persistence.EntityNotFoundException;

/**
 * BaseExceptionlHandler
 * <pre>
 *  Controller에서 catch되지 않는 exception 공통 처리 클래스.
 *  (각 exception별로 처리 메소드 생성 가능)
 *  "컨트롤러 메소드의 실행이 완료된 후 뷰를 렌더링하는 과정에서 발생하는 예외는 @ExceptionHandler로 처리되지 않습니다."
 * </pre>
 *
 * @author nichefish
 */
@ControllerAdvice
@RequiredArgsConstructor
@Log4j2
public class BaseExceptionHandler {

    private final ApplicationEventPublisherWrapper publisher;

    /**
     * 예외 처리 공통 로직
     * Ajax 요청과 페이지뷰 요청을 구분하여 응답을 내려준다.
     *
     * @param e 처리할 예외
     * @param request 요청 정보를 포함한 WebRequest 객체
     * @param status 반환할 HTTP 상태 코드
     * @return Ajax 요청의 경우 {@link ResponseEntity}, 페이지 요청의 경우 {@link ModelAndView} 객체
     */
    private Object handleException(final Exception e, final WebRequest request, final HttpStatus status) {
        return handleException(e, request, status, "general");
    }

    /**
     * 예외 처리 공통 로직
     * Ajax 요청과 페이지뷰 요청을 구분하여 응답을 내려준다.
     *
     * @param e 처리할 예외
     * @param request 발생한 웹 요청 정보
     * @param status 반환할 HTTP 상태 코드
     * @param errorType Vue 에러 페이지에서 사용할 에러 타입
     * @return Ajax 요청의 경우 {@link ResponseEntity}, 페이지 요청의 경우 {@link ModelAndView} 객체
     * @see LogEventListener
     */
    private Object handleException(final Exception e, final WebRequest request, final HttpStatus status, final String errorType) {
        return handleException(e, request, status, errorType, MessageUtils.getExceptionMsg(e));
    }

    /**
     * 예외 상세 로그와 사용자 응답 메시지를 분리하여 처리한다.
     *
     * @param e 처리할 예외
     * @param request 발생한 웹 요청 정보
     * @param status 반환할 HTTP 상태 코드
     * @param errorType Vue 에러 페이지에서 사용할 에러 타입
     * @param responseMsg 사용자 응답에 포함할 안전한 메시지
     * @return Ajax 요청의 경우 {@link ResponseEntity}, 페이지 요청의 경우 {@link ModelAndView} 객체
     */
    private Object handleException(
            final Exception e,
            final WebRequest request,
            final HttpStatus status,
            final String errorType,
            final String responseMsg
    ) {
        final String errorMsg = MessageUtils.getExceptionMsg(e);
        if (isExpectedException(e)) {
            log.warn(
                    "EXCEPTION_HANDLED type={} status={} message={}",
                    e.getClass().getSimpleName(),
                    status.value(),
                    errorMsg
            );
        } else {
            log.warn(
                    "EXCEPTION_HANDLED type={} status={} message={}",
                    e.getClass().getSimpleName(),
                    status.value(),
                    errorMsg,
                    e
            );
        }

        // 로그 처리
        final LogParam logParam = new LogParam(false, errorMsg);
        publisher.publishAsyncEvent(new LogEvent(this, logParam));

        // Ajax 요청인 경우
        if (HttpUtils.isAjaxRequest(request)) {
            final AjaxResponse ajaxResponse = new AjaxResponse(false, responseMsg);
            ajaxResponse.setStatus(status.value());
            return ResponseEntity
                    .status(status)
                    .body(ajaxResponse);
        }
        // 페이지 요청인 경우
        final String redirectUrl = UriComponentsBuilder
                .fromPath("/vue-app/error")
                .queryParam("type", errorType)
                .queryParam("message", responseMsg)
                .build()
                .encode()
                .toUriString();
        return new ModelAndView("redirect:" + redirectUrl);
    }

    /**
     * 검증 오류에서 첫 사용자 메시지를 추출한다.
     *
     * @param e Spring 바인딩 예외
     * @return 첫 검증 메시지 또는 공통 잘못된 인자 메시지
     */
    private String resolveBindingMessage(final BindException e) {
        return e.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .filter(message -> message != null && !message.isBlank())
                .map(MessageUtils::getMessage)
                .findFirst()
                .orElseGet(() -> MessageUtils.getExceptionMsg("IllegalArgumentException"));
    }

    private boolean isExpectedException(final Exception e) {
        return e instanceof BaseException
                || e instanceof BindException
                || e instanceof AccessDeniedException
                || e instanceof NotAuthorizedException
                || e instanceof NoHandlerFoundException;
    }

    /**
     * {@link NotAuthorizedException} — 리소스 소유·조회 권한 부재 시 처리 (Ajax 403).
     *
     * @param e 처리할 {@link NotAuthorizedException}
     * @param request 발생한 웹 요청 정보
     * @return Ajax 요청의 경우 {@link ResponseEntity}, 페이지 요청의 경우 {@link ModelAndView} 객체
     */
    @ExceptionHandler(NotAuthorizedException.class)
    public Object handleNotAuthorizedException(
            final NotAuthorizedException e,
            final WebRequest request
    ) {
        return handleException(e, request, HttpStatus.FORBIDDEN, "access_denied");
    }

    /**
     * {@link NoHandlerFoundException} - 요청한 핸들러를 찾을 수 없을 때 발생하는 예외를 처리합니다.
     *
     * @param e 처리할 {@link NoHandlerFoundException}
     * @param request 발생한 웹 요청 정보
     * @return Ajax 요청의 경우 {@link ResponseEntity}, 페이지 요청의 경우 {@link ModelAndView} 객체
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handleNoHandlerFoundException(
            final NoHandlerFoundException e,
            final WebRequest request
    ) {
        return handleException(e, request, HttpStatus.NOT_FOUND, "not_found");
    }

    /**
     * {@link AccessDeniedException} - 요청한 리소스에 접근할 수 없을 때 발생하는 예외를 처리합니다.
     *
     * @param e 처리할 {@link AccessDeniedException}
     * @param request 발생한 웹 요청 정보
     * @return Ajax 요청의 경우 {@link ResponseEntity}, 페이지 요청의 경우 {@link ModelAndView} 객체
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Object accessDenied(
            final AccessDeniedException e,
            final WebRequest request
    ) {
        return handleException(e, request, HttpStatus.FORBIDDEN, "access_denied");
    }

    /**
     * {@link BindException} - Spring Validation을 통과하지 못했을 때 발생하는 예외를 처리합니다.
     *
     * @param e 처리할 {@link BindException}
     * @param request 발생한 웹 요청 정보
     * @return Ajax 요청의 경우 {@link ResponseEntity}, 페이지 요청의 경우 {@link ModelAndView} 객체
     */
    @ExceptionHandler(BindException.class)
    public Object handleBingdingException(
            final BindException e,
            final WebRequest request
    ) {
        return handleException(e, request, HttpStatus.BAD_REQUEST, "bad_request", resolveBindingMessage(e));
    }

    /** 클라이언트 요청 값 또는 비즈니스 규칙 위반을 400으로 응답한다. */
    @ExceptionHandler({BusinessException.class, IllegalArgumentException.class})
    public Object handleBadRequestException(final Exception e, final WebRequest request) {
        return handleException(e, request, HttpStatus.BAD_REQUEST, "bad_request");
    }

    /** 존재하지 않는 리소스 조회·수정·삭제를 404로 응답한다. */
    @ExceptionHandler(EntityNotFoundException.class)
    public Object handleEntityNotFoundException(final EntityNotFoundException e, final WebRequest request) {
        return handleException(e, request, HttpStatus.NOT_FOUND, "not_found");
    }

    /** 중복 등록과 데이터 무결성 충돌을 409로 응답한다. */
    @ExceptionHandler({DuplicateException.class, DataIntegrityViolationException.class})
    public Object handleConflictException(final Exception e, final WebRequest request) {
        return handleException(e, request, HttpStatus.CONFLICT, "conflict");
    }

    /**
     * {@link Exception} - 공통 예외를 처리합니다.
     *
     * @param e 처리할 {@link Exception}
     * @param request 발생한 웹 요청 정보
     * @return Ajax 요청의 경우 {@link ResponseEntity}, 페이지 요청의 경우 {@link ModelAndView} 객체
     */
    @ExceptionHandler(Exception.class)
    public Object generalException(
            final Exception e,
            final WebRequest request
    ) {
        return handleException(
                e,
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "general",
                MessageUtils.getMessage("msg.rslt.exception")
        );
    }
}
