package com.guardian.backend.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guardian.backend.ai.dto.*;
import com.guardian.backend.ai.orchestrator.AiOrchestratorService;
import com.guardian.backend.ai.orchestrator.SafetyPolicy;
import com.guardian.backend.diagnostics.DiagnosticTroubleCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AiConversationService {

    private static final Logger log = LoggerFactory.getLogger(AiConversationService.class);

    private final AiOrchestratorService aiOrchestratorService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${guardian.ai.provider:GROQ}")
    private String aiProvider;

    @Value("${guardian.ai.groq.api-key:mock-key}")
    private String groqApiKey;

    @Value("${guardian.ai.groq.model:llama-3.3-70b-versatile}")
    private String groqModel;

    @Value("${guardian.ai.groq.api-url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqApiUrl;

    @Value("${guardian.ai.openrouter.api-key:mock-key}")
    private String openrouterApiKey;

    @Value("${guardian.ai.openrouter.model:meta-llama/llama-3.2-3b-instruct:free}")
    private String openrouterModel;

    @Value("${guardian.ai.openrouter.api-url:https://openrouter.ai/api/v1/chat/completions}")
    private String openrouterApiUrl;

    @Value("${guardian.ai.anthropic.api-key:mock-key}")
    private String anthropicApiKey;

    @Value("${guardian.ai.anthropic.model:claude-sonnet-5}")
    private String anthropicModel;

    @Value("${guardian.ai.anthropic.api-url:https://api.anthropic.com/v1/messages}")
    private String anthropicApiUrl;

    public AiConversationService(AiOrchestratorService aiOrchestratorService) {
        this.aiOrchestratorService = aiOrchestratorService;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(2500))
                .build();
    }

    public AiConversationResponse processConversation(AiConversationRequest request) {
        DriverContextDto driver = request.driver();
        VehicleContextDto vehicle = request.vehicle();

        String driverName = (driver != null && driver.name() != null && !driver.name().isBlank())
                ? driver.name() : "Driver";
        String vehicleName = (vehicle != null && vehicle.make() != null)
                ? (vehicle.make() + " " + (vehicle.model() != null ? vehicle.model() : ""))
                : "your car";

        // 1. Safety Policy Check (Deterministic Zero-Latency Short-circuit)
        Optional<AiConversationResponse> criticalAlert = SafetyPolicy.evaluate(request);
        if (criticalAlert.isPresent()) {
            return criticalAlert.get();
        }

        // 2. Cloud AI Provider Route if valid key is available
        try {
            if ("GROQ".equalsIgnoreCase(aiProvider) && isValidKey(groqApiKey)) {
                AiConversationResponse res = callOpenAiCompatibleLlm(groqApiUrl, groqApiKey, groqModel, request, driverName, vehicleName);
                if (res != null) return res;
            } else if ("OPENROUTER".equalsIgnoreCase(aiProvider) && isValidKey(openrouterApiKey)) {
                AiConversationResponse res = callOpenAiCompatibleLlm(openrouterApiUrl, openrouterApiKey, openrouterModel, request, driverName, vehicleName);
                if (res != null) return res;
            } else if ("ANTHROPIC".equalsIgnoreCase(aiProvider) && isValidKey(anthropicApiKey)) {
                AiConversationResponse res = callAnthropicLlm(request, driverName, vehicleName);
                if (res != null) return res;
            }
        } catch (Exception e) {
            log.warn("Cloud LLM call failed, engaging Guardian AI Orchestrator fallback engine: {}", e.getMessage());
        }

        // 3. Fallback to Guardian AI Orchestrator
        return aiOrchestratorService.orchestrate(request);
    }

    private boolean isValidKey(String key) {
        return key != null && !key.isBlank() && !"mock-key".equalsIgnoreCase(key);
    }

    private AiConversationResponse callOpenAiCompatibleLlm(String apiUrl, String apiKey, String model,
                                                           AiConversationRequest request, String driverName, String vehicleName) throws Exception {
        String systemPrompt = buildSystemPrompt(request, driverName, vehicleName);

        Map<String, Object> payload = Map.of(
                "model", model,
                "temperature", 0.2,
                "max_tokens", 300,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", request.userQuery())
                )
        );

        String jsonBody = objectMapper.writeValueAsString(payload);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofMillis(3000))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                String content = choices.get(0).path("message").path("content").asText();
                return parseLlmJsonOutput(content);
            }
        } else {
            log.error("OpenAI-compatible LLM endpoint returned status {}: {}", response.statusCode(), response.body());
        }
        return null;
    }

    private AiConversationResponse callAnthropicLlm(AiConversationRequest request, String driverName, String vehicleName) throws Exception {
        String systemPrompt = buildSystemPrompt(request, driverName, vehicleName);

        Map<String, Object> messagePayload = Map.of(
                "model", anthropicModel,
                "max_tokens", 300,
                "temperature", 0.2,
                "system", systemPrompt,
                "messages", List.of(
                        Map.of("role", "user", "content", request.userQuery())
                )
        );

        String jsonBody = objectMapper.writeValueAsString(messagePayload);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(anthropicApiUrl))
                .header("Content-Type", "application/json")
                .header("x-api-key", anthropicApiKey)
                .header("anthropic-version", "2023-06-01")
                .timeout(Duration.ofMillis(3000))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            AnthropicMessageResponse msgResponse = objectMapper.readValue(response.body(), AnthropicMessageResponse.class);
            if (msgResponse != null && msgResponse.content != null && !msgResponse.content.isEmpty()) {
                String text = msgResponse.content.get(0).text;
                return parseLlmJsonOutput(text);
            }
        } else {
            log.error("Anthropic API returned status {}: {}", response.statusCode(), response.body());
        }
        return null;
    }

    private String buildSystemPrompt(AiConversationRequest request, String driverName, String vehicleName) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are Guardian, an AI automotive co-driver for the driver inside the car.\n");
        sb.append("Your mission is to keep the driver informed, safe, and alert without causing distraction.\n\n");
        sb.append("GUIDELINES:\n");
        sb.append("1. Keep spoken replies short, natural, and concise (1-2 sentences maximum, strictly under 30 words).\n");
        sb.append("2. Ground your answers strictly in the real-time vehicle telemetry provided below.\n");
        sb.append("3. Always reply with a valid JSON object ONLY, with no extra markdown formatting or backticks, conforming to:\n");
        sb.append("{\n");
        sb.append("  \"spokenReply\": \"<1-2 short sentences for voice output>\",\n");
        sb.append("  \"displaySummary\": \"<Glanceable status for screen>\",\n");
        sb.append("  \"category\": \"<GENERAL|VEHICLE_HEALTH|POWERTRAIN|FUEL|DIAGNOSTICS|TRIP>\",\n");
        sb.append("  \"criticalAlert\": <true|false>,\n");
        sb.append("  \"followUps\": [\"<Short suggestion 1>\", \"<Short suggestion 2>\"]\n");
        sb.append("}\n\n");

        sb.append("CONTEXT:\n");
        sb.append("- Driver: ").append(driverName).append("\n");
        sb.append("- Vehicle: ").append(vehicleName).append("\n");

        if (request.telemetry() != null) {
            TelemetryContextDto t = request.telemetry();
            sb.append("- Telemetry: Speed=").append(t.speedKmh()).append(" km/h, RPM=").append(t.rpm())
                    .append(", Coolant=").append(t.coolantTempC()).append(" C, Battery=").append(t.batteryVoltage()).append(" V")
                    .append(", Fuel=").append(t.fuelLevelPct()).append(" %, ActiveDTCs=").append(t.dtcActive()).append("\n");
        }

        if (request.trip() != null) {
            TripContextDto tr = request.trip();
            sb.append("- Trip: Distance=").append(tr.distanceKm()).append(" km, Duration=").append(tr.durationMinutes()).append(" mins\n");
        }

        return sb.toString();
    }

    private AiConversationResponse parseLlmJsonOutput(String text) {
        try {
            String cleaned = text.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            } else if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();

            JsonNode node = objectMapper.readTree(cleaned);
            String spokenReply = node.has("spokenReply") ? node.get("spokenReply").asText("All systems running nominally.") : "All systems running nominally.";
            String displaySummary = node.has("displaySummary") ? node.get("displaySummary").asText("Status OK") : "Status OK";
            String category = node.has("category") ? node.get("category").asText("GENERAL") : "GENERAL";
            boolean criticalAlert = node.has("criticalAlert") && node.get("criticalAlert").asBoolean(false);

            List<String> followUps = new ArrayList<>();
            if (node.has("followUps") && node.get("followUps").isArray()) {
                for (JsonNode f : node.get("followUps")) {
                    followUps.add(f.asText());
                }
            }

            return new AiConversationResponse(spokenReply, displaySummary, category, criticalAlert, followUps, Instant.now());
        } catch (Exception e) {
            log.error("Failed to parse JSON response from LLM: {}", text, e);
            return new AiConversationResponse(text, "AI Response", "GENERAL", false, List.of(), Instant.now());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AnthropicMessageResponse {
        public List<ContentBlock> content;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ContentBlock {
        @SuppressWarnings("unused")
        public String type;
        public String text;
    }
}
