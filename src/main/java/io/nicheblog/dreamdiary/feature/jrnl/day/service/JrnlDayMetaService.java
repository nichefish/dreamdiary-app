package io.nicheblog.dreamdiary.feature.jrnl.day.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf.meta.model.MetaDto;
import io.nicheblog.dreamdiary.feature.jrnl.day.entity.JrnlDayMetaEntity;
import io.nicheblog.dreamdiary.feature.jrnl.day.mapstruct.JrnlDayMetaMapstruct;
import io.nicheblog.dreamdiary.feature.jrnl.day.repository.jpa.JrnlDayMetaRepository;
import io.nicheblog.dreamdiary.feature.jrnl.day.repository.jpa.JrnlDayRepository;
import io.nicheblog.dreamdiary.feature.jrnl.day.spec.JrnlDayMetaSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoReadableService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JrnlDayMetaService
 * <pre>
 *  저널 일자 메타 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service("jrnlDayMetaService")
@RequiredArgsConstructor
@Log4j2
public class JrnlDayMetaService
        implements BaseDtoReadableService<MetaDto, Integer, JrnlDayMetaEntity> {

    @Getter
    private final JrnlDayMetaRepository repository;
    private final JrnlDayRepository jrnlDayRepository;
    @Getter
    private final JrnlDayMetaSpec spec;
    @Getter
    private final JrnlDayMetaMapstruct mapstruct = JrnlDayMetaMapstruct.INSTANCE;

    public JrnlDayMetaMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JrnlDayMetaMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final ApplicationContext context;
    private JrnlDayMetaService getSelf() {
        return context.getBean(this.getClass());
    }


    /**
     * 내 태그 카테고리 맵을 반환합니다.
     *
     * @return {@link Map} -- 태그 이름을 키로 하고, 카테고리 목록을 값으로 가지는 맵
     */
    public Map<String, List<String>> getMyMetaCtgrMap() throws Exception {
        final String userId = AuthUtils.getLgnUserId();
        return this.getSelf().getMetaCtgrMapByUser(userId);
    }

    /**
     * 특정 메타가 존재하는 연도 목록을 반환합니다.
     *
     * @param metaNo 메타 번호
     * @return 연도 목록
     */
    public List<Integer> getMyYyListByMetaNo(final Integer metaNo) {
        final String userId = AuthUtils.requireUserId(AuthUtils.getLgnUserId());
        return this.getSelf().getYyListByMetaNoAndUser(metaNo, userId);
    }

    /**
     * 사용자 기준 특정 메타가 존재하는 연도 목록을 반환합니다.
     *
     * @param metaNo 메타 번호
     * @param userId 사용자 ID
     * @return 연도 목록
     */
    @Cacheable(value="jrnlDayMetaYyListByUser", key="new org.springframework.cache.interceptor.SimpleKey(#metaNo, #userId)")
    public List<Integer> getYyListByMetaNoAndUser(final Integer metaNo, final String userId) {
        return jrnlDayRepository.findDistinctYysByMetaNoAndRegstrId(metaNo, AuthUtils.requireUserId(userId));
    }

    /**
     * 사용자별 메타 카테고리 맵을 반환합니다.
     *
     * @param userId 사용자 아이디
     * @return {@link Map} -- 메타 이름을 키로 하고, 카테고리 목록을 값으로 가지는 맵
     */
    @Cacheable(value="jrnlDayMetaCtgrMapByUser", key="#userId")
    public Map<String, List<String>> getMetaCtgrMapByUser(final String userId) throws Exception {
        final HashMap<String, Object> paramMap = new HashMap<>() {{
            put("regstrId", AuthUtils.requireUserId(userId));
        }};

        final List<JrnlDayMetaEntity> metaList = this.getSelf().getListEntity(paramMap);
        return metaList.stream()
                .collect(Collectors.groupingBy(
                        JrnlDayMetaEntity::getMetaNm,
                        Collectors.mapping(tag -> {
                            if (StringUtils.isBlank(tag.getCtgr())) return "";
                            return tag.getCtgr();
                        }, Collectors.toList())
                ));
    }
}
