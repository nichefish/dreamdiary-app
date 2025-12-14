package io.nicheblog.dreamdiary.domain.vcatn.papr.service;

import io.nicheblog.dreamdiary.domain.vcatn.papr.entity.VcatnSchdulEntity;
import io.nicheblog.dreamdiary.domain.vcatn.papr.mapstruct.VcatnSchdulMapstruct;
import io.nicheblog.dreamdiary.domain.vcatn.papr.model.VcatnSchdulDto;
import io.nicheblog.dreamdiary.domain.vcatn.papr.repository.jpa.VcatnSchdulRepository;
import io.nicheblog.dreamdiary.domain.vcatn.papr.spec.VcatnSchdulSpec;
import io.nicheblog.dreamdiary.domain.vcatn.stats.model.VcatnStatsYyDto;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseCrudService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VcatnPaprService
 * <pre>
 *  휴가계획서 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("vcatnSchdulService")
@RequiredArgsConstructor
@Log4j2
public class VcatnSchdulService
        implements BaseCrudService<VcatnSchdulDto, VcatnSchdulDto, Integer, VcatnSchdulEntity> {

    @Getter
    private final VcatnSchdulRepository repository;
    @Getter
    private final VcatnSchdulSpec spec;
    @Getter
    private final VcatnSchdulMapstruct mapstruct = VcatnSchdulMapstruct.INSTANCE;

    public VcatnSchdulMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public VcatnSchdulMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    /**
     * 휴가 년도 정보를 바탕으로 휴가 일정 목록을 조회합니다.
     *
     * @param statsYy 휴가 년도 정보를 담고 있는 객체
     * @return {@link List} 휴가 일정 목록
     */
    public List<VcatnSchdulDto> getListDto(VcatnStatsYyDto statsYy) throws Exception {
        final Map<String, Object> searchParamMap = new HashMap(){{
            put("statsYy", statsYy.getStatsYy());
        }};
        return this.getListDto(searchParamMap);
    }

    /**
     * 단일 항목 조회 (dto level)
     *
     * @param key 조회할 엔티티의 키
     * @return {@link VcatnSchdulDto} -- 조회 항목 반환
     */
    @Transactional(readOnly = true)
    public VcatnSchdulDto getDtlDto(final Integer key) throws Exception {
        final VcatnSchdulEntity retrievedEntity = this.getDtlEntity(key);

        return mapstruct.toDto(retrievedEntity);
    }
}
