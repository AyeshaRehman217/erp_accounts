package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.SlaveVoucherDto;

import java.util.UUID;

@Repository
public interface SlaveCustomVoucherRepository {

    Flux<SlaveVoucherDto> showAllVoucherRecords(String name, String description, String dp, String d, Integer size, Long page);

    Flux<SlaveVoucherDto> showAllVoucherRecordsWithStatus(Boolean status, String name, String description, String dp, String d, Integer size, Long page);

    Mono<SlaveVoucherDto> showVoucherWithUUID(UUID voucherUUID);
}
