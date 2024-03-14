package tuf.webscaf.app.dbContext.slave.repository.custom.mapper;

import io.r2dbc.spi.Row;
import tuf.webscaf.app.dbContext.slave.dto.SlaveChartOfAccountDto;

import java.util.UUID;
import java.util.function.BiFunction;

public class SlaveCustomChartOfAccountMapper implements BiFunction<Row, Object, SlaveChartOfAccountDto> {

    @Override
    public SlaveChartOfAccountDto apply(Row source, Object o) {

        SlaveChartOfAccountDto chartAccountBuilder = null;

//        if (source.get("parent_account_uuid") == null) {
            chartAccountBuilder = SlaveChartOfAccountDto
                    .builder()
                    .uuid(source.get("uuid", UUID.class))
                    .name(source.get("name", String.class))
                    .accountTypeUUID(source.get("account_type_uuid", UUID.class))
                    .accountTypeName(source.get("account_type_name", String.class))
                    .isEntryAllowed(source.get("is_entry_allowed", Boolean.class))
                    .level(source.get("level", Long.class))
                    .parentAccountUUID(source.get("parent_account_uuid", UUID.class))
                    .controlCode(source.get("control_code", String.class))
                    .build();
//        }
        return chartAccountBuilder;
    }
}
