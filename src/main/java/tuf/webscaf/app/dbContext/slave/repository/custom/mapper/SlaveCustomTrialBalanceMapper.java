package tuf.webscaf.app.dbContext.slave.repository.custom.mapper;

import io.r2dbc.spi.Row;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tuf.webscaf.app.dbContext.slave.dto.SlaveTrialBalanceDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountGroupEntity;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.BiFunction;

public class SlaveCustomTrialBalanceMapper implements BiFunction<Row, Object, SlaveTrialBalanceDto> {

    @Override
    public SlaveTrialBalanceDto apply(Row source, Object o) {

        return SlaveTrialBalanceDto
                .builder()
                .accountUUID(source.get("accountUUID", UUID.class))
                .accountTypeUUID(source.get("accountTypeUUID", UUID.class))
                .accountTypeName(source.get("accountTypeName", String.class))
                .accountName(source.get("accountName", String.class))
                .accountCode(source.get("accountCode", String.class))
                .debit(source.get("debitAmount", Double.class))
                .credit(source.get("creditAmount", Double.class))
                .netBalance(source.get("netBalance", Double.class))
                .balanceBroughtForward(source.get("balanceBroughtForward", Double.class))
                .build();

    }
}
