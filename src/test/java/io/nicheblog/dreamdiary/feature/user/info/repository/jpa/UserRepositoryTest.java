package io.nicheblog.dreamdiary.feature.user.info.repository.jpa;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.feature.user.emplym.entity.UserEmplymEntity;
import io.nicheblog.dreamdiary.feature.user.emplym.entity.UserEmplymEntityTestFactory;
import io.nicheblog.dreamdiary.feature.user.info.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.info.entity.UserEntityTestFactory;
import io.nicheblog.dreamdiary.feature.user.profile.entity.UserProfileEntity;
import io.nicheblog.dreamdiary.feature.user.profile.entity.UserProfileEntityTestFactory;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.config.DataSourceConfig;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserRepositoryTest
 * <pre>
 *  UserRepository 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(DataSourceConfig.class)
@Import(TestAuditConfig.class)
@Log4j2
class UserRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private UserRepository userRepository;

    private UserEntity userEntity;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 UserEntity 초기화
        userEntity = UserEntityTestFactory.create();
    }

    /**
     * regist 테스트
     */
    @Test
    public void testRegist() throws Exception {
        // Given::
        
        // When::
        final UserEntity registered = userRepository.save(userEntity);
        final Integer key = registered.getId();
        final UserEntity retrieved = userRepository.findById(key).orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.registered")));

        // Then::
        assertNotNull(retrieved, "저장한 데이터를 조회할 수 없습니다.");
        assertNotNull(retrieved.getId(), "저장된 엔티티의 key 값이 없습니다.");
        // audit
        assertNotNull(retrieved.getCreatedAt(), "등록일자 audit 처리가 되지 않았습니다.");
        assertNotNull(retrieved.getCreatedBy(),  "등록자 audit 처리가 되지 않았습니다.");
        assertEquals(TestConstant.TEST_AUDITOR, retrieved.getCreatedBy(), "등록자가 예상 값과 일치하지 않습니다.");
        // value
        assertEquals(registered.getUsername(), retrieved.getUsername(), "값이 제대로 등록되지 않았습니다.");
    }

    /**
     * regist 테스트 - 사용자 프로필 정보가 있을 경우
     */
    @Test
    public void testRegist_proflCascade() throws Exception {
        // Given::
        UserProfileEntity userProfile = UserProfileEntityTestFactory.create();
        userEntity.setProfile(userProfile);
        userEntity.cascade();

        // When::
        final UserEntity registered = userRepository.save(userEntity);
        final Integer key = registered.getId();
        final UserEntity retrieved = userRepository.findById(key).orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.registered")));

        // Then::
        assertNotNull(retrieved, "저장한 데이터를 조회할 수 없습니다.");
        UserProfileEntity retrievedProfile = retrieved.getProfile();
        assertNotNull(retrievedProfile, "사용자 프로필 정보가 제대로 저장되지 않았습니다.");
        assertNotNull(retrievedProfile.getUserProfileId(), "사용자 프로필 정보 ID가 제대로 생성되지 않았습니다.");
        // cascade 체크
        assertNotNull(retrievedProfile.getUser(), "프로필에 연결된 사용자 정보가 없습니다.");
        assertEquals(key, retrievedProfile.getUser().getId(), "프로필의 사용자 ID가 일치하지 않습니다.");
        // 항목 체크
        assertEquals(userProfile.getBrthdy(), retrievedProfile.getBrthdy(), "프로필의 생년월일 정보가 일치하지 않습니다.");
    }

    /**
     * regist 테스트 - 사용자 직원정보가 있을 경우
     */
    @Test
    public void testRegist_emplymCascade() throws Exception {
        // Given::
        UserEmplymEntity userEmplym = UserEmplymEntityTestFactory.create();
        userEntity.setEmplym(userEmplym);
        userEntity.cascade();

        // When::
        final UserEntity registered = userRepository.save(userEntity);
        final Integer key = registered.getId();
        final UserEntity retrieved = userRepository.findById(key).orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.registered")));

        // Then::
        assertNotNull(retrieved, "저장한 데이터를 조회할 수 없습니다.");
        UserEmplymEntity retrievedEmplym = retrieved.getEmplym();
        assertNotNull(retrievedEmplym, "사용자 인사정보가 제대로 저장되지 않았습니다.");
        assertNotNull(retrievedEmplym.getId(), "사용자 인사정보 ID가 제대로 생성되지 않았습니다.");
        // cascade 체크
        assertNotNull(retrievedEmplym.getUser(), "사용자 인사정보가 제대로 저장되지 않았습니다.");
        assertEquals(key, retrievedEmplym.getUser().getId(), "사용자 인사정보가 사용자 정보에 제대로 매핑되지 않았습니다.");
        // 항목 체크
        // 날짜 변환 체크
        assertEquals(userEmplym.getEcnyDt(), retrievedEmplym.getEcnyDt(), "프로필의 생년월일 정보가 일치하지 않습니다.");
        assertEquals(userEmplym.getRetireDt(), retrievedEmplym.getRetireDt(), "프로필의 생년월일 정보가 일치하지 않습니다.");
        // 이메일 변환 로직
        assertEquals(userEmplym.getEmplymEmail(), retrievedEmplym.getEmplymEmail(), "프로필의 이메일 정보가 일치하지 않습니다.");
    }

    /**
     * modify 테스트
     */
    @Test
    public void testModify() throws Exception {
        // Given::
        UserEntity registered = userRepository.save(userEntity);
        Integer key = registered.getId();

        // When::
        UserEntity toModify = userRepository.findById(key).orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-modify")));
        toModify.setCn("modified");
        UserEntity modified = userRepository.save(toModify);

        // Then::
        assertNotNull(modified, "저장한 데이터를 조회할 수 없습니다.");
        assertNotNull(modified.getId(), "저장된 엔티티의 key 값이 없습니다.");
        // audit
        assertNotNull(modified.getUpdatedAt(), "수정일자 audit 처리가 되지 않았습니다.");
        assertNotNull(modified.getUpdatedBy(),  "수정자 audit 처리가 되지 않았습니다.");
        assertEquals(TestConstant.TEST_AUDITOR, modified.getUpdatedBy(), "수정자가 예상 값과 일치하지 않습니다.");
        // value
        assertEquals("modified", modified.getCn(), "값이 정상적으로 수정되지 않았습니다.");
    }

    /**
     * delete 테스트
     */
    @Test
    public void testDelete() throws Exception {
        // Given::
        final UserEntity registered = userRepository.save(userEntity);
        final Integer key = registered.getId();

        // When::
        final UserEntity toDelete = userRepository.findById(key).orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-delete")));
        userRepository.delete(toDelete);

        final UserEntity retrieved = userRepository.findById(key).orElse(null);

        // Then::
        assertNull(retrieved, "삭제가 제대로 이루어지지 않았습니다.");
    }
    
}
