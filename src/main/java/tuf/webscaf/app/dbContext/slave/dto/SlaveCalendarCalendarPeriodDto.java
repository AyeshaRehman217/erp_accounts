package tuf.webscaf.app.dbContext.slave.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SlaveCalendarCalendarPeriodDto {
    private Long id;
    private Long version;
    private UUID uuid;
    private String name;
    private String description;
    private Boolean status;
    private LocalDateTime fiscalYear;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private UUID calendarTypeUUID;
    private UUID calendarPeriodUUID;
    private Integer quarter;
    private Integer periodNo;
    private Boolean isOpen;
    private Boolean adjustment;
    private UUID createdBy;
    private UUID updatedBy;
    private UUID deletedBy;
    private LocalDateTime deletedAt;
    private UUID reqCompanyUUID;
    private UUID reqBranchUUID;
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
    private String reqDeletedBrowser;
    private String reqDeletedIP;
    private String reqDeletedPort;
    private String reqDeletedOS;
    private String reqDeletedDevice;
    private String reqDeletedReferer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean archived;
    private Boolean deletable;
    private Boolean editable;
}
