# AI Resume Analyzer

A full-stack application that analyzes resumes against job descriptions using GPT-4 and gives candidates a match score with detailed feedback. Built this to solve a real problem - most people don't know why they're getting rejected, this tool tells them exactly what's missing.

## Tech Stack

**Backend:** Java 17, Spring Boot 3, Spring Security, PostgreSQL, Redis (caching), Docker
**Frontend:** React 18, TypeScript, Tailwind CSS, Axios
**AI Layer:** OpenAI GPT-4 API, Apache PDFBox (PDF parsing), Apache Tika (DOCX parsing)
**Infra:** AWS EC2, S3 (resume storage), RDS PostgreSQL, GitHub Actions CI/CD

## Features

- Upload resume as PDF or DOCX
- - Paste any job description
  - - Get a match score (0-100) with section-by-section breakdown
    - - Missing keywords and skills highlighted
      - - Actionable suggestions to improve the resume
        - - History of all past analyses per user
          - - REST API with JWT auth
           
            - ## Project Structure
           
            - ```
              ai-resume-analyzer/
                backend/
                  src/main/java/com/resumeanalyzer/
                    controller/
                      ResumeController.java
                      AuthController.java
                    service/
                      ResumeAnalysisService.java
                      OpenAIService.java
                      FileParserService.java
                    model/
                      Resume.java
                      AnalysisResult.java
                      User.java
                    repository/
                      ResumeRepository.java
                      AnalysisRepository.java
                    config/
                      SecurityConfig.java
                      OpenAIConfig.java
                  pom.xml
                frontend/
                  src/
                    components/
                      ResumeUpload.tsx
                      AnalysisResult.tsx
                      Dashboard.tsx
                    hooks/
                      useAnalysis.ts
                    api/
                      resumeApi.ts
                  package.json
                docker-compose.yml
              ```

              ## Running locally

              ```bash
              # clone repo
              git clone https://github.com/Venkatganga997/ai-resume-analyzer.git

              # backend
              cd backend
              cp .env.example .env
              # add your OPENAI_API_KEY in .env
              mvn spring-boot:run

              # frontend (separate terminal)
              cd frontend
              npm install
              npm start
              ```

              App runs at http://localhost:3000, API at http://localhost:8080

              ## API Endpoints

              | Method | Endpoint | Description |
              |--------|----------|-------------|
              | POST | /api/auth/login | Login |
              | POST | /api/auth/register | Register |
              | POST | /api/resume/analyze | Analyze resume vs JD |
              | GET | /api/resume/history | Past analyses |
              | GET | /api/resume/{id} | Single analysis result |

              ## How it works

              1. User uploads resume, system parses text from PDF/DOCX using Apache PDFBox/Tika
              2. 2. Job description pasted by user
                 3. 3. Both sent to GPT-4 with a structured prompt asking for match score + breakdown
                    4. 4. GPT-4 returns JSON with score, matched skills, missing skills, suggestions
                       5. 5. Result stored in PostgreSQL, returned to frontend
                          6. 6. Redis caches repeated analyses for same resume+JD combo (cuts API costs)
                            
                             7. Built this while job hunting myself, got tired of sending resumes into a black hole.
