package tuf.webscaf.app.http.handler;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.IncomeSummaryDetailEntity;
import tuf.webscaf.app.dbContext.master.repository.IncomeSummaryDetailRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveIncomeSummaryDetailEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveIncomeSummaryDetailRepository;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.List;

@Component
@Tag(name = "incomeSummaryDetailHandler")
public class IncomeSummaryDetailHandler {
    @Autowired
    IncomeSummaryDetailRepository incomeSummaryDetailRepository;

    @Autowired
    SlaveIncomeSummaryDetailRepository slaveIncomeSummaryDetailRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");
        Sort.Direction direction;
        switch (d.toLowerCase()) {
            case "asc":
                direction = Sort.Direction.ASC;
                break;
            case "desc":
                direction = Sort.Direction.DESC;
                break;
            default:
                direction = Sort.Direction.ASC;
        }

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("id");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
        Flux<SlaveIncomeSummaryDetailEntity> incomeSummaryDetailEntityFlux = slaveIncomeSummaryDetailRepository
                .findAllByDeletedAtIsNull(pageable, searchKeyWord);
        return incomeSummaryDetailEntityFlux
                .collectList()
                .flatMap(incomeSummaryDB -> slaveIncomeSummaryDetailRepository
                        .countByDeletedAtIsNull(searchKeyWord)
                        .flatMap(count ->
                        {
                            if (incomeSummaryDB.isEmpty()) {
                                return responseIndexInfoMsg("Record does not exist", count);
                            } else {
                                return responseIndexSuccessMsg("All Records fetched successfully!", incomeSummaryDB, count);
                            }
                        }));
    }

    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        final long incomeSummaryDetailId = Long.parseLong(serverRequest.pathVariable("id"));

        return serverRequest.formData()
                .flatMap(incomeSummaryDetailEntity -> slaveIncomeSummaryDetailRepository.findByIdAndDeletedAtIsNull(incomeSummaryDetailId)
                        .flatMap(value1 -> {
                            var messages = List.of(
                                    new AppResponseMessage(
                                            AppResponse.Response.SUCCESS,
                                            "Record fetched successfully!"
                                    )
                            );
                            return appresponse.set(
                                    HttpStatus.OK.value(),
                                    HttpStatus.OK.name(),
                                    null,
                                    "eng",
                                    "token",
                                    0L,
                                    0L,
                                    messages,
                                    Mono.just(value1)
                            );
                        })
                        .switchIfEmpty(Mono.defer(() -> responseMsg("Record does not exist"))));
    }

    public Mono<ServerResponse> store(ServerRequest serverRequest) {
        String userId = serverRequest.headers().firstHeader("auid");

        if (userId == null) {
            return responseMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseMsg("Unknown user");
        }

        return serverRequest.formData()
                .flatMap(value -> {
                    IncomeSummaryDetailEntity incomeSummaryDetailEntity = IncomeSummaryDetailEntity.builder()
                            .accountId(Long.valueOf(value.getFirst("accountId")))
                            .incomeSummaryId(Long.valueOf(value.getFirst("incomeSummaryId")))
                            .crAmount(Long.valueOf(value.getFirst("crAmount")))
                            .drAmount(Long.valueOf(value.getFirst("drAmount")))
                            .createdBy(UUID.fromString(userId))
                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                            .build();

                    return incomeSummaryDetailRepository
                            .save(incomeSummaryDetailEntity)
                            .flatMap(value1 -> {
                                var messages = List.of(
                                        new AppResponseMessage(
                                                AppResponse.Response.SUCCESS,
                                                "Record stored successfully!"
                                        )
                                );
                                return appresponse.set(
                                        HttpStatus.OK.value(),
                                        HttpStatus.OK.name(),
                                        null,
                                        "eng",
                                        "token",
                                        0L,
                                        0L,
                                        messages,
                                        Mono.just(value1)
                                );
                            })
                            .switchIfEmpty(Mono.defer(() -> responseMsg("There is something wrong. Please Try Again!")));
                });
    }

    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        final long incomeSummaryDetailId = Long.parseLong(serverRequest.pathVariable("id"));
        String userId = serverRequest.headers().firstHeader("auid");

        if (userId == null) {
            return responseMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseMsg("Unknown user");
        }

        return serverRequest.formData()
                .flatMap(incomeSummaryDetailEntity -> incomeSummaryDetailRepository.findByIdAndDeletedAtIsNull(incomeSummaryDetailId)
                        .flatMap(incomeDetailDB -> {
                            incomeDetailDB.setIncomeSummaryId(Long.valueOf(incomeSummaryDetailEntity.getFirst("incomeSummaryId")));
                            incomeDetailDB.setAccountId(Long.valueOf(incomeSummaryDetailEntity.getFirst("accountId")));
                            incomeDetailDB.setDrAmount(Long.valueOf(incomeSummaryDetailEntity.getFirst("drAmount")));
                            incomeDetailDB.setCrAmount(Long.valueOf(incomeSummaryDetailEntity.getFirst("crAmount")));
//                                incomeDetailDB.setUpdatedBy(Long.valueOf(incomeSummaryDetailEntity.getFirst("updatedBy")));
                            incomeDetailDB.setUpdatedBy(UUID.fromString(userId));
                            incomeDetailDB.setUpdatedAt(LocalDateTime.now(ZoneId.of(zone)));
                            return incomeSummaryDetailRepository.save(incomeDetailDB);
                        })
                        .flatMap(value1 -> {
                            var messages = List.of(
                                    new AppResponseMessage(
                                            AppResponse.Response.SUCCESS,
                                            "Record updated successfully!"
                                    )
                            );
                            return appresponse.set(
                                    HttpStatus.OK.value(),
                                    HttpStatus.OK.name(),
                                    null,
                                    "eng",
                                    "token",
                                    0L,
                                    0L,
                                    messages,
                                    Mono.just(value1)
                            );

                        })
                        .switchIfEmpty(Mono.defer(() -> responseMsg("There is something wrong. Please Try Again!"))));
    }

    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        final long incomeDetailId = Long.parseLong(serverRequest.pathVariable("id"));
        String userId = serverRequest.headers().firstHeader("auid");

        if (userId == null) {
            return responseMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseMsg("Unknown user");
        }


        return serverRequest.formData()
                .flatMap(value -> incomeSummaryDetailRepository.findByIdAndDeletedAtIsNull(incomeDetailId)
                        .flatMap(incomeSummaryDetailEntity -> {
//                          incomeSummaryDetailEntity.setDeletedBy(Long.valueOf(value.getFirst("deletedBy")));
                            incomeSummaryDetailEntity.setDeletedBy(UUID.fromString(userId));
                            incomeSummaryDetailEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            return incomeSummaryDetailRepository.save(incomeSummaryDetailEntity)
                                    .thenReturn(incomeSummaryDetailEntity);
                        }).flatMap(value1 -> {
                            var messages = List.of(
                                    new AppResponseMessage(
                                            AppResponse.Response.SUCCESS,
                                            "Record deleted successfully!"
                                    )
                            );
                            return appresponse.set(
                                    HttpStatus.OK.value(),
                                    HttpStatus.OK.name(),
                                    null,
                                    "eng",
                                    "token",
                                    0L,
                                    0L,
                                    messages,
                                    Mono.just(value1)
                            );
                        })
                        .switchIfEmpty(Mono.defer(() -> responseMsg("Record does not exist"))));
    }

    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        final long id = Long.parseLong(serverRequest.pathVariable("id"));
        Mono<MultiValueMap<String, String>> formData = serverRequest.formData();

        return formData.flatMap(value -> {
            Boolean status = Boolean.parseBoolean(value.getFirst("status"));

            return incomeSummaryDetailRepository.findByIdAndDeletedAtIsNull(id)
                    .flatMap(val -> {
                        // If status is not boolean vale
                        if (status != false && status != true) {
                            var messages = List.of(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Status must be 0 or 1"
                                    )
                            );

                            return appresponse.set(
                                    HttpStatus.OK.value(),
                                    HttpStatus.OK.name(),
                                    null,
                                    "eng",
                                    "token",
                                    0L,
                                    0L,
                                    messages,
                                    Mono.empty()
                            );
                        }

                        // If already same status exist in database.
                        if (((val.getStatus() ? true : false) == status)) {

                            var messages = List.of(
                                    new AppResponseMessage(
                                            AppResponse.Response.WARNING,
                                            "Record already exist with same status"
                                    )
                            );

                            return appresponse.set(
                                    HttpStatus.OK.value(),
                                    HttpStatus.OK.name(),
                                    null,
                                    "eng",
                                    "token",
                                    0L,
                                    0L,
                                    messages,
                                    Mono.just(val)
                            );
                        }

                        // Need to update
                        val.setStatus(status == true ? true : false);
                        var messages = List.of(
                                new AppResponseMessage(
                                        AppResponse.Response.SUCCESS,
                                        "Status changed successfully"
                                )
                        );

                        return appresponse.set(
                                HttpStatus.OK.value(),
                                HttpStatus.OK.name(),
                                null,
                                "eng",
                                "token",
                                0L,
                                0L,
                                messages,
                                incomeSummaryDetailRepository.save(val)
                        );
                    });
        });
    }

    public Mono<ServerResponse> responseMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.INFO,
                        msg
                )
        );

        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.empty()
        );
    }

    public Mono<ServerResponse> responseIndexInfoMsg(String msg, Long totalDataRowsWithFilter) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.INFO,
                        msg
                )
        );


        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                totalDataRowsWithFilter,
                0L,
                messages,
                Mono.empty()

        );
    }

    public Mono<ServerResponse> responseIndexSuccessMsg(String msg, Object entity, Long totalDataRowsWithFilter) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.SUCCESS,
                        msg)
        );

        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                totalDataRowsWithFilter,
                0L,
                messages,
                Mono.just(entity)
        );
    }

}
