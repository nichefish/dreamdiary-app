package io.nicheblog.dreamdiary.extension.state.model.cmpstn;

/**
 * StateCmpstnModule
 * <pre>
 *   State 모듈 인터페이스 (dto level)
 * </pre>
 *
 * @author nichefish
 */
public interface StateCmpstnModule {
    /** Getter */
    StateCmpstn getState();

    /** Setter */
    void setState(StateCmpstn embed);
}

