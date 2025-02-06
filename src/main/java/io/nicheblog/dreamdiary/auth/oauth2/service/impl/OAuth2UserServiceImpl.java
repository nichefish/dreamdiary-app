package io.nicheblog.dreamdiary.auth.oauth2.service.impl;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2UserService
 * <pre>
 *  네이버용 OAuth2 인증 처리 서비스
 *  (네이버에서는 response 안에 attribute들이 들어깄기 떄문에 별도의 처리가 필요하다.)
 * </pre>
 * 
 * @author nichefish
 */
@Service("oauth2UserService")
public class OAuth2UserServiceImpl
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    /**
     * 주어진 OAuth2UserRequest 정보를 기반으로 사용자 정보를 조회한다.
     *
     * @param userRequest OAuth 2.0 사용자 요청 정보
     * @return {@link OAuth2User} 객체 (네이버의 경우 평탄화된 속성, 그 외는 기본 속성)
     * @throws OAuth2AuthenticationException 네이버 응답이 예상과 다르거나 사용자 정보를 조회할 수 없는 경우

     */
    @Override
    public OAuth2User loadUser(final OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1) 우선 Spring이 제공하는 기본 서비스로부터 userInfo를 가져온다.
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        final String registrationId = userRequest.getClientRegistration().getRegistrationId();
        // "naver"가 아니면, 디폴트 로직 결과를 그대로 반환
        if (!"naver".equals(registrationId)) return oauth2User;

        // === 여기부터 네이버(Naver)일 때 ===
        // 네이버는 구조가 { resultcode=..., message=..., response={ id=..., email=..., name=... } }
        final Map<String, Object> attributes = oauth2User.getAttributes();
        final Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            // 응답 형식이 예상과 다르면 예외 처리
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_response"), "Naver response is null");
        }

        // "response" 안에 있는 실제 필드들을 꺼내서, 최상위로 평탄화
        final Map<String, Object> flattenedAttributes = new HashMap<>(response);

        // 여기서 "id"를 userNameAttribute로 삼는 예시
        // (원하시면 "email" 등 다른 필드를 쓸 수도 있습니다.)
        final String userNameAttributeName = "email";

        // 권한은 기존 delegate에서 가져오거나, 직접 ROLE_USER 같은 걸 넣을 수 있음
        Collection<? extends GrantedAuthority> authorities = oauth2User.getAuthorities();
        if (authorities.isEmpty()) {
            authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // 3) DefaultOAuth2User를 만들어 반환
        return new DefaultOAuth2User(authorities, flattenedAttributes, userNameAttributeName);
    }
}
