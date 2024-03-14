package tuf.webscaf.app.dbContext.slave.repository.custom.mapper;

import io.r2dbc.spi.Row;
import tuf.webscaf.app.dbContext.slave.dto.SlaveChildParentAccountDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveParentAccountDto;

import java.util.UUID;
import java.util.function.BiFunction;

public class SlaveCustomParentToChildAccountMapper implements BiFunction<Row, Object, SlaveParentAccountDto> {

    @Override
    public SlaveParentAccountDto apply(Row source, Object o) {

        return SlaveParentAccountDto
                .builder()
                .name(source.get("name", String.class))
                .parentAccountUUID(source.get("uuid", UUID.class))
                .build();
    }
}
