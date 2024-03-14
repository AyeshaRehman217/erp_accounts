package tuf.webscaf.app.dbContext.master.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FinancialAccountDto {
    private Long id;

    private Long version;

    private UUID uuid;

    private Boolean status;

    private String name;

    private String code;

    private String description;

    private Boolean isEntryAllowed;

    private UUID accountTypeUUID;

    private UUID parentAccountUUID;

    private UUID companyUUID;

    private UUID campusUUID;

    private String controlCode;

    private Boolean counterFlag;

    private Long level;

    private Boolean isFinancialParent;

    private UUID createdBy;

    private LocalDateTime createdAt;

    private UUID updatedBy;

    private LocalDateTime updatedAt;

    private UUID deletedBy;

    private LocalDateTime deletedAt;

    private Boolean editable;

    private Boolean deletable;

    private Boolean archived;
}
