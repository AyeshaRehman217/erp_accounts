package tuf.webscaf.app.dbContext.slave.repository.custom.mapper;

import io.r2dbc.spi.Row;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tuf.webscaf.app.dbContext.slave.dto.SlaveLedgerAccountDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveLedgerReportDto;

import java.time.LocalDateTime;
import java.util.function.BiFunction;

public class SlaveCustomLedgerReportDtoMapper implements BiFunction<Row, Object, SlaveLedgerReportDto> {
    MultiValueMap<String, SlaveLedgerAccountDto> accountDtoMap = new LinkedMultiValueMap<>();

    @Override
    public SlaveLedgerReportDto apply(Row source, Object o) {
        SlaveLedgerAccountDto slaveLedgerAccountDto = null;

        try {
            slaveLedgerAccountDto = SlaveLedgerAccountDto.builder()
                    .name(source.get("account", String.class))
                    .amount(source.get("ledgerAmount", String.class))
                    .description(source.get("ledgerDescription", String.class))
                    .build();
        } catch (IllegalArgumentException i) {

        }

        if (slaveLedgerAccountDto.getAmount() != null) {
            // if amount is negative value
            if (Double.parseDouble(slaveLedgerAccountDto.getAmount()) < 0) {
                slaveLedgerAccountDto.setAmount("(" + source.get("ledgerAmount", String.class).replace("-", "") + ")");
            }
        }


        String key = source.get("rowId", String.class);

        if (!key.isEmpty()) {
            accountDtoMap.add(key, slaveLedgerAccountDto);
        }


        SlaveLedgerReportDto ledgerReportDto = SlaveLedgerReportDto.builder()
                .rowId(key)
                .transactionUUID(source.get("transactionUUID", String.class))
                .date(source.get("transaction_date", LocalDateTime.class))
                .account(accountDtoMap.get(key))
                .description(source.get("description", String.class))
                .debit(source.get("dr_amount", String.class))
                .credit(source.get("cr_amount", String.class))
                .costCenter(source.get("costCenter", String.class))
                .profitCenter(source.get("profitCenter", String.class))
                .job(source.get("job", String.class))
                .netBalance(source.get("netBalance", String.class))
                .build();

        if (Double.parseDouble(ledgerReportDto.getDebit()) < 0) {
            ledgerReportDto.setDebit("(" + source.get("dr_amount", String.class).replace("-", "") + ")");
        }

        if (Double.parseDouble(ledgerReportDto.getCredit()) < 0) {
            ledgerReportDto.setCredit("(" + source.get("cr_amount", String.class).replace("-", "") + ")");
        }

        if (Double.parseDouble(ledgerReportDto.getNetBalance()) < 0) {
            ledgerReportDto.setNetBalance("(" + source.get("netBalance", String.class).replace("-", "") + ")");
        }

        return ledgerReportDto;
    }
}
