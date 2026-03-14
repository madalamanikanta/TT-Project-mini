package jar;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Persistent record for internships that have been bookmarked/saved by users.
 */
@Entity
@Table(name = "saved_internships",
       uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "internship_id"})},
       indexes = {
           @Index(name = "idx_saved_user", columnList = "user_id"),
           @Index(name = "idx_saved_internship", columnList = "internship_id")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SuppressWarnings("serial")
public class SavedInternship {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "internship_id", nullable = false)
    private Long internshipId;

    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;

    @PrePersist
    protected void onCreate() {
        this.savedAt = LocalDateTime.now();
    }
}
