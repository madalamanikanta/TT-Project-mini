package jar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
@RequestMapping("/api/internships")
public class InternshipController {

    private static final Logger logger = LoggerFactory.getLogger(InternshipController.class);

    @Autowired
    private InternshipService internshipService;

    // used when endpoints need the user id from JWT
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * GET /api/internships
     * Fetch all internships from database
     */
    @GetMapping
    public ResponseEntity<?> getAllInternships() {
        try {
            List<Internship> internships = internshipService.getAllInternships();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", internships.size());
            response.put("data", internships);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching internships", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching internships: " + e.getMessage()));
        }
    }

    /**
     * GET /api/internships/fetch
     * Fetch internships from external API, save to DB, and return data
     */
    @GetMapping("/fetch")
    public ResponseEntity<?> fetchInternships(
            @RequestParam(defaultValue = "internship software") String query,
            @RequestParam(defaultValue = "2") int pages) {
        try {
            logger.info("Fetching internships with query: {}", query);
            List<Internship> newInternships = internshipService.fetchAndSaveInternships(query, pages);
            List<Internship> allInternships = internshipService.getAllInternships();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully fetched and saved internships");
            response.put("newCount", newInternships.size());
            response.put("totalCount", allInternships.size());
            response.put("data", allInternships);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching internships from external API", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching internships: " + e.getMessage()));
        }
    }

    /**
     * GET /api/internships/search
     * Search internships by title
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchInternships(@RequestParam String title) {
        try {
            List<Internship> internships = internshipService.searchInternships(title);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("query", title);
            response.put("count", internships.size());
            response.put("data", internships);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error searching internships", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error searching internships: " + e.getMessage()));
        }
    }


    /**
     * GET /api/internships/company/{company}
     * Get internships by company
     */
    @GetMapping("/company/{company}")
    public ResponseEntity<?> getInternshipsByCompany(@PathVariable String company) {
        try {
            List<Internship> internships = internshipService.getInternshipsByCompany(company);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("company", company);
            response.put("count", internships.size());
            response.put("data", internships);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching internships by company", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * GET /api/internships/source/{source}
     * Get internships by source
     */
    @GetMapping("/source/{source}")
    public ResponseEntity<?> getInternshipsBySource(@PathVariable String source) {
        try {
            List<Internship> internships = internshipService.getInternshipsBySource(source);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("source", source);
            response.put("count", internships.size());
            response.put("data", internships);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching internships by source", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching internships: " + e.getMessage()));
        }
    }

    /**
     * GET /api/internships/{id}
     * Get internship by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getInternshipById(@PathVariable Long id) {
        try {
            return internshipService.getInternshipById(id)
                    .map(internship -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("data", internship);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(createErrorResponse("Internship not found with ID: " + id)));
        } catch (Exception e) {
            logger.error("Error fetching internship by ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching internship: " + e.getMessage()));
        }
    }

    /**
     * GET /api/internships/match/{userId}
     * Get internships matched to user's skills, sorted by relevance score
     * (legacy/administrative endpoint - still available)
     */
    @GetMapping("/match/{userId}")
    public ResponseEntity<?> matchInternshipsForUser(@PathVariable Long userId) {
        try {
            logger.info("Fetching matched internships for user: {}", userId);
            List<InternshipMatchResult> matchResults = internshipService.matchInternshipsForUser(userId);
            
            // Convert to DTOs with weighted relevance score
            List<jar.dto.InternshipDTO> internshipDTOs = matchResults.stream()
                    .map(result -> internshipService.convertToDTO(result.getInternship(), result.getScore()))
                    .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("count", internshipDTOs.size());
            response.put("data", internshipDTOs);
            
            logger.info("Found {} matched internships for user {}", internshipDTOs.size(), userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Error matching internships: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error matching internships for user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error matching internships: " + e.getMessage()));
        }
    }

    /**
     * Helper method to create error response
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return response;
    }

    /**
     * GET /api/internships/matches
     * Same as /match/{userId} but uses the authenticated user from JWT.
     */
    @GetMapping("/matches")
    public ResponseEntity<?> matchInternshipsForCurrentUser(HttpServletRequest request) {
        try {
            Long userId = extractUserId(request);
            logger.info("Fetching matched internships for current user: {}", userId);
            List<InternshipMatchResult> matchResults = internshipService.matchInternshipsForUser(userId);
            List<jar.dto.InternshipDTO> internshipDTOs = matchResults.stream()
                    .map(result -> internshipService.convertToDTO(result.getInternship(), result.getScore()))
                    .collect(java.util.stream.Collectors.toList());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("count", internshipDTOs.size());
            response.put("data", internshipDTOs);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Error matching internships: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error matching internships for current user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error matching internships: " + e.getMessage()));
        }
    }

    /**
     * Pull user id from JWT in Authorization header. Throws runtime exception if token missing.
     */
    private Long extractUserId(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            return jwtUtil.extractUserId(token);
        }
        throw new RuntimeException("Missing Authorization header");
    }
}
