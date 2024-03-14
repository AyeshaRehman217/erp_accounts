package tuf.webscaf.app.dbContext.slave.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("public.\"balance_and_income_lines\"")
public class SlaveBalanceAndIncomeLineEntity {

    @Id
    @Column("id")
    @Schema(hidden = true)
    private Long id;

    @Version
    @Schema(hidden = true)
    private Long version;

    @Column("uuid")
    private UUID uuid;

    @Column("status")
    private Boolean status;

    @Column("visible_position_index")
    private Integer visiblePositionIndex;

    @Column("name")
    private String name;

    @Column("description")
    private String description;

    @Column("line_text_show")
    private String lineTextShow;

    @Column("printed_no")
    private String printedNo;

    @Column("total_of_line_positions")
    private String totalOfLinePositions;

    @Column("line_type")
    private String lineType;

    @Column("line_position")
    private String linePosition;

    @Column("line_value_type")
    private String lineValueType;

    @Column("line_intendentation")
    private String lineIndentation;

    @Column("line_side")
    private String lineSide;

    @Column("balance_and_income_report_id")
    private Long balanceIncomeReportId;

    @Column("created_by")
    @CreatedBy
    private UUID createdBy;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_by")
    @CreatedBy
    private UUID updatedBy;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column("deleted_by")
    private UUID deletedBy;

    @Column("deleted_at")
    private LocalDateTime deletedAt;

    @Column("req_company_uuid")
    private UUID reqCompanyUUID;

    @Column("req_branch_uuid")
    private UUID reqBranchUUID;

    @Column("req_created_browser")
    private String reqCreatedBrowser;

    @Column("req_created_ip")
    private String reqCreatedIP;

    @Column("req_created_port")
    private String reqCreatedPort;

    @Column("req_created_os")
    private String reqCreatedOS;

    @Column("req_created_device")
    private String reqCreatedDevice;

    @Column("req_created_referer")
    private String reqCreatedReferer;

    @Column("req_updated_browser")
    private String reqUpdatedBrowser;

    @Column("req_updated_ip")
    private String reqUpdatedIP;

    @Column("req_updated_port")
    private String reqUpdatedPort;

    @Column("req_updated_os")
    private String reqUpdatedOS;

    @Column("req_updated_device")
    private String reqUpdatedDevice;

    @Column("req_updated_referer")
    private String reqUpdatedReferer;

    @Column("req_deleted_browser")
    private String reqDeletedBrowser;

    @Column("req_deleted_ip")
    private String reqDeletedIP;

    @Column("req_deleted_port")
    private String reqDeletedPort;

    @Column("req_deleted_os")
    private String reqDeletedOS;

    @Column("req_deleted_device")
    private String reqDeletedDevice;

    @Column("req_deleted_referer")
    private String reqDeletedReferer;

    @Column
    @Schema(hidden = true)
    private Boolean editable;

    @Column
    @Schema(hidden = true)
    private Boolean deletable;

    @Column
    @Schema(hidden = true)
    private Boolean archived;
}
