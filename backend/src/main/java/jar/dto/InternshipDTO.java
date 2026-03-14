package jar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for internship response in matching results.
 * Avoids JSON recursion by not including bi-directional skill relationship.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipDTO {
    private Long id;
    private String title;
    private String company;
    private String source;
    private String externalLink;
    private String location;
    private String description;
    private String duration;
    private String stipend;
    private String deadline;
    private LocalDateTime createdAt;
    private Set<SkillDTO> skills;
    
    /**
     * Weighted relevance score for this internship:
     * - Exact skill match: +2
     * - Partial skill match (keyword): +1
     * - Title contains required skill: +1
     * - Title contains user skill: +1
     * Used for sorting by relevance.
     */
    private int score;
}
