package io.nicheblog.dreamdiary.global.handler;

import io.nicheblog.dreamdiary.extension.log.actvty.event.LogActvtyEvent;
import io.nicheblog.dreamdiary.extension.log.actvty.handler.LogActvtyEventListener;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.util.HttpUtils;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
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
        return handleException(e, request, status, "/view/global/_common/error/error_page");
    }

    /**
     * 예외 처리 공통 로직
     * Ajax 요청과 페이지뷰 요청을 구분하여 응답을 내려준다.
     *
     * @param e 처리할 예외
     * @param request 발생한 웹 요청 정보
     * @param status 반환할 HTTP 상태 코드
     * @param view 예외 발생 시 렌더링할 뷰 이름 (AJAX 요청이 아닐 때 사용)
     * @return Ajax 요청의 경우 {@link ResponseEntity}, 페이지 요청의 경우 {@link ModelAndView} 객체
     * @see LogActvtyEventListener
     */
    private Object handleException(final Exception e, final WebRequest request, final HttpStatus status, final String view) {
        final String errorMsg = MessageUtils.getExceptionMsg(e);
        log.warn("Exception handled: ", e);

        // 로그 처리
        final LogActvtyParam logParam = new LogActvtyParam(false, errorMsg);
        publisher.publishAsyncEvent(new LogActvtyEvent(this, logParam));

        // Ajax 요청인 경우
        if (HttpUtils.isAjaxRequest(request)) {
            AjaxResponse ajaxResponse = new AjaxResponse(false, errorMsg);
            return ResponseEntity
                    .status(status)
                    .body(ajaxResponse);
        }
        // 페이지 요청인 경우
        return new ModelAndView(view).addObject("errorMsg", errorMsg);
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
        return handleException(e, request, HttpStatus.NOT_FOUND, "/view/global/_common/error/error_not_found");
    }

    /**
     * {@link AccessDeniedException} - 요청한 리소스에 접근할 수 없을 때 발생하는 예외를 처리합니다.
     *
     * @param e 처리할 {@link AccessDeniedException}
     * @param request 발생한 웹 요청 정보
     * @return Ajax 요청의 경우 {@link ResponseEntity}, 페이지 요청의 경우 {@link ModelAndView} 객체
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public Object accessDenied(
            final AccessDeniedException e,
            final WebRequest request
    ) {
        return handleException(e, request, HttpStatus.FORBIDDEN, "/view/global/_common/error/error_access_denied");
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
        return handleException(e, request, HttpStatus.BAD_REQUEST);
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
