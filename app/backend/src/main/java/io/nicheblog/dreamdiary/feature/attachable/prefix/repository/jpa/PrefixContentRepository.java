package io.nicheblog.dreamdiary.feature.attachable.prefix.repository.jpa;

import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixContentEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 말머리-컨텐츠 연결 저장소.
 * <p>
 * 콘텐츠당 0..1 연결이므로 {@code (ref_id, ref_content_type)}로 단건 조회한다.
 * (cascade하지 않고 수동 관리 — meta_content와 동일.)
 * </p>
 *
 * @author nichefish
 */
@Repository
public interface PrefixContentRepository
        extends BaseStreamRepository<PrefixContentEntity, Integer> {

    /**
     * attachable 키로 콘텐츠의 말머리 연결을 단건 조회한다.
     *
     * @param refId 참조 글 번호
     * @param refContentType 참조 컨텐츠 타입
     * @return 말머리 연결(0..1)
     */
    Optional<PrefixContentEntity> findByRefIdAndRefContentType(Integer refId, String refContentType);
}
