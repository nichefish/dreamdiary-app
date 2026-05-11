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

    /**
     * 로그인 사용자의 채팅 세션 목록을 최근 대화 시각 기준으로 조회한다.
     *
     * @return 로그인 사용자의 채팅 세션 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<ChatSessionDto> getMySessions() {
        return repository.findAllByCreatedByOrderByLastMessageAtDescCreatedAtDesc(AuthUtils.getLoginUsername()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 새 채팅 세션을 생성한다.
     *
     * @param dto 생성 시 전달된 세션 제목, 모델, 시스템 프롬프트
     * @return 생성된 채팅 세션 DTO
     */
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

    /**
     * 로그인 사용자가 소유한 채팅 세션 엔티티를 조회한다.
     *
     * @param sessionId 조회할 채팅 세션 ID
     * @return 로그인 사용자가 접근할 수 있는 채팅 세션 엔티티
     * @throws EntityNotFoundException 세션이 없거나 로그인 사용자의 세션이 아닌 경우
     */
    @Transactional(readOnly = true)
    public ChatSessionEntity getMySessionEntity(final Integer sessionId) {
        final ChatSessionEntity entity = repository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("chat session not found"));

        if (!AuthUtils.isCreatedBy(entity.getCreatedBy())) {
            throw new EntityNotFoundException("chat session not found");
        }

        return entity;
    }

    /**
     * 메시지 송수신 이후 세션의 마지막 대화 시각과 기본 제목을 갱신한다.
     *
     * @param sessionId 갱신할 채팅 세션 ID
     * @param titleCandidate 기본 제목을 대체할 수 있는 사용자 메시지
     * @return 갱신된 채팅 세션 DTO
     */
    @Transactional
    public ChatSessionDto touchAfterMessage(final Integer sessionId, final String titleCandidate) {
        final ChatSessionEntity entity = this.getMySessionEntity(sessionId);
        entity.setLastMessageAt(new Date());

        if (StringUtils.equals(entity.getTitle(), DEFAULT_TITLE) && StringUtils.isNotBlank(titleCandidate)) {
            entity.setTitle(toSessionTitle(titleCandidate));
        }

        return toDto(repository.saveAndFlush(entity));
    }

    /**
     * 로그인 사용자의 채팅 세션과 하위 메시지를 삭제한다.
     *
     * @param sessionId 삭제할 채팅 세션 ID
     */
    @Transactional
    public void delete(final Integer sessionId) {
        final ChatSessionEntity entity = this.getMySessionEntity(sessionId);
        chatMessageRepository.findAllBySessionIdOrderBySeqAscCreatedAtAsc(sessionId)
                .forEach(chatMessageRepository::delete);
        repository.delete(entity);
    }

    /**
     * 새 세션과 AI 호출에 사용할 기본 시스템 프롬프트를 반환한다.
     *
     * @return 기본 시스템 프롬프트
     */
    public String getDefaultSystemPrompt() {
        return DEFAULT_SYSTEM_PROMPT;
    }

    /**
     * 첫 사용자 메시지에서 세션 목록에 표시할 짧은 제목을 만든다.
     *
     * @param message 제목 후보가 되는 사용자 메시지
     * @return 세션 제목으로 사용할 축약 문자열
     */
    private String toSessionTitle(final String message) {
        final String compact = StringUtils.normalizeSpace(message);
        if (compact.length() <= 28) return compact;
        return compact.substring(0, 28) + "...";
    }

    /**
     * 채팅 세션 엔티티를 화면 응답용 DTO로 변환한다.
     *
     * @param entity 변환할 채팅 세션 엔티티
     * @return 채팅 세션 DTO
     */
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

    /**
     * 날짜 값을 화면 표시용 문자열로 변환한다.
     *
     * @param date 변환할 날짜 값
     * @return 날짜 문자열, 변환할 수 없으면 {@code null}
     */
    private String formatDate(final Date date) {
        try {
            return DateUtils.asStr(date, DatePtn.DATETIME);
        } catch (final Exception e) {
            return null;
        }
    }
}
