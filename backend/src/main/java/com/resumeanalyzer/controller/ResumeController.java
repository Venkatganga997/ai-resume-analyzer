package com.resumeanalyzer.controller;

import com.resumeanalyzer.model.AnalysisResult;
import com.resumeanalyzer.service.ResumeAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "${app.frontend.url}"})
public class ResumeController {

      private final ResumeAnalysisService analysisService;

      @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
      public ResponseEntity<AnalysisResult> analyze(
                    @RequestPart("file") MultipartFile file,
                    @RequestPart("jobDescription") String jobDescription,
                    @AuthenticationPrincipal UserDetails userDetails) {

                if (file.isEmpty()) {
                              return ResponseEntity.badRequest().build();
                          }

                // only allow pdf and docx
                String contentType = file.getContentType();
                if (contentType == null ||
                                    (!contentType.equals("application/pdf") &&
                                                     !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
                              return ResponseEntity.badRequest().build();
                          }

                try {
                              AnalysisResult result = analysisService.analyze(file, jobDescription, userDetails.getUsername());
                              return ResponseEntity.ok(result);
                          } catch (Exception e) {
                              return ResponseEntity.internalServerError().build();
                          }
            }

      @GetMapping("/history")
      public ResponseEntity<List<AnalysisResult>> getHistory(
                    @AuthenticationPrincipal UserDetails userDetails) {
                List<AnalysisResult> history = analysisService.getHistory(userDetails.getUsername());
                return ResponseEntity.ok(history);
            }

      @GetMapping("/{id}")
      public ResponseEntity<AnalysisResult> getById(
                    @PathVariable String id,
                    @AuthenticationPrincipal UserDetails userDetails) {
                try {
                              AnalysisResult result = analysisService.getById(id, userDetails.getUsername());
                              return ResponseEntity.ok(result);
                          } catch (RuntimeException e) {
                              return ResponseEntity.notFound().build();
                          }
            }
  }
