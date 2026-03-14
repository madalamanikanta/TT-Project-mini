package jar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateInternshipDTO {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Company is required")
    private String company;

    private String location;
    private String description;
    private String duration;
    private String stipend;
    private String deadline;
    private String externalLink;
    private String skills;
}
