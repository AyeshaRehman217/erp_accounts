package tuf.webscaf.app.dbContext.slave.repository.custom.mapper;

import io.r2dbc.spi.Row;
import tuf.webscaf.app.dbContext.slave.dto.SlaveTrialBalanceReportDto;

import java.util.UUID;
import java.util.function.BiFunction;

public class SlaveCustomTrialBalanceReportMapper implements BiFunction<Row, Object, SlaveTrialBalanceReportDto> {

    @Override
    public SlaveTrialBalanceReportDto apply(Row source, Object o) {

        SlaveTrialBalanceReportDto trialBalanceReportDto = SlaveTrialBalanceReportDto
                .builder()
                .uuid(source.get("uuid", UUID.class))
                .parentAccountUUID(source.get("parent_account_uuid", UUID.class))
                .accountCode(source.get("control_code", String.class))
                .level(source.get("level", String.class))
                .accountName(source.get("name", String.class))
                .debit(source.get("debit", String.class))
                .credit(source.get("credit", String.class))
                .build();

        if (Double.parseDouble(trialBalanceReportDto.getDebit()) < 0) {
            trialBalanceReportDto.setDebit("(" + source.get("debit", String.class).replace("-", "") + ")");
        }

        if (Double.parseDouble(trialBalanceReportDto.getCredit()) < 0) {
            trialBalanceReportDto.setCredit("(" + source.get("credit", String.class).replace("-", "") + ")");
        }

        return trialBalanceReportDto;

    }
}
