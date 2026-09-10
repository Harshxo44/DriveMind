package com.velora.backend.ai;

import com.velora.backend.ai.dto.AiConversationRequest;
import com.velora.backend.ai.dto.AiConversationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
public class AiConversationController {

    private final AiConversationService aiConversationService;

    public AiConversationController(AiConversationService aiConversationService) {
        this.aiConversationService = aiConversationService;
    }

    @PostMapping("/conversation")
    public ResponseEntity<AiConversationResponse> handleConversation(@Valid @RequestBody AiConversationRequest request) {
        AiConversationResponse response = aiConversationService.processConversation(request);
        return ResponseEntity.ok(response);
    }
}
