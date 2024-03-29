package tuf.webscaf.app.dbContext.master.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class BranchWithBranchProfileDto {

    private Long id;

    private Long version;

    private UUID uuid;

    private Boolean status;

    private String name;

    private String description;

    private UUID companyUUID;

    private UUID branchProfileUUID;

    private LocalDateTime establishmentDate;

    private UUID languageUUID;

    private UUID locationUUID;

    private UUID createdBy;

    private UUID updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private UUID reqCompanyUUID;

//    private UUID reqBranchUUID;

    private String reqCreatedBrowser;

    private String reqCreatedIP;

    private String reqCreatedPort;

    private String reqCreatedOS;

    private String reqCreatedDevice;

    private String reqCreatedReferer;

    private String reqUpdatedBrowser;

    private String reqUpdatedIP;

    private String reqUpdatedPort;

    private String reqUpdatedOS;

    private String reqUpdatedDevice;

    private String reqUpdatedReferer;

    private Boolean editable;

    private Boolean archived;

    private Boolean deletable;
}
