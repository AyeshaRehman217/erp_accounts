package tuf.webscaf.app.dbContext.slave.repository.custom.mapper;

import io.r2dbc.spi.Row;
import tuf.webscaf.app.dbContext.slave.dto.SlaveChildParentAccountDto;

import java.util.UUID;
import java.util.function.BiFunction;

public class SlaveCustomChildToParentAccountMapper implements BiFunction<Row, Object, SlaveChildParentAccountDto> {

    @Override
    public SlaveChildParentAccountDto apply(Row source, Object o) {

        return SlaveChildParentAccountDto
                .builder()
                .code(source.get("code", String.class))
                .parentAccountUUID(source.get("parent_account_uuid", UUID.class))
                .build();
    }
}
