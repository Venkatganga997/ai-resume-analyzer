package com.resumeanalyzer.service;

import com.resumeanalyzer.model.AnalysisResult;
import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.repository.AnalysisRepository;
import com.resumeanalyzer.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeAnalysisService {

      private final OpenAIService openAIService;
      private final FileParserService fileParserService;
      private final ResumeRepository resumeRepository;
      private final AnalysisRepository analysisRepository;

      public AnalysisResult analyze(MultipartFile file, String jobDescription, String userId) throws IOException {
                log.info("Starting resume analysis for user: {}", userId);

                // parse resume text from PDF or DOCX
                String resumeText = fileParserService.extractText(file);

                // check cache first - same resume + JD combo
                String cacheKey = generateCacheKey(resumeText, jobDescription);

                // call GPT-4 for analysis
                AnalysisResult result = openAIService.analyzeResume(resumeText, jobDescription);
                result.setId(UUID.randomUUID().toString());
                result.setUserId(userId);
                result.setCreatedAt(LocalDateTime.now());
                result.setCacheKey(cacheKey);

                // save original resume file reference
                Resume resume = Resume.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(userId)
                        .fileName(file.getOriginalFilename())
                        .fileSize(file.getSize())
                        .uploadedAt(LocalDateTime.now())
                        .build();
                resumeRepository.save(resume);

                result.setResumeId(resume.getId());
                analysisRepository.save(result);

                log.info("Analysis complete. Score: {}", result.getMatchScore());
                return result;
            }

      public List<AnalysisResult> getHistory(String userId) {
                return analysisRepository.findByUserIdOrderByCreatedAtDesc(userId);
            }

      public AnalysisResult getById(String id, String userId) {
                return analysisRepository.findByIdAndUserId(id, userId)
                        .orElseThrow(() -> new RuntimeException("Analysis not found"));
            }

      private String generateCacheKey(String resumeText, String jobDescription) {
                // simple hash for cache key
                return String.valueOf((resumeText + jobDescription).hashCode());
            }
  }
