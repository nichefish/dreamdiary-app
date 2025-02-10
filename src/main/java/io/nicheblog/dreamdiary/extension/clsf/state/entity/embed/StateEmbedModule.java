package io.nicheblog.dreamdiary.extension.clsf.state.entity.embed;

/**
 * StateEmbedModule
 * <pre>
 *   State 모듈 인터페이스 (entity level)
 * </pre>
 *
 * @author nichefish
 */
public interface StateEmbedModule {
    /** Getter */
    StateEmbed getState();

    /** Setter */
    void setState(StateEmbed embed);
}
