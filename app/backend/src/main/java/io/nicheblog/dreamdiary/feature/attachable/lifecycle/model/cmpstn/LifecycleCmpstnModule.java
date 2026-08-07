package io.nicheblog.dreamdiary.feature.attachable.lifecycle.model.cmpstn;

/**
 * 라이프사이클 조합 객체를 노출하는 DTO용 인터페이스.
 *
 * <p>journal entry(리플렉션 포함)가 이 인터페이스를 구현하면, view helper가 구체 DTO 타입을
 * 몰라도 라이프사이클을 주입할 수 있다.</p>
 */
public interface LifecycleCmpstnModule {

    /**
     * 해당 DTO에 붙은 라이프사이클 조합 객체를 반환한다.
     *
     * @return 라이프사이클 조합 객체
     */
    LifecycleCmpstn getLifecycle();

    /**
     * 해당 DTO에 라이프사이클 조합 객체를 설정한다.
     *
     * @param cmpstn 라이프사이클 조합 객체
     */
    void setLifecycle(LifecycleCmpstn cmpstn);
}
