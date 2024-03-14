package tuf.webscaf.app.dbContext.master.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AccountGroupDto {
    private Long id;
    private Long version;
    private String name;
    private String description;
    private Boolean status;

    //Get Account  Entity
    private List<Long> accountId;
    private UUID createdBy;
    private UUID updatedBy;
    private UUID deletedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean archived;
    private Boolean editable;
}
