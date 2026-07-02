package io.nicheblog.dreamdiary.infrastructure.web.config;

import io.nicheblog.dreamdiary.global.ActiveProfile;
import io.nicheblog.dreamdiary.global.ReleaseInfo;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.handler.CustomEventBus;
import org.mockito.Mockito;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.Executor;

/**
 * WebMvc 슬라이스 테스트용 빈 보강
 * <pre>
 *  {@literal @}WebMvcTest 시 {@link WebMvcContextConfig}가 {@link io.nicheblog.dreamdiary.infrastructure.log.interceptor.LogInterceptor}
 *  등 조립 빈을 끌어오는데, 기본 패키지 스캔 밖(global 등)의 {@literal @}Component 들은 컨텍스트에 등록되지 않을 수 있다.
 *  (FreeMarker MVC 렌더 경로 제거 이후에도 웹 레이어 인터셉터 조립에는 동일하게 필요하다.)
 * </pre>
 *
 * 변경 전: {@code Failed to load ApplicationContext} ({@link org.springframework.beans.factory.NoSuchBeanDefinitionException}: {@link ActiveProfile}, {@link ReleaseInfo},
 * 또는 {@link io.nicheblog.dreamdiary.infrastructure.log.interceptor.LogInterceptor}→{@link ApplicationEventPublisherWrapper} 미등록).
 * 변경 후: {@code spring.profiles.active=test}로 {@link ActiveProfile} 바인딩하고, 웹 레이어 인터셉터 슬라이스에 빠지기 쉬운 빈을 보충한다.
 * — {@link ReleaseInfo},{@link ApplicationEventPublisherWrapper} 및 해당 래퍼의 {@literal @}Resource 이름 빈 {@code taskExecutor},{@code customEventBus}
 * ({@link CustomEventBus}: Mockito 스텁, 워커 비가동).
 *
 * @author nichefish
 */
@TestConfiguration
@TestPropertySource(properties = "spring.profiles.active=test")
@EnableConfigurationProperties(ActiveProfile.class)
public class WebMvcTestSliceSupportConfig {

    /**
     * 배포 메타({@link ReleaseInfo})의 테스트용 인스턴스.
     * (변경 전: 화면 인터셉터의 {@code releaseDate} 모델 주입용. 변경 후: FreeMarker 인터셉터 제거 — {@code CmmController} 등 웹 레이어 소비처용으로 유지.)
     *
     * @return 비어 있지 않은 기본값을 가진 {@link ReleaseInfo}
     */
    @Bean
    public ReleaseInfo releaseInfo() {
        final ReleaseInfo info = new ReleaseInfo();
        info.setEnabled(Boolean.FALSE);
        info.setVersion("0.0.0-test");
        info.setReleaseDate(null);
        return info;
    }

    /**
     * {@link ApplicationEventPublisherWrapper#publishAsyncEvent} 가 테스트에서 동기로 끝나도록 하는 실행기.
     *
     * @return 현재 스레드에서 {@link Runnable} 을 즉시 수행하는 {@link Executor}
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        return Runnable::run;
    }

    /**
     * 원본 {@link CustomEventBus} 는 {@literal @}PostConstruct 로 워커를 시작하므로, 슬라이스에서는 목으로 대체한다.
     *
     * @return 메서드 호출 무해화용 Mockito 스텁
     */
    @Bean(name = "customEventBus")
    public CustomEventBus customEventBusStub() {
        return Mockito.mock(CustomEventBus.class, Mockito.withSettings().stubOnly());
    }

    /**
     * {@link io.nicheblog.dreamdiary.infrastructure.log.interceptor.LogInterceptor} 의 생성자 의존성.
     * {@literal @}Resource 필드는 컨텍스트의 {@link org.springframework.context.ApplicationEventPublisher} 등록 정보로 자동 주입된다.
     *
     * @return 테스트 슬라이스용 래퍼
     */
    @Bean
    public ApplicationEventPublisherWrapper applicationEventPublisherWrapper() {
        return new ApplicationEventPublisherWrapper();
    }
}
