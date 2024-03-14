package tuf.webscaf.app.dbContext.slave.repository.custom.mapper;

import io.r2dbc.spi.Row;

import tuf.webscaf.app.dbContext.master.dto.VoucherDto;
import tuf.webscaf.app.dbContext.master.dto.VoucherTypeDto;
import tuf.webscaf.app.dbContext.slave.dto.*;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.BiFunction;

public class SlaveCustomTransactionMapper implements BiFunction<Row, Object, SlaveTransactionDto> {

    @Override
    public SlaveTransactionDto apply(Row source, Object o) {

        SlaveTransactionStatusDto statusDto = null;
        try {
            statusDto = SlaveTransactionStatusDto.builder()
                    .uuid(source.get("transStatusUUID", UUID.class))
                    .name(source.get("transactionName", String.class))
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

        SlaveTransactionDataJobCenterDto jobs = null;
        try {
            jobs = SlaveTransactionDataJobCenterDto.builder()
                    .uuid(source.get("jobUUID", UUID.class))
                    .name(source.get("jobCenterName", String.class))
                    .build();
        } catch (IllegalArgumentException i) {
        }


        return SlaveTransactionDto.builder()
                .id(source.get("id", Long.class))
                .transaction_id(source.get("uuid", UUID.class))
                .transaction_status(statusDto)
                .voucher(voucherDto)
                .calendar_period_uuid(source.get("calendar_period_uuid", UUID.class))
                .date(source.get("transaction_date", LocalDateTime.class))
                .company_uuid(source.get("company_uuid", UUID.class))
                .branch_uuid(source.get("branch_uuid", UUID.class))
                .transaction_description(source.get("description", String.class))
                .job_center(jobs)
                .debit(source.get("debit", Double.class))
                .credit(source.get("credit", Double.class))
                .build();


    }

}
