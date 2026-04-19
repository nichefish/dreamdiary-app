package io.nicheblog.dreamdiary.feature.attachable.history.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable.history.HistoryType;
import io.nicheblog.dreamdiary.feature.attachable.history.entity.HistoryEntity;
import io.nicheblog.dreamdiary.feature.attachable.history.mapstruct.HistoryMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.attachable.history.repository.jpa.HistoryRepository;
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
@Service
@RequiredArgsConstructor
@Log4j2
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final HistoryMapstruct historyMapstruct;

    /**
     * 게시물 작업 이력 등록.
     *
     * @param refKey 게시물 복합 키
     * @param content 작업 시점 내용 스냅샷
     */
    @Transactional
    public void addHistory(final BaseAttachableKey refKey, final String content) {
        this.addHistory(refKey, content, HistoryType.CHANGE, null);
    }

    /**
     * 게시물 작업 이력 등록.
     *
     * @param refKey 게시물 복합 키
     * @param content 작업 시점 내용 스냅샷
     * @param historyType 이력 타입
     * @param fromHistoryId 복구 원본 이력 번호
     */
    @Transactional
    public void addHistory(final BaseAttachableKey refKey, final String content, final HistoryType historyType, final Integer fromHistoryId) {
        if (refKey == null || refKey.getId() == null || refKey.getContentType() == null) return;
        if (AuthUtils.getLoginUsername() == null) return;

        final HistoryEntity history = new HistoryEntity(refKey, content, historyType, fromHistoryId);
        historyRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<HistoryDto> getHistoryList(final BaseAttachableKey refKey) throws Exception {
        if (refKey == null || refKey.getId() == null || refKey.getContentType() == null) return new ArrayList<>();

        final List<HistoryEntity> historyList = historyRepository.findAllByRefIdAndRefContentTypeOrderByCreatedAtDesc(refKey.getId(), refKey.getContentType());
        final List<HistoryDto> result = new ArrayList<>();
        for (final HistoryEntity history : historyList) {
            final HistoryDto dto = historyMapstruct.toDto(history);
            dto.setPreviewContent(buildPreview(dto.getContent()));
            result.add(dto);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Optional<HistoryDto> getHistory(final BaseAttachableKey refKey, final Integer historyId) throws Exception {
        if (refKey == null || refKey.getId() == null || refKey.getContentType() == null || historyId == null) return Optional.empty();

        final Optional<HistoryEntity> history = historyRepository.findByIdAndRefIdAndRefContentType(historyId, refKey.getId(), refKey.getContentType());
        if (history.isEmpty()) return Optional.empty();

        final HistoryDto dto = historyMapstruct.toDto(history.get());
        dto.setPreviewContent(buildPreview(dto.getContent()));
        return Optional.of(dto);
    }

    @Transactional
    public boolean deleteHistory(final BaseAttachableKey refKey, final Integer historyId) {
        if (refKey == null || refKey.getId() == null || refKey.getContentType() == null || historyId == null) return false;

        final Optional<HistoryEntity> history = historyRepository.findByIdAndRefIdAndRefContentType(historyId, refKey.getId(), refKey.getContentType());
        if (history.isEmpty()) return false;

        historyRepository.delete(history.get());
        return true;
    }

    @Transactional
    public boolean deleteAllHistory(final BaseAttachableKey refKey) {
        if (refKey == null || refKey.getId() == null || refKey.getContentType() == null) return false;

        historyRepository.deleteAllByRefIdAndRefContentType(refKey.getId(), refKey.getContentType());
        return true;
    }

    private String buildPreview(final String content) {
        if (StringUtils.isBlank(content)) return "";
        final String plainText = CmmUtils.htmlToText(content).replaceAll("\\s+", " ").trim();
        return StringUtils.abbreviate(plainText, 240);
    }
}

