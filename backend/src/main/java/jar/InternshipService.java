package jar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InternshipService {

    private static final Logger logger = LoggerFactory.getLogger(InternshipService.class);

    @Autowired
    private InternshipRepository internshipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    private static final String JSEARCH_BASE_URL = "https://jsearch.p.rapidapi.com/search";
    private static final String JSEARCH_HOST = "jsearch.p.rapidapi.com";
    
    // IMPORTANT: Replace with your actual RapidAPI key
    private static final String JSEARCH_API_KEY = "YOUR_RAPIDAPI_KEY_HERE";

    /**
     * Fetch internships from JSearch API and save to database
     */
    public List<Internship> fetchAndSaveInternships(String query, int pages) {
        List<Internship> savedInternships = new ArrayList<>();
        
        try {
            for (int page = 1; page <= pages; page++) {
                List<Internship> pageInternships = fetchFromJSearchAPI(query, page);
                for (Internship internship : pageInternships) {
                    if (!internshipRepository.existsByExternalLink(internship.getExternalLink())) {
                        Internship saved = internshipRepository.save(internship);
                        savedInternships.add(saved);
                        logger.info("Saved internship: {}", saved.getTitle());
                    } else {
                        logger.debug("Internship already exists: {}", internship.getExternalLink());
                    }
                }
            }
            logger.info("Fetched and saved {} new internships", savedInternships.size());
        } catch (Exception e) {
            logger.error("Error fetching internships from external API", e);
        }
        
        return savedInternships;
    }

    /**
     * Fetch from JSearch API (RapidAPI)
     */
    private List<Internship> fetchFromJSearchAPI(String query, int page) {
        List<Internship> internships = new ArrayList<>();
        
        try {
            String url = String.format("%s?query=%s&page=%d&num_pages=1", 
                    JSEARCH_BASE_URL, query, page);

            Map<String, String> headers = new HashMap<>();
            headers.put("X-RapidAPI-Key", JSEARCH_API_KEY);
            headers.put("X-RapidAPI-Host", JSEARCH_HOST);

            // Note: RestTemplate doesn't directly support custom headers in GET
            // You may need to use interceptors or create a custom RestTemplate bean
            // For now, using a simple approach with string concatenation
            
            String response = restTemplate.getForObject(url, String.class);
            
            if (response != null) {
                internships = parseJSearchResponse(response);
            }
        } catch (Exception e) {
            logger.error("Error fetching from JSearch API", e);
        }
        
        return internships;
    }

    /**
     * Parse JSearch API response
     */
    private List<Internship> parseJSearchResponse(String jsonResponse) {
        List<Internship> internships = new ArrayList<>();
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);
            JsonNode dataArray = root.path("data");

            if (dataArray.isArray()) {
                for (JsonNode jobNode : dataArray) {
                    Internship internship = mapJSearchJobToInternship(jobNode);
                    if (internship != null) {
                        internships.add(internship);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing JSearch response", e);
        }
        
        return internships;
    }

    /**
     * Map JSearch job object to Internship entity
     */
    private Internship mapJSearchJobToInternship(JsonNode jobNode) {
        try {
            String jobId = jobNode.path("job_id").asText();
            if (jobId.isEmpty()) {
                return null;
            }

            String title = jobNode.path("job_title").asText("Not specified");
            String company = jobNode.path("employer_name").asText("Not specified");
            String jobDescription = jobNode.path("job_description").asText("");
                    
            // Extract skills from job description (simple extraction)
            extractSkills(jobDescription);
                    
            Internship internship = Internship.builder()
                    .title(title)
                    .company(company)
                    .source("JSearch")
                    .externalLink(jobId)
                    .build();
            
            return internship;
        } catch (Exception e) {
            logger.error("Error mapping job to internship", e);
            return null;
        }
    }

    /**
     * Extract skills from job description (simple regex-based extraction)
     */
    @Autowired
    private SkillRepository skillRepository;

    /**
     * Create internship using provided DTO logic
     */
    public jar.dto.InternshipDTO createInternship(jar.dto.CreateInternshipDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty() ||
            dto.getCompany() == null || dto.getCompany().trim().isEmpty()) {
            throw new IllegalArgumentException("Title and Company are required.");
        }

        Internship internship = new Internship();
        internship.setTitle(dto.getTitle());
        internship.setCompany(dto.getCompany());
        internship.setLocation(dto.getLocation());
        internship.setDescription(dto.getDescription());
        internship.setDuration(dto.getDuration());
        internship.setStipend(dto.getStipend());
        internship.setDeadline(dto.getDeadline());
        internship.setExternalLink(dto.getExternalLink());
        internship.setSource("admin");

        Internship saved = internshipRepository.save(internship);

        String skillsStr = dto.getSkills();
        if (skillsStr != null && !skillsStr.trim().isEmpty()) {
            String[] skillArray = skillsStr.split(",");
            for (String sk : skillArray) {
                String trimmed = sk.trim();
                if (!trimmed.isEmpty()) {
                    Skill skill = skillRepository.findByNameIgnoreCase(trimmed).orElseGet(() -> {
                        Skill newSkill = new Skill();
                        newSkill.setName(trimmed);
                        return skillRepository.save(newSkill);
                    });
                    skill.getInternships().add(saved);
                    skillRepository.save(skill);
                }
            }
        }

        return convertToDTO(saved, 0);
    }

    /**
     * Extract skills from job description (simple regex-based extraction)
     */
    private String extractSkills(String description) {
        if (description == null || description.isEmpty()) {
            return "Not specified";
        }

        String[] commonSkills = {
                "Java", "Python", "JavaScript", "TypeScript", "React", "Angular", "Vue",
                "Spring", "Node.js", "SQL", "MongoDB", "AWS", "Docker", "Kubernetes",
                "Git", "REST API", "GraphQL", "C++", "Go", "Rust", ".NET", "Azure",
                "Machine Learning", "Data Science", "DevOps", "CI/CD", "Linux", "HTML", "CSS"
        };

        Set<String> foundSkills = new HashSet<>();
        String lowerDesc = description.toLowerCase();

        for (String skill : commonSkills) {
            if (lowerDesc.contains(skill.toLowerCase())) {
                foundSkills.add(skill);
            }
        }

        if (foundSkills.isEmpty()) {
            return "Not specified";
        }

        return String.join(", ", foundSkills);
    }

    /**
     * Get all internships from database
     */
    public List<Internship> getAllInternships() {
        return internshipRepository.findAll();
    }

    /**
     * Get internships by company
     */
    public List<Internship> getInternshipsByCompany(String company) {
        return internshipRepository.findByCompany(company);
    }


    /**
     * Search internships by title
     */
    public List<Internship> searchInternships(String title) {
        return internshipRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * Get internships by source
     */
    public List<Internship> getInternshipsBySource(String source) {
        return internshipRepository.findBySource(source);
    }

    /**
     * Get internship by ID
     */
    public Optional<Internship> getInternshipById(Long id) {
        return internshipRepository.findById(id);
    }

    /**
     * Match internships for a user based on their skills using weighted scoring.
     * 
     * Scoring Algorithm:
     * - For each required skill in the internship:
     *   - Exact match with user skill: +2
     *   - Partial match (skill name contains user skill keyword): +1
     *   - Title contains required skill: +1
     * - For each user skill:
     *   - Title contains user skill (not already matched): +1
     * 
     * Sorting: By score (descending), then by creation date (most recent first)
     * 
     * If user has no skills, return all internships sorted by creation date.
     */
    public List<InternshipMatchResult> matchInternshipsForUser(Long userId) {
        logger.info("Matching internships for user: {}", userId);
        
        // Fetch user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get user's skills
        Set<Skill> userSkills = user.getSkills();
        logger.debug("User {} has {} skills", userId, userSkills.size());
        
        // Fetch all internships
        List<Internship> allInternships = internshipRepository.findAll();
        logger.debug("Total internships in database: {}", allInternships.size());
        
        // If user has no skills, return all internships sorted by creation date
        if (userSkills.isEmpty()) {
            logger.info("User has no skills, returning all internships sorted by date");
            return allInternships.stream()
                    .map(internship -> new InternshipMatchResult(internship, 0))
                    .sorted(Comparator.comparing(InternshipMatchResult::getCreatedAt).reversed())
                    .collect(Collectors.toList());
        }
        
        // Match internships using weighted scoring
        return allInternships.stream()
                .map(internship -> {
                    int score = calculateMatchScore(internship, userSkills);
                    return new InternshipMatchResult(internship, score);
                })
                .filter(result -> result.getScore() > 0)  // Only include internships with at least some match
                .sorted(Comparator
                        .comparingInt(InternshipMatchResult::getScore).reversed()  // Sort by score (highest first)
                        .thenComparing(result -> result.getCreatedAt()).reversed())  // Then by date (most recent first)
                .collect(Collectors.toList());
    }

    /**
     * Calculate weighted relevance score for an internship based on user's skills.
     * 
     * Scoring:
     * - Exact skill match: +2
     * - Partial match (skill name contains user skill): +1
     * - Title contains required skill: +1
     * - Title contains user skill (not already matched): +1
     */
    private int calculateMatchScore(Internship internship, Set<Skill> userSkills) {
        int score = 0;
        
        // Prepare user skill names in lowercase for efficient matching
        Set<String> userSkillNames = userSkills.stream()
                .map(skill -> skill.getName().toLowerCase())
                .collect(Collectors.toSet());
        
        String titleLower = internship.getTitle().toLowerCase();
        
        // Score for matching required skills
        for (Skill requiredSkill : internship.getSkills()) {
            String skillNameLower = requiredSkill.getName().toLowerCase();
            
            // Exact match: +2
            if (userSkillNames.contains(skillNameLower)) {
                score += 2;
                logger.debug("Exact match: {} (+2)", skillNameLower);
                
                // Also bonus if title contains this skill
                if (titleLower.contains(skillNameLower)) {
                    score += 1;
                    logger.debug("Title contains skill: {} (+1)", skillNameLower);
                }
            } else {
                // Partial match: +1 (if required skill contains a user skill keyword)
                for (String userSkill : userSkillNames) {
                    if (skillNameLower.contains(userSkill)) {
                        score += 1;
                        logger.debug("Partial match: {} contains {} (+1)", skillNameLower, userSkill);
                        break; // Count only once per required skill
                    }
                }
                
                // Title bonus: +1 if title contains required skill
                if (titleLower.contains(skillNameLower)) {
                    score += 1;
                    logger.debug("Title contains required skill: {} (+1)", skillNameLower);
                }
            }
        }
        
        // Additional bonus: if title contains user's skills not already in required skills
        for (String userSkill : userSkillNames) {
            if (titleLower.contains(userSkill)) {
                // Check if this skill is already in the required skills (to avoid double counting)
                boolean isRequiredSkill = internship.getSkills().stream()
                        .anyMatch(s -> s.getName().toLowerCase().equals(userSkill));
                if (!isRequiredSkill) {
                    score += 1;
                    logger.debug("Title contains user skill: {} (+1)", userSkill);
                }
            }
        }
        
        return score;
    }

    /**
     * Convert Internship to InternshipDTO with relevance score.
     */
    public jar.dto.InternshipDTO convertToDTO(Internship internship, int score) {
        Set<jar.dto.SkillDTO> skillDTOs = internship.getSkills().stream()
                .map(skill -> jar.dto.SkillDTO.builder()
                        .id(skill.getId())
                        .name(skill.getName())
                        .build())
                .collect(Collectors.toSet());
        
        return jar.dto.InternshipDTO.builder()
                .id(internship.getId())
                .title(internship.getTitle())
                .company(internship.getCompany())
                .source(internship.getSource())
                .externalLink(internship.getExternalLink())
                .location(internship.getLocation())
                .description(internship.getDescription())
                .duration(internship.getDuration())
                .stipend(internship.getStipend())
                .deadline(internship.getDeadline())
                .createdAt(internship.getCreatedAt())
                .skills(skillDTOs)
                .score(score)
                .build();
    }

    /**
     * Delete old internships to keep database clean
     */
    public void deleteInternshipsOlderThanDays(int days) {
        List<Internship> allInternships = internshipRepository.findAll();
        int deletedCount = 0;

        for (Internship internship : allInternships) {
            if (internship.getCreatedAt() != null) {
                long daysDifference = java.time.temporal.ChronoUnit.DAYS.between(
                        internship.getCreatedAt().toLocalDate(),
                        java.time.LocalDate.now()
                );
                if (daysDifference > days) {
                    internshipRepository.delete(internship);
                    deletedCount++;
                }
            }
        }

        logger.info("Deleted {} old internships", deletedCount);
    }
}
