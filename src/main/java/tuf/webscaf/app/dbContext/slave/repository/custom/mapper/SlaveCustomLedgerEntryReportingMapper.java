package tuf.webscaf.app.dbContext.slave.repository.custom.mapper;

import io.r2dbc.spi.Row;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tuf.webscaf.app.dbContext.slave.dto.*;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountGroupEntity;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class SlaveCustomLedgerEntryReportingMapper implements BiFunction<Row, Object, SlaveReportingLedgerEntriesDto> {

    @Override
    public SlaveReportingLedgerEntriesDto apply(Row source, Object o) {
        SlaveLedgerRowAccountDto accountDto = null;

        try {
            accountDto = SlaveLedgerRowAccountDto.builder()
                    .account_code(source.get("accountCode", String.class))
//                    .account_id(source.get("accountId", Long.class))
                    .uuid(source.get("accountUUID", UUID.class))
                    .account_name(source.get("accountName", String.class))
                    .build();
        } catch (IllegalArgumentException i) {
        }


        SlaveLedgerRowProfitCenterDto profitCenterDto = null;
        if (source.get("profit_center_uuid", UUID.class) != null) {
            try {
                profitCenterDto = SlaveLedgerRowProfitCenterDto.builder()
//                        .id(source.get("profitId", Long.class))
                        .uuid(source.get("profitUUID", UUID.class))
                        .name(source.get("profitCenterName", String.class))
                        .build();
            } catch (IllegalArgumentException i) {
            }
        }


        SlaveLedgerRowCostCenterDto costCenterDto = null;
        if (source.get("cost_center_uuid", UUID.class) != null) {
            try {
                costCenterDto = SlaveLedgerRowCostCenterDto.builder()
                        .uuid(source.get("costUUID", UUID.class))
                        .name(source.get("costCenterName", String.class))
                        .build();
            } catch (IllegalArgumentException i) {
            }
        }

        SlaveLedgerRowDto slaveLedgerRowDto = SlaveLedgerRowDto
                .builder()
                .account(accountDto)
                .profit_center(profitCenterDto)
                .cost_center(costCenterDto)
                .description(source.get("description", String.class))
                .cr(source.get("cr_amount", Double.class))
                .dr(source.get("dr_amount", Double.class))
                .build();

        List<SlaveLedgerRowDto> ledgerRow = new ArrayList<>();

        if (!(slaveLedgerRowDto == null)) {
            ledgerRow.add(slaveLedgerRowDto);
        }

        return SlaveReportingLedgerEntriesDto
                .builder()
                .ledgerEntryList(ledgerRow)
                .transactionDate(source.get("transactionDate", LocalDateTime.class))
                .build();
    }
}
