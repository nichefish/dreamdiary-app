package io.nicheblog.dreamdiary.infrastructure.log.entity;

import io.nicheblog.dreamdiary.auth.security.entity.AuditorInfo;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeItemEntity;
import io.nicheblog.dreamdiary.infrastructure.log.type.LogType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * LogEntity
 * <pre>
 *  통합 로그 Entity.
 * </pre>
 */
@Entity
@Table(name = "log")
@DynamicInsert
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class LogEntity
        extends BaseCrudEntity
        implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("로그 고유 ID")
    private Integer id;

    @CreatedBy
    @Column(name = "username", length = 20)
    @Comment("작업자 계정명")
    private String username;

    @Column(name = "trace_id", length = 72)
    private String traceId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "username", referencedColumnName = "username", insertable = false, updatable = false)
    @Fetch(value = FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    private AuditorInfo userInfo;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = DateUtils.PTN_DATETIME)
    @Column(name = "created_at", updatable = false)
    @Comment("기록 일시")
    private Date createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_type", length = 20)
    private LogType logType;

    @Column(name = "activity_code", length = 400)
    @Comment("활동 카테고리 코드")
    private String activityCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(formula = @JoinFormula(value = "\'" + Code.ACTVTY_CTGR_CD + "\'", referencedColumnName = "group_code")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "activity_code", referencedColumnName = "code", insertable = false, updatable = false))
    })
    @Fetch(value = FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    private CodeItemEntity activityCtgrInfo;

    @Column(name = "http_method", length = 1000)
    @Comment("HTTP 메소드")
    private String httpMethod;

    @Column(name = "request_uri", length = 400)
    @Comment("요청 URI")
    private String requestUri;

    @Column(name = "signature", length = 200)
    private String signature;

    @Column(name = "http_status")
    @Schema(description = "HTTP 상태 코드")
    private Integer httpStatus;

    @Column(name = "duration_ms")
    @Schema(description = "소요 시간(ms)")
    private Long durationMs;

    @Column(name = "request_param", length = 1000)
    @Comment("요청 파라미터")
    private String requestParam;

    @Column(name = "message", columnDefinition = "LONGTEXT")
    @Comment("통합 메시지")
    private String message;

    @Column(name = "referer", length = 1000)
    @Comment("리퍼러")
    private String referer;

    @Column(name = "ip_address", length = 20)
    @Comment("작업자 IP")
    private String ipAddress;

    @Column(name = "result")
    @Comment("작업 결과")
    private Boolean result;

    @Column(name = "exception_name", length = 255)
    @Comment("예외 이름")
    private String exceptionName;

    @Column(name = "exception_message", columnDefinition = "LONGTEXT")
    @Comment("예외 메시지")
    private String exceptionMessage;

    public HashMap<String, String> getParamMap() {
        final HashMap<String, String> paramMap = new HashMap<>();
        final Set<String> excludedParams = Set.of("isMngr", "isMngrMode", "isDev");

        Optional.ofNullable(this.getRequestParam())
                .filter(param -> !param.isEmpty())
                .ifPresent(paramStr ->
                        Arrays.stream(paramStr.split("&"))
                                .map(param -> param.split("="))
                                .filter(keyValue -> keyValue.length == 2)
                                .forEach(keyValue -> {
                                    final String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                                    if (!excludedParams.contains(key)) {
                                        final String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                                        paramMap.put(key, value);
                                    }
                                })
                );

        return paramMap;
    }
}
