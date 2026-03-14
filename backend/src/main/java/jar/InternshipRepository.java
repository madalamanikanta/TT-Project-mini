package jar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InternshipRepository extends JpaRepository<Internship, Long> {

    /**
     * Find internship by external job ID to avoid duplicates
     */
    Optional<Internship> findByExternalLink(String externalLink);

    /**
     * Find internships by company
     */
    List<Internship> findByCompany(String company);

    /**
     * Search internships by title (case-insensitive)
     */
    List<Internship> findByTitleContainingIgnoreCase(String title);

    /**
     * Find internships by source (external API)
     */
    List<Internship> findBySource(String source);

    /**
     * Check if internship with external link already exists
     */
    boolean existsByExternalLink(String externalLink);
}
