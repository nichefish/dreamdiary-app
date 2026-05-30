package io.nicheblog.dreamdiary.infrastructure.web.handler;

import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.exception.BaseException;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.event.LogEvent;
import io.nicheblog.dreamdiary.infrastructure.log.handler.LogEventListener;
import io.nicheblog.dreamdiary.infrastructure.log.model.LogParam;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import io.nicheblog.dreamdiary.infrastructure.web.util.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
            AjaxResponse ajaxResponse = new AjaxResponse(false, errorMsg);
            return ResponseEntity
                    .status(status)
                    .body(ajaxResponse);
        }
        // 페이지 요청인 경우
        final String redirectUrl = UriComponentsBuilder
                .fromPath("/vue-app/error")
                .queryParam("type", errorType)
                .queryParam("message", errorMsg)
                .build()
                .encode()
                .toUriString();
        return new ModelAndView("redirect:" + redirectUrl);
    }

    private boolean isExpectedException(final Exception e) {
        return e instanceof BaseException
                || e instanceof BindException
                || e instanceof AccessDeniedException
                || e instanceof NoHandlerFoundException;
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
        return handleException(e, request, HttpStatus.BAD_REQUEST, "bad_request");
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
        return handleException(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
