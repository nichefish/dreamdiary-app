package io.nicheblog.dreamdiary.extension.clsf.state.entity.embed;

import io.nicheblog.dreamdiary.extension.clsf.state.entity.StateEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * StateEmbedModule
 * <pre>
 *   상태 모듈 인터페이스
 * </pre>
 *
 * @author nichefish
 * @see MapstructHelper
 */
public interface StateEmbedModule {
    /** Getter */
    StateEmbed getState();

    /** Setter */
    void setState(StateEmbed embed);

    /** 상태 목록 */
    default List<StateEntity> getList() {
        if (this.getState() == null) return new ArrayList<>();

        return this.getState().getList();
    }
}
