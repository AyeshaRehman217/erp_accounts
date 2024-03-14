package tuf.webscaf.app.dbContext.slave.repository.custom.mapper;

import io.r2dbc.spi.Row;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tuf.webscaf.app.dbContext.slave.dto.SlaveProfitAndLossStatementDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountGroupEntity;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.BiFunction;

public class SlaveProfitAndLossStatementMapper implements BiFunction<Row, Object, SlaveProfitAndLossStatementDto> {


    @Override
    public SlaveProfitAndLossStatementDto apply(Row source, Object o) {

        return SlaveProfitAndLossStatementDto.builder()
                .accountUUID(source.get("account_uuid", UUID.class))
                .balanceBroughtForward(source.get("balanceBroughtForward", Double.class))
                .build();
    }
}
