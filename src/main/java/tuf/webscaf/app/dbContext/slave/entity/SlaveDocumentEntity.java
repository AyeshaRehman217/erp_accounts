package tuf.webscaf.app.dbContext.slave.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SlaveDocumentEntity {
    @Id
    @Column
    @Schema(hidden = true)
    private Long id;

    @Version
    @Schema(hidden = true)
    private Long version;

    @Column("module_id")
    private Long moduleId;

    @Column("doc_type_uuid")
    private UUID docTypeUUID;

    @Column("doc_bucket_uuid")
    private UUID docBucketUUID;

    @Column("doc_nature_uuid")
    private UUID docNatureUUID;

    @Column
    private String name;

    @Column
    private String slug;

    @Column
    private String extension;

    @Column
    private String url;

    @Column("created_at_year")
    private Integer createdAtYear;

    @Column("created_at_month")
    private Integer createdAtMonth;

    @Column("created_at_day")
    private Integer createdAtDay;

    @Column("doc_id")
    private UUID docId;

    @Column("fiscal_year_id")
    private Long fiscalYearId;

    @Column("is_submitted")
    private Boolean isSubmitted;

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
    private Boolean editable;

    @Column
    private Boolean deletable;

    @Column
    private Boolean archived;

}
