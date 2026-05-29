package com.resumeanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeanalyzer.model.AnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIService {

      @Value("${openai.api.key}")
      private String apiKey;

      @Value("${openai.model:gpt-4}")
      private String model;

      private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

      private final RestTemplate restTemplate;
      private final ObjectMapper objectMapper;

      public AnalysisResult analyzeResume(String resumeText, String jobDescription) {
                String prompt = buildPrompt(resumeText, jobDescription);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", model);
                requestBody.put("temperature", 0.3);

                Map<String, String> message = new HashMap<>();
                message.put("role", "user");
                message.put("content", prompt);
                requestBody.put("messages", List.of(message));

                // tell GPT to respond with JSON
                Map<String, String> responseFormat = new HashMap<>();
                responseFormat.put("type", "json_object");
                requestBody.put("response_format", responseFormat);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                try {
                              ResponseEntity<String> response = restTemplate.exchange(OPENAI_URL, HttpMethod.POST, request, String.class);
                              return parseResponse(response.getBody());
                          } catch (Exception e) {
                              log.error("OpenAI API call failed: {}", e.getMessage());
                              throw new RuntimeException("Analysis failed, please try again");
                          }
            }

      private String buildPrompt(String resumeText, String jobDescription) {
                return """
                You are a resume screening expert. Analyze the resume against the job description.

                Resume:
                %s

                Job Description:
                %s

                Respond in JSON format:
                {
                  "matchScore": <0-100>,
                  "matchedSkills": ["skill1", "skill2"],
                  "missingSkills": ["skill1", "skill2"],
                  "experienceMatch": "good|partial|poor",
                  "summary": "brief overall assessment",
                  "suggestions": ["suggestion1", "suggestion2", "suggestion3"]
                }
                """.formatted(resumeText, jobDescription);
            }

      private AnalysisResult parseResponse(String responseBody) throws Exception {
                JsonNode root = objectMapper.readTree(responseBody);
                String content = root.path("choices").get(0).path("message").path("content").asText();
                JsonNode result = objectMapper.readTree(content);

                AnalysisResult analysis = new AnalysisResult();
                analysis.setMatchScore(result.path("matchScore").asInt());
                analysis.setSummary(result.path("summary").asText());
                analysis.setExperienceMatch(result.path("experienceMatch").asText());

                List<String> matched = new ArrayList<>();
                result.path("matchedSkills").forEach(n -> matched.add(n.asText()));
                analysis.setMatchedSkills(matched);

                List<String> missing = new ArrayList<>();
                result.path("missingSkills").forEach(n -> missing.add(n.asText()));
                analysis.setMissingSkills(missing);

                List<String> suggestions = new ArrayList<>();
                result.path("suggestions").forEach(n -> suggestions.add(n.asText()));
                analysis.setSuggestions(suggestions);

                return analysis;
            }
  }
