package io.nicheblog.dreamdiary.extension.clsf.meta.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.meta.entity.MetaEntity;
import io.nicheblog.dreamdiary.extension.clsf.meta.mapstruct.MetaMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.meta.model.MetaDto;
import io.nicheblog.dreamdiary.extension.clsf.meta.model.MetaSearchParam;
import io.nicheblog.dreamdiary.extension.clsf.meta.repository.jpa.MetaRepository;
import io.nicheblog.dreamdiary.extension.clsf.meta.spec.MetaSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseCrudService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MetaService
 * <pre>
 *  메타 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("metaService")
@RequiredArgsConstructor
@Log4j2
public class MetaService
        implements BaseCrudService<MetaDto, MetaDto, Integer, MetaEntity> {

    @Getter
    private final MetaRepository repository;
    @Getter
    private final MetaSpec spec;
    @Getter
    private final MetaMapstruct mapstruct = MetaMapstruct.INSTANCE;

    public MetaMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public MetaMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    /**
     * 메타 관리 화면에서 요소를 관리할 컨텐츠 타입 목록 조회
     *
     * @return {@link List} -- 컨텐츠 타입 목록
     */
    public List<ContentType> getContentTypeList() {
        return List.of(
                ContentType.JRNL_DAY,
                ContentType.JRNL_DIARY,
                ContentType.JRNL_DREAM
        );
    }

    /**
     * 컨텐츠 타입에 해당하는 메타만 INNER-JOIN으로 조회
     *
     * @param contentType 조회할 컨텐츠 타입
     * @return {@link List} -- 컨텐츠 타입에 해당하는 메타 목록
     */
    @Transactional(readOnly = true)
    public List<MetaDto> getContentSpecificMetaList(final ContentType contentType) {
        return this.getContentSpecificMetaList(contentType.key);
    }

    /**
     * 컨텐츠 타입에 해당하는 메타만 INNER-JOIN으로 조회
     *
     * @param contentType 조회할 컨텐츠 타입
     * @return {@link List} -- 컨텐츠 타입에 해당하는 메타 목록
     */
    @Transactional(readOnly = true)
    public List<MetaDto> getContentSpecificMetaList(final String contentType) {
        final List<MetaEntity> contentSpeficitMetaList = repository.findAll(spec.getContentSpecificMeta(contentType));
        return contentSpeficitMetaList.stream()
                .map(entity -> {
                    try {
                        return mapstruct.toDto(entity);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 컨텐츠 타입에 해당하는 메타만 INNER-JOIN으로 조회 (+사이즈 정보 포함)
     * 메타 1개 = 1. 그 외엔 2~9
     *
     * @param contentType 조회할 컨텐츠 타입
     * @return {@link List} -- 컨텐츠 타입에 해당하는 메타 목록
     */
    @Transactional(readOnly = true)
    public List<MetaDto> getContentSpecificSizedMetaList(final ContentType contentType) {
        final List<MetaDto> metaList = this.getContentSpecificMetaList(contentType);

        final int maxSize = this.calcMaxSize(metaList, contentType);
        final int MIN_SIZE = 2; // 최소 크기
        final int MAX_SIZE = 9; // 최대 크기
        return metaList.stream()
                .peek(dto -> {
                    final int size = dto.getContentSize();
                    if (size == 1) {
                        dto.setMetaClass("ts-1");
                    } else {
                        final double ratio = (double) size / maxSize; // 사용 빈도의 비율 계산
                        final int metaSize = (int) (MIN_SIZE + (MAX_SIZE - MIN_SIZE) * ratio);
                        dto.setMetaClass("ts-"+metaSize);
                    }
                })
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 컨텐츠 타입과 무관하게 메타 조회 (+사이즈 정보 포함)
     * 메타 1개 = 1. 그 외엔 2~9
     *
     * @param searchParam 검색 파라미터
     * @return {@link List} -- 컨텐츠 타입에 해당하는 메타 목록
     */
    @Transactional(readOnly = true)
    public List<MetaDto> getOverallSizedMetaList(final MetaSearchParam searchParam) throws Exception {
        final List<MetaEntity> metaEntityList = this.getListEntity(searchParam);
        final List<MetaDto> metaList = mapstruct.toDtoList(metaEntityList);
        final String refContentType = searchParam.getContentType();

        final int maxSize = this.calcMaxSize(metaList, refContentType);
        final int MIN_SIZE = 2; // 최소 크기
        final int MAX_SIZE = 9; // 최대 크기
        return metaList.stream()
                .peek(dto -> {
                    final int size = dto.getContentSize();
                    if (size == 1) {
                        dto.setMetaClass("ts-1");
                    } else {
                        final double ratio = (double) size / maxSize; // 사용 빈도의 비율 계산
                        final int metaSize = (int) (MIN_SIZE + (MAX_SIZE - MIN_SIZE) * ratio);
                        dto.setMetaClass("ts-"+metaSize);
                    }
                })
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 최대 사용빈도 계산한 메타 목록 조회
     *
     * @param metaList 메타 목록 (List<MetaDto>)
     * @param contentType 조회할 컨텐츠 타입 (ContentType)
     * @return {@link Integer} -- 메타 목록에서 계산된 최대 사용 빈도 (Integer)
     */
    @Transactional(readOnly = true)
    public Integer calcMaxSize(final List<MetaDto> metaList, final ContentType contentType) {
        return this.calcMaxSize(metaList, contentType.key);
    }

    /**
     * 최대 사용빈도 계산한 메타 목록 조회
     *
     * @param metaList 메타 목록 (List<MetaDto>)
     * @param contentType 조회할 컨텐츠 타입 (ContentType)
     * @return {@link Integer} -- 메타 목록에서 계산된 최대 사용 빈도 (Integer)
     */
    @Transactional(readOnly = true)
    public Integer calcMaxSize(final List<MetaDto> metaList, final String contentType) {
        int maxFrequency = 0;
        for (final MetaDto meta : metaList) {
            // 캐싱 처리 위해 셀프 프록시
            final Integer metaSize = this.countMetaSize(meta.getMetaNo(), contentType, AuthUtils.getLgnUserId());
            meta.setContentSize(metaSize);
            maxFrequency = Math.max(maxFrequency, metaSize);
        }
        return maxFrequency;
    }

    /**
     * 최대 사용빈도 계산한 메타 목록 조회
     *
     * @param metaNo 메타 번호
     * @param contentType 조회할 컨텐츠 타입 (ContentType)
     * @return {@link Integer} -- 메타 목록에서 계산된 최대 사용 빈도 (Integer)
     */
    @Transactional(readOnly = true)
    public Integer countMetaSize(final Integer metaNo, final String contentType, final String regstrId) {
        return repository.countMetaSize(metaNo, contentType, regstrId);
    }

    /**
     * 마스터 메타 추가:: 메소드 분리
     *
     * @param metaList 처리할 메타 Dto 목록
     * @return {@link List<MetaEntity>} -- 저장된 메타 엔티티 목록
     */
    @Transactional
    public List<MetaEntity> addMasterMeta(final List<MetaDto> metaList) {

        final List<MetaEntity> metaEntityList = metaList.stream()
                .distinct() // 중복된 메타 문자열 제거
                .map(meta -> {
                    Optional<MetaEntity> existingMeta = repository.findByMetaNmAndCtgrAndLabel(meta.getMetaNm(), meta.getCtgr(), meta.getLabel());
                    if (existingMeta.isPresent()) {
                        MetaEntity metaEntity = existingMeta.get();
                        metaEntity.setDelYn("N");
                        metaEntity.setValue(meta.getValue());       // content에 전달하기 위해 value 값 전달
                        metaEntity.setUnit(meta.getUnit());       // content에 전달하기 위해 value 값 전달
                        return metaEntity;
                    }
                    // 기존 데이터가 없으면 새 객체 생성 (content에 전달하기 위해 value 값 전달)
                    return MetaEntity.builder().metaNm(meta.getMetaNm()).ctgr(meta.getCtgr()).label(meta.getLabel()).value(meta.getValue()).build();
                })
                .collect(Collectors.toList());

        return repository.saveAllAndFlush(metaEntityList);
    }

    /**
     * 메타-컨텐츠와 연관관계 없는 마스터 메타 삭제
     */
    @Transactional
    public void deleteNoRefMetas() {
        final List<MetaEntity> entity = repository.findAll(spec.getNoRefMetas());
        repository.deleteAll(entity);
    }
}