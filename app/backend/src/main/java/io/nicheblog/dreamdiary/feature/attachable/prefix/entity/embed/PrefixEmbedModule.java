package io.nicheblog.dreamdiary.feature.attachable.prefix.entity.embed;

import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixContentEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixEntity;

/**
 * PrefixEmbedModule
 * <pre>
 *   Prefix(말머리 선택) 임베드 모듈 인터페이스.
 *   콘텐츠 엔티티가 구현하여 prefix_content 연결을 조립·조회한다. (meta/state 모듈과 동일 방식.)
 * </pre>
 *
 * @author nichefish
 */
public interface PrefixEmbedModule {

    /** Getter */
    PrefixEmbed getPrefix();

    /** Setter */
    void setPrefix(PrefixEmbed embed);

    /**
     * 선택된 말머리 연결(0..1)을 반환한다. 없으면 {@code null}.
     * @return 말머리 연결 또는 null
     */
    default PrefixContentEntity getSelectedPrefixContent() {
        if (this.getPrefix() == null) return null;
        return this.getPrefix().getSelected();
    }

    /**
     * 선택된 말머리 ID를 반환한다. 없으면 {@code null}.
     * @return 말머리 ID 또는 null
     */
    default Integer getSelectedPrefixId() {
        final PrefixContentEntity selected = this.getSelectedPrefixContent();
        return selected == null ? null : selected.getPrefixId();
    }

    /**
     * 선택된 말머리 엔티티(표시용)를 반환한다. 없으면 {@code null}.
     * @return 말머리 엔티티 또는 null
     */
    default PrefixEntity getSelectedPrefix() {
        final PrefixContentEntity selected = this.getSelectedPrefixContent();
        return selected == null ? null : selected.getPrefix();
    }
}
