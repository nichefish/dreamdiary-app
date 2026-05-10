package io.nicheblog.dreamdiary.feature.chat.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.chat.entity.ChatSessionEntity;
import io.nicheblog.dreamdiary.feature.chat.model.ChatSessionDto;
import io.nicheblog.dreamdiary.feature.chat.repository.jpa.ChatMessageRepository;
import io.nicheblog.dreamdiary.feature.chat.repository.jpa.ChatSessionRepository;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ChatSessionService
 * <pre>
 *  AI 채팅 세션 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class ChatSessionService {

    private static final String DEFAULT_TITLE = "새 대화";
    private static final String DEFAULT_MODEL = "qwen2.5:7b";
    private static final String DEFAULT_SYSTEM_PROMPT = "너는 Dreamdiary assistant다. 사용자의 기록과 생각 정리를 돕는다.";

    private final ChatSessionRepository repository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional(readOnly = true)
    public List<ChatSessionDto> getMySessions() {
        return repository.findAllByCreatedByOrderByLastMessageAtDescCreatedAtDesc(AuthUtils.getLoginUsername()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatSessionDto create(final ChatSessionDto dto) {
        final Date now = new Date();
        final ChatSessionEntity entity = ChatSessionEntity.builder()
                .title(StringUtils.defaultIfBlank(dto == null ? null : dto.getTitle(), DEFAULT_TITLE))
                .status("ACTIVE")
                .model(StringUtils.defaultIfBlank(dto == null ? null : dto.getModel(), DEFAULT_MODEL))
                .systemPrompt(StringUtils.defaultIfBlank(dto == null ? null : dto.getSystemPrompt(), DEFAULT_SYSTEM_PROMPT))
                .lastMessageAt(now)
                .build();

        return toDto(repository.saveAndFlush(entity));
    }

    @Transactional(readOnly = true)
    public ChatSessionEntity getMySessionEntity(final Integer sessionId) {
        final ChatSessionEntity entity = repository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("chat session not found"));

        if (!AuthUtils.isCreatedBy(entity.getCreatedBy())) {
            throw new EntityNotFoundException("chat session not found");
        }

        return entity;
    }

    @Transactional
    public ChatSessionDto touchAfterMessage(final Integer sessionId, final String titleCandidate) {
        final ChatSessionEntity entity = this.getMySessionEntity(sessionId);
        entity.setLastMessageAt(new Date());

        if (StringUtils.equals(entity.getTitle(), DEFAULT_TITLE) && StringUtils.isNotBlank(titleCandidate)) {
            entity.setTitle(toSessionTitle(titleCandidate));
        }

        return toDto(repository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(final Integer sessionId) {
        final ChatSessionEntity entity = this.getMySessionEntity(sessionId);
        chatMessageRepository.findAllBySessionIdOrderBySeqAscCreatedAtAsc(sessionId)
                .forEach(chatMessageRepository::delete);
        repository.delete(entity);
    }

    public String getDefaultSystemPrompt() {
        return DEFAULT_SYSTEM_PROMPT;
    }

    private String toSessionTitle(final String message) {
        final String compact = StringUtils.normalizeSpace(message);
        if (compact.length() <= 28) return compact;
        return compact.substring(0, 28) + "...";
    }

    private ChatSessionDto toDto(final ChatSessionEntity entity) {
        final ChatSessionDto dto = ChatSessionDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .status(entity.getStatus())
                .model(entity.getModel())
                .systemPrompt(entity.getSystemPrompt())
                .createdBy(entity.getCreatedBy())
                .createdByNm(entity.getCreatedByInfo() == null ? null : entity.getCreatedByInfo().getNickname())
                .createdAt(formatDate(entity.getCreatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(formatDate(entity.getUpdatedAt()))
                .lastMessageAt(formatDate(entity.getLastMessageAt()))
                .build();

        dto.setIsCreatedBy(entity.isCreatedBy());
        dto.setIsUpdatedBy(entity.isUpdatedBy());
        return dto;
    }

    private String formatDate(final Date date) {
        try {
            return DateUtils.asStr(date, DatePtn.DATETIME);
        } catch (final Exception e) {
            return null;
        }
    }
}
