package io.nicheblog.dreamdiary.domain.jrnl.day.service;

import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayMetaEntity;
import io.nicheblog.dreamdiary.domain.jrnl.day.mapstruct.JrnlDayMetaMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.day.repository.jpa.JrnlDayMetaRepository;
import io.nicheblog.dreamdiary.domain.jrnl.day.spec.JrnlDayMetaSpec;
import io.nicheblog.dreamdiary.extension.clsf.meta.model.MetaDto;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseReadonlyService;
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
        implements BaseReadonlyService<MetaDto, Integer, JrnlDayMetaEntity> {

    @Getter
    private final JrnlDayMetaRepository repository;
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
     * 메타 카테고리 맵을 반환합니다.
     *
     * @param userId 사용자 아이디
     * @return {@link Map} -- 메타 이름을 키로 하고, 카테고리 목록을 값으로 가지는 맵
     */
    @Cacheable(value="myJrnlDayMetaCtgrMap", key="#userId")
    public Map<String, List<String>> getMetaCtgrMap(final String userId) throws Exception {
        final HashMap<String, Object> paramMap = new HashMap<>() {{
            put("regstrId", userId);
        }};

        final List<JrnlDayMetaEntity> tagList = this.getSelf().getListEntity(paramMap);
        return tagList.stream()
                .collect(Collectors.groupingBy(
                        JrnlDayMetaEntity::getMetaNm,
                        Collectors.mapping(tag -> {
                            if (StringUtils.isBlank(tag.getCtgr())) return "";
                            return tag.getCtgr();
                        }, Collectors.toList())
                ));
    }
}
