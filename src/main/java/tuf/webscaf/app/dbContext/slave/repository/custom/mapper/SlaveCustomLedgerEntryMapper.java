package tuf.webscaf.app.dbContext.slave.repository.custom.mapper;

import io.r2dbc.spi.Row;
import tuf.webscaf.app.dbContext.slave.dto.*;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.BiFunction;

public class SlaveCustomLedgerEntryMapper implements BiFunction<Row, Object, SlaveLedgerRowDto> {

    @Override
    public SlaveLedgerRowDto apply(Row source, Object o) {

        SlaveLedgerRowAccountDto accountDto = null;

        try {
            accountDto = SlaveLedgerRowAccountDto.builder()
                    .account_code(source.get("controlCode", String.class))
//                    .account_id(source.get("accountId", Long.class))
                    .uuid(source.get("accountUUID", UUID.class))
                    .account_name(source.get("accountName", String.class))
                    .build();
        } catch (IllegalArgumentException i) {
        }


        SlaveLedgerRowProfitCenterDto profitCenterDto = null;
        try {
            profitCenterDto = SlaveLedgerRowProfitCenterDto.builder()
//                        .id(source.get("profitId", Long.class))
                    .uuid(source.get("profitUUID", UUID.class))
                    .name(source.get("profitCenterName", String.class))
                    .build();
        } catch (IllegalArgumentException i) {
        }


        SlaveLedgerRowCostCenterDto costCenterDto = null;
        try {
            costCenterDto = SlaveLedgerRowCostCenterDto.builder()
//                        .id(source.get("costId", Long.class))
                    .uuid(source.get("costUUID", UUID.class))
                    .name(source.get("costCenterName", String.class))
                    .build();
        } catch (IllegalArgumentException i) {
        }


        return SlaveLedgerRowDto.builder()
                .account(accountDto)
                .profit_center(profitCenterDto)
                .cost_center(costCenterDto)
                .description(source.get("ledgerDescription", String.class))
                .cr(source.get("crAmount", Double.class))
                .dr(source.get("drAmount", Double.class))
                .build();
    }

}
