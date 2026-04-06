package io.nicheblog.dreamdiary.feature.clsf.history.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.feature.clsf.history.HistoryType;
import io.nicheblog.dreamdiary.feature.clsf.history.entity.HistoryEntity;
import io.nicheblog.dreamdiary.feature.clsf.history.mapstruct.HistoryMapstruct;
import io.nicheblog.dreamdiary.feature.clsf.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.clsf.history.repository.jpa.HistoryRepository;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * HistoryService
 * <pre>
 *  작업 이력 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("historyService")
@RequiredArgsConstructor
@Log4j2
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final HistoryMapstruct historyMapstruct;

    /**
     * 게시물 작업 이력 등록.
     *
     * @param refKey 게시물 복합 키
     * @param cn 작업 시점 내용 스냅샷
     */
    @Transactional
    public void addHistory(final BaseClsfKey refKey, final String cn) {
        this.addHistory(refKey, cn, HistoryType.CHANGE, null);
    }

    /**
     * 게시물 작업 이력 등록.
     *
     * @param refKey 게시물 복합 키
     * @param cn 작업 시점 내용 스냅샷
     * @param historyType 이력 타입
     * @param fromHistoryNo 복구 원본 이력 번호
     */
    @Transactional
    public void addHistory(final BaseClsfKey refKey, final String cn, final HistoryType historyType, final Integer fromHistoryNo) {
        if (refKey == null || refKey.getPostNo() == null || refKey.getContentType() == null) return;
        if (AuthUtils.getLgnUserId() == null) return;

        final HistoryEntity history = new HistoryEntity(refKey, cn, historyType, fromHistoryNo);
        historyRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<HistoryDto> getHistoryList(final BaseClsfKey refKey) throws Exception {
        if (refKey == null || refKey.getPostNo() == null || refKey.getContentType() == null) return new ArrayList<>();

        final List<HistoryEntity> historyList = historyRepository.findAllByRefPostNoAndRefContentTypeOrderByRegDtDesc(refKey.getPostNo(), refKey.getContentType());
        final List<HistoryDto> result = new ArrayList<>();
        for (final HistoryEntity history : historyList) {
            final HistoryDto dto = historyMapstruct.toDto(history);
            dto.setPreviewCn(buildPreview(dto.getCn()));
            result.add(dto);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Optional<HistoryDto> getHistory(final BaseClsfKey refKey, final Integer historyNo) throws Exception {
        if (refKey == null || refKey.getPostNo() == null || refKey.getContentType() == null || historyNo == null) return Optional.empty();

        final Optional<HistoryEntity> history = historyRepository.findByHistoryNoAndRefPostNoAndRefContentType(historyNo, refKey.getPostNo(), refKey.getContentType());
        if (history.isEmpty()) return Optional.empty();

        final HistoryDto dto = historyMapstruct.toDto(history.get());
        dto.setPreviewCn(buildPreview(dto.getCn()));
        return Optional.of(dto);
    }

    @Transactional
    public boolean deleteHistory(final BaseClsfKey refKey, final Integer historyNo) {
        if (refKey == null || refKey.getPostNo() == null || refKey.getContentType() == null || historyNo == null) return false;

        final Optional<HistoryEntity> history = historyRepository.findByHistoryNoAndRefPostNoAndRefContentType(historyNo, refKey.getPostNo(), refKey.getContentType());
        if (history.isEmpty()) return false;

        historyRepository.delete(history.get());
        return true;
    }

    @Transactional
    public boolean deleteAllHistory(final BaseClsfKey refKey) {
        if (refKey == null || refKey.getPostNo() == null || refKey.getContentType() == null) return false;

        historyRepository.deleteAllByRefPostNoAndRefContentType(refKey.getPostNo(), refKey.getContentType());
        return true;
    }

    private String buildPreview(final String cn) {
        if (StringUtils.isBlank(cn)) return "";
        final String plainText = CmmUtils.htmlToText(cn).replaceAll("\\s+", " ").trim();
        return StringUtils.abbreviate(plainText, 240);
    }
}
