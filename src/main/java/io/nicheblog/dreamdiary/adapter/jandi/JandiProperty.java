package io.nicheblog.dreamdiary.adapter.jandi;

import io.nicheblog.dreamdiary.adapter.jandi.exception.JandiException;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JandiProperty
 * <pre>
 *  application.yml에서 잔디 메신저 관련 설정값을 가져온다.
 * </pre>
 *
 * @author nichefish
 */
@Component
@ConfigurationProperties(prefix = "api.jandi")
@Getter
@Setter
public class JandiProperty {

    /** 팀 고유 id */
    private String teamId;
    /** 토픽 정보 목록 */
    private List<Topic> topics;

    /* ----- */

    /**
     * 토픽 정보
     */
    @Getter
    @Setter
    public static class Topic {
        /** 토픽 이름 (구분용) */
        private String name;
        /** incoming webhook id (TODO: outgoing webhook 추가되면 이름 바꿔야함) */
        private String id;
    }

    /** 토픽 조회 */
    public String getId(final String name) {
        if (StringUtils.isEmpty(name)) throw new JandiException(MessageUtils.getMessage("jandi.rslt.team-not-found"));

        for (final Topic topic : this.topics) {
            if (name.equals(topic.getName())) return topic.getId();
        }
        throw new JandiException(MessageUtils.getMessage("jandi.rslt.team-not-found"));
    }
}
