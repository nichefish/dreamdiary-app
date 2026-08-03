package io.nicheblog.dreamdiary.feature.journal.reflection.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.history.HistoryType;
import io.nicheblog.dreamdiary.feature.attachable.history.model.HistoryActionModule;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstn;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Reflection(Commentary) 쓰기 DTO.
 *
 * <p>Reflection 은 별도 Aggregate(journal_reflection)이며 대상 필수(About-A)다. Primary 스트림의 peer 가
 * 아니므로 chapter·sortOrder 를 갖지 않고, 대상({@code refId}/{@code refContentType})을 필수로 싣는다.
 * 태그는 두지 않는다(딸린 Reflection 태그 없음, 도메인 §1.4/§5.2).</p>
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JournalReflectionPostDto extends BaseAttachableDto
        implements Identifiable<Integer>, FileCmpstnModule, CommentCmpstnModule, HistoryActionModule {

    /** 제목 (선택) */
    private String title;
    /** 본문(사유) */
    private String content;
    /** 대상(About-A) 엔티티 번호. 필수. */
    private Integer refId;
    /** 대상 엔티티 콘텐츠 타입 {JOURNAL_DIARY, JOURNAL_DREAM, JOURNAL_REFLECTION}. 필수. */
    private ContentType refContentType;
    /** 전용 테이블이라 항상 JOURNAL_REFLECTION. attachable join referencedColumnName 으로 저장. */
    @Builder.Default
    private String contentType = ContentType.JOURNAL_REFLECTION.key;
    /** 이력 시작점 (복원 등) */
    private Integer fromHistoryId;

    @Builder.Default
    private String historyType = HistoryType.CHANGE.key;

    public FileCmpstn file;
    public CommentCmpstn comment;

    /**
     * 식별 키를 반환한다.
     *
     * @return Reflection ID
     */
    @Override
    public Integer getKey() {
        return this.id;
    }
}
