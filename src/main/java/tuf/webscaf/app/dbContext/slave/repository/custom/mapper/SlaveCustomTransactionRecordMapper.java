package tuf.webscaf.app.dbContext.slave.repository.custom.mapper;

import io.r2dbc.spi.Row;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tuf.webscaf.app.dbContext.master.dto.VoucherDto;
import tuf.webscaf.app.dbContext.master.dto.VoucherTypeDto;
import tuf.webscaf.app.dbContext.slave.dto.*;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTransactionEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherEntity;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public class SlaveCustomTransactionRecordMapper implements BiFunction<Row, Object, SlaveTransactionRecordDto> {

    MultiValueMap<UUID, SlaveLedgerRowDto> ledgerRowDtoMap = new LinkedMultiValueMap<>();

    @Override
    public SlaveTransactionRecordDto apply(Row source, Object o) {

        SlaveLedgerRowAccountDto accountDto = null;
        try {
            accountDto = SlaveLedgerRowAccountDto
                    .builder()
                    .account_code(source.get("controlCode", String.class))
                    .uuid(source.get("accUUID", UUID.class))
                    .account_name(source.get("accountName", String.class))
                    .build();
        } catch (IllegalArgumentException i) {
        }


        SlaveLedgerRowProfitCenterDto profitCenterDto = null;
        try {
            profitCenterDto = SlaveLedgerRowProfitCenterDto
                    .builder()
                    .uuid(source.get("profitUUID", UUID.class))
                    .name(source.get("profitCenterName", String.class))
                    .build();
        } catch (IllegalArgumentException i) {
        }


        SlaveLedgerRowCostCenterDto costCenterDto = null;
        try {
            costCenterDto = SlaveLedgerRowCostCenterDto
                    .builder()
                    .uuid(source.get("costUUID", UUID.class))
                    .name(source.get("costCenterName", String.class))
                    .build();
        } catch (IllegalArgumentException i) {
        }

        SlaveTransactionDataJobCenterDto jobCenter = null;
        try {
            jobCenter = SlaveTransactionDataJobCenterDto
                    .builder()
                    .uuid(source.get("jobUUID", UUID.class))
                    .name(source.get("jobCenterName", String.class))
                    .build();
        } catch (IllegalArgumentException i) {
        }

        SlaveTransactionStatusDto statusDto = null;
        try {
            statusDto = SlaveTransactionStatusDto
                    .builder()
                    .uuid(source.get("transactionStatusUUID", UUID.class))
                    .name(source.get("transactionStatusName", String.class))
                    .build();
        } catch (IllegalArgumentException i) {
        }

        VoucherTypeDto voucherType = null;
        try {
            voucherType = VoucherTypeDto.builder()
                    .uuid(source.get("voucherTypeUUID", UUID.class))
                    .name(source.get("voucherTypeName", String.class))
                    .slug(source.get("voucherTypeSlug", String.class))
                    .build();
        } catch (IllegalArgumentException i) {
        }

        VoucherDto voucherDto = null;
        try {
            voucherDto = VoucherDto.builder()
                    .uuid(source.get("voucherUUID", UUID.class))
                    .name(source.get("voucherName", String.class))
                    .voucherType(voucherType)
                    .build();
        } catch (IllegalArgumentException i) {
        }

        SlaveTransactionEntity slaveTransactionEntity = null;
        try {
            slaveTransactionEntity = SlaveTransactionEntity.builder()
                    .id(source.get("id", Long.class))
                    .uuid(source.get("uuid", UUID.class))
                    .voucherUUID(source.get("voucher_uuid", UUID.class))
                    .build();
        } catch (IllegalArgumentException i) {
        }


        SlaveLedgerRowDto slaveLedgerRowDto = SlaveLedgerRowDto.builder()
                .account(accountDto)
                .description(source.get("ledgerDescription", String.class))
                .profit_center(profitCenterDto)
                .cost_center(costCenterDto)
                .cr(source.get("crAmount", Double.class))
                .dr(source.get("drAmount", Double.class))
                .build();
        List<SlaveLedgerRowDto> ledgerRowsList = new LinkedList<>();
        if (slaveTransactionEntity != null) {
            ledgerRowDtoMap.add(slaveTransactionEntity.getUuid(), slaveLedgerRowDto);
            ledgerRowsList.addAll(ledgerRowDtoMap.get(slaveTransactionEntity.getUuid()));
        }


        SlaveTransactionDataDto slaveTransactionDataDto = SlaveTransactionDataDto.builder()
                .calendar_period_uuid(source.get("calendar_period_uuid", UUID.class))
                .date(source.get("transaction_date", LocalDateTime.class))
                .company_uuid(source.get("company_uuid", UUID.class))
                .branch_uuid(source.get("branch_uuid", UUID.class))
                .transaction_description(source.get("description", String.class))
                .job_center(jobCenter)
                .rows(ledgerRowsList)
                .debit(source.get("debit", Double.class))
                .credit(source.get("credit", Double.class))
                .build();

        return SlaveTransactionRecordDto.builder()
                .id(source.get("id", Long.class))
                .transaction_id(source.get("uuid", UUID.class))
                .transaction_data(slaveTransactionDataDto)
                .voucher(voucherDto)
                .transaction_status(statusDto)
                .build();
    }

}
