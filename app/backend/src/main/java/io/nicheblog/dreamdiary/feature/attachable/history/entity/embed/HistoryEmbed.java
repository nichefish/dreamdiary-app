package io.nicheblog.dreamdiary.feature.attachable.history.entity.embed;

import io.nicheblog.dreamdiary.auth.security.entity.AuditorInfo;
import io.nicheblog.dreamdiary.auth.security.util.AuditorUtils;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * HistoryEmbed
 * <pre>
 *  임베드:: 마지막 본문 수정 정보. (entity level)
 * </pre>
 *
 * @author nichefish
 * @see HistoryEmbedModule
 */
@Embeddable
@Getter
@Setter
@Builder
@AllArgsConstructor
public class HistoryEmbed
        implements Serializable {

    @PostLoad
    private void onLoad() {
        this.isHistoryTriggeredBy = AuthUtils.isCreatedBy(this.historyTriggeredBy);
    }

    /** 마지막 본문 수정자 ID */
    @Column(name = "history_triggered_by", length = 20)
    private String historyTriggeredBy;

    /** 마지막 본문 수정자 정보 :: join 제거하고 캐시 처리 */
    @Transient
    private AuditorInfo historyTriggeredByInfo;

    @Transient
    private String historyTriggeredByNm;

    /** 현재 로그인 사용자의 마지막 본문 수정 여부 */
    @Transient
    private Boolean isHistoryTriggeredBy;

    /** 마지막 본문 수정일시 */
    @DateTimeFormat(pattern = DateUtils.PTN_DATETIME)
    @Column(name = "history_triggered_at")
    private LocalDateTime historyTriggeredAt;

    public HistoryEmbed() {
        this.historyTriggeredBy = AuthUtils.getLoginUsername();
    }

    public HistoryEmbed(final Boolean updtLastModifiedDt) {
        this();
        if (updtLastModifiedDt) this.historyTriggeredAt = DateUtils.getCurrLocalDateTime();
    }

    public AuditorInfo getHistoryTriggeredByInfo() {
        if (StringUtils.isEmpty(this.historyTriggeredBy)) return null;
        if (this.historyTriggeredByInfo == null) {
            this.historyTriggeredByInfo = AuditorUtils.getAuditorInfo(this.historyTriggeredBy);
        }
        return this.historyTriggeredByInfo;
    }
}
