package jar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import jar.dto.UserDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API endpoints for admin-only functionality.
 */
@RestController
@RequestMapping("/api/admin")
public class ApiAdminController {

    private static final Logger logger = LoggerFactory.getLogger(ApiAdminController.class);

    private final UserService userService;
    private final UserRepository userRepository;
    private final InternshipRepository internshipRepository;
    private final SavedInternshipRepository savedInternshipRepository;
    private final InternshipService internshipService;

    public ApiAdminController(UserService userService, UserRepository userRepository, InternshipRepository internshipRepository, SavedInternshipRepository savedInternshipRepository, InternshipService internshipService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.internshipRepository = internshipRepository;
        this.savedInternshipRepository = savedInternshipRepository;
        this.internshipService = internshipService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(Authentication authentication) {
        logger.info("Admin dashboard accessed by {}", authentication == null ? "anonymous" : authentication.getName());

        long totalUsers = userRepository.count();
        long totalAdmins = userRepository.countByRole(User.Role.ADMIN);
        long totalStudents = totalUsers - totalAdmins;
        long totalInternships = internshipRepository.count();

        // Use the last 5 saved internships as a proxy for "Recent Applications" since no Application entity exists yet
        List<SavedInternship> allSaved = savedInternshipRepository.findAll();
        // sort descending by ID to simulate newest first
        allSaved.sort((a,b) -> b.getId().compareTo(a.getId()));
        List<Map<String, String>> recentActivity = allSaved.stream().limit(5).map(si -> {
            String userName = userRepository.findById(si.getUserId()).map(User::getEmail).orElse("Unknown User");
            String internTitle = internshipRepository.findById(si.getInternshipId()).map(Internship::getTitle).orElse("Unknown Internship");
            return Map.of(
                "action", "Saved/Applied to Internship",
                "user", userName + " -> " + internTitle,
                "time", si.getSavedAt() != null ? si.getSavedAt().toString() : "Recently"
            );
        }).collect(Collectors.toList());

        // fallback if no activity exists
        if (recentActivity.isEmpty()) {
            recentActivity = List.of(
                Map.of("action", "System Started", "user", "System", "time", "Just now")
            );
        }

        return ResponseEntity.ok(Map.of(
                "message", "Admin dashboard data",
                "totalUsers", totalUsers,
                "totalAdmins", totalAdmins,
                "totalStudents", totalStudents,
                "totalInternships", totalInternships,
                "recentActivity", recentActivity
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> listUsers() {
        List<UserDTO> users = userRepository.findAll().stream()
                .map(userService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/internships")
    public ResponseEntity<List<jar.dto.InternshipDTO>> listInternships() {
        List<jar.dto.InternshipDTO> internships = internshipRepository.findAll().stream()
                .map(internship -> internshipService.convertToDTO(internship, 0))
                .collect(Collectors.toList());
        return ResponseEntity.ok(internships);
    }

    @org.springframework.web.bind.annotation.PostMapping("/internships")
    public ResponseEntity<?> createInternship(@org.springframework.web.bind.annotation.RequestBody jar.dto.CreateInternshipDTO dto) {
        try {
            jar.dto.InternshipDTO result = internshipService.createInternship(dto);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating internship", e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getSettings() {
        // Return dynamic simulated settings 
        return ResponseEntity.ok(Map.of(
                "appName", "Internship Platform",
                "version", "1.1.0",
                "maintenanceMode", false,
                "registrationEnabled", true,
                "apiRequestsLast24h", 1240
        ));
    }
}
