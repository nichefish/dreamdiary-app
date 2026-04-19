package io.nicheblog.dreamdiary.feature.journal.day.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable.meta.model.MetaDto;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayMetaEntity;
import io.nicheblog.dreamdiary.feature.journal.day.mapstruct.JournalDayMetaMapstruct;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayMetaRepository;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.day.spec.JournalDayMetaSpec;
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
 * JournalDayMetaService
 * <pre>
 *  저널 일자 메타 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalDayMetaService
        implements BaseDtoReadableService<MetaDto, Integer, JournalDayMetaEntity> {

    @Getter
    private final JournalDayMetaRepository repository;
    private final JournalDayRepository journalDayRepository;
    @Getter
    private final JournalDayMetaSpec spec;
    @Getter
    private final JournalDayMetaMapstruct mapstruct = JournalDayMetaMapstruct.INSTANCE;

    public JournalDayMetaMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JournalDayMetaMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final ApplicationContext context;
    private JournalDayMetaService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 사용자 기준 특정 메타가 존재하는 연도 목록을 반환합니다.
     *
     * @param metaId 메타 ID
     * @param username 사용자 계정명
     * @return 연도 목록
     */
    @Cacheable(value="journalDayMetaYyListByUser", key="new org.springframework.cache.interceptor.SimpleKey(#metaId, #username)")
    public List<Integer> getYyListByMetaIdAndUser(final Integer metaId, final String username) {
        return journalDayRepository.findDistinctYysByMetaIdAndCreatedBy(metaId, AuthUtils.requireUsername(username));
    }

    /**
     * 사용자별 메타 카테고리 맵을 반환합니다.
     *
     * @param username 사용자 계정명
     * @return {@link Map} -- 메타 이름을 키로 하고, 카테고리 목록을 값으로 가지는 맵
     */
    @Cacheable(value="journalDayMetaCtgrMapByUser", key="#username")
    public Map<String, List<String>> getMetaCtgrMapByUser(final String username) throws Exception {
        final HashMap<String, Object> paramMap = new HashMap<>() {{
            put("createdBy", AuthUtils.requireUsername(username));
        }};

        final List<JournalDayMetaEntity> metaList = this.getSelf().getListEntity(paramMap);
        return metaList.stream()
                .collect(Collectors.groupingBy(
                        JournalDayMetaEntity::getMetaNm,
                        Collectors.mapping(tag -> {
                            if (StringUtils.isBlank(tag.getCtgr())) return "";
                            return tag.getCtgr();
                        }, Collectors.toList())
                ));
    }
}


