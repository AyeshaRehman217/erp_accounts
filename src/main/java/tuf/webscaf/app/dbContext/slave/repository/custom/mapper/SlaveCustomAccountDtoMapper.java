package tuf.webscaf.app.dbContext.slave.repository.custom.mapper;

import io.r2dbc.spi.Row;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAccountDto;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.BiFunction;

public class SlaveCustomAccountDtoMapper implements BiFunction<Row, Object, SlaveAccountDto> {

    @Override
    public SlaveAccountDto apply(Row source, Object o) {

        return SlaveAccountDto.builder()
                .id(source.get("id", Long.class))
                .version(source.get("version", Long.class))
                .uuid(source.get("uuid", UUID.class))
                .name(source.get("name", String.class))
                .description(source.get("description", String.class))
                .code(source.get("code", String.class))
                .accountTypeUUID(source.get("account_type_uuid", UUID.class))
//                .subAccountTypeUUID(source.get("sub_account_type_uuid", UUID.class))
                .parentAccountUUID(source.get("parent_account_uuid", UUID.class))
                .isOpeningBalance(source.get("is_opening_balance", Boolean.class))
                .openingBalanceUUID(source.get("opening_balance_uuid", UUID.class))
                .companyUUID(source.get("company_uuid", UUID.class))
                .controlCode(source.get("control_code", String.class))
                .level(source.get("level", Long.class))
                .isEntryAllowed(source.get("is_entry_allowed", Boolean.class))
                .status(source.get("status", Boolean.class))
                .createdBy(source.get("created_by", UUID.class))
                .createdAt(source.get("created_at", LocalDateTime.class))
                .updatedAt(source.get("updated_at", LocalDateTime.class))
                .updatedBy(source.get("updated_by", UUID.class))
                .deletedBy(source.get("deleted_by", UUID.class))
                .deletedAt(source.get("deleted_at", LocalDateTime.class))
                .reqCompanyUUID(source.get("req_company_uuid", UUID.class))
                .reqBranchUUID(source.get("req_branch_uuid", UUID.class))
                .reqCreatedIP(source.get("req_created_ip", String.class))
                .reqCreatedPort(source.get("req_created_port", String.class))
                .reqCreatedBrowser(source.get("req_created_browser", String.class))
                .reqCreatedOS(source.get("req_created_os", String.class))
                .reqCreatedDevice(source.get("req_created_device", String.class))
                .reqCreatedReferer(source.get("req_created_referer", String.class))
                .reqUpdatedIP(source.get("req_updated_ip", String.class))
                .reqUpdatedPort(source.get("req_updated_port", String.class))
                .reqUpdatedBrowser(source.get("req_updated_browser", String.class))
                .reqUpdatedOS(source.get("req_updated_os", String.class))
                .reqUpdatedDevice(source.get("req_updated_device", String.class))
                .reqUpdatedReferer(source.get("req_updated_referer", String.class))
                .editable(source.get("editable", Boolean.class))
                .deletable(source.get("deletable", Boolean.class))
                .archived(source.get("archived", Boolean.class))
                .build();
    }
}
