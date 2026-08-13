package io.nicheblog.dreamdiary.feature.chat.service;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ChatOrchestratorOwnershipTest {

    @Test
    void validatesSessionOwnershipBeforeCancellingChat() {
        final ChatSessionService chatSessionService = mock(ChatSessionService.class);
        final ChatOrchestrator service = new ChatOrchestrator(
                null,
                chatSessionService,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        service.cancelChat(17);

        verify(chatSessionService).getMySessionEntity(17);
    }

    @Test
    void ignoresCancelRequestWithoutSessionId() {
        final ChatSessionService chatSessionService = mock(ChatSessionService.class);
        final ChatOrchestrator service = new ChatOrchestrator(
                null,
                chatSessionService,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        service.cancelChat(null);

        verifyNoInteractions(chatSessionService);
    }
}
