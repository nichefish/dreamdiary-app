package io.nicheblog.dreamdiary.feature.attachable._shared.model;

import lombok.*;

import javax.validation.constraints.Positive;

/**
 * 저널 목록 캐시에 영향을 주는 부착 가능 컨텐츠 변경 요청용 공통 캐시 컨텍스트.
 *
 * <p>브라우저가 현재 보고 있는 월/주 정보를 함께 보내면, 서버는 전체 목록을 다시 만들지 않고
 * 보조 맵 캐시만 부분 갱신할 수 있다.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachableCacheContext {

    @Positive
    private Integer yy;

    @Positive
    private Integer mnth;

    private String weekStartDt;
}
