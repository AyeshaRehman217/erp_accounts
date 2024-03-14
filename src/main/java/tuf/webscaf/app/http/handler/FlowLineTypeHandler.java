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
import tuf.webscaf.app.dbContext.master.entity.CashFlowReportEntity;
import tuf.webscaf.app.dbContext.master.entity.FlowLineTypeEntity;
import tuf.webscaf.app.dbContext.master.repository.FlowLineTypeRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocumentNatureMetaFieldEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveFlowLineTypeEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveFlowLineTypeRepository;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.List;

@Component
@Tag(name = "flowLineTypeHandler")
public class FlowLineTypeHandler {
    @Autowired
    FlowLineTypeRepository flowLineTypeRepository;

    @Autowired
    SlaveFlowLineTypeRepository slaveFlowLineTypeRepository;

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
        Flux<SlaveFlowLineTypeEntity> flowLineTypeEntityFlux = slaveFlowLineTypeRepository
                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
        return flowLineTypeEntityFlux
                .collectList()
                .flatMap(FlowLineTypeDB -> slaveFlowLineTypeRepository
                        .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                        .flatMap(count ->
                        {
                            if (FlowLineTypeDB.isEmpty()) {
                                return responseIndexInfoMsg("Record does not exist", count);
                            } else {
                                return responseIndexSuccessMsg("All Records fetched successfully!", FlowLineTypeDB, count);
                            }
                        }));
    }

    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        final long branchId = Long.parseLong(serverRequest.pathVariable("id"));

        return serverRequest.formData()
                .flatMap(docEntity -> slaveFlowLineTypeRepository.findByIdAndDeletedAtIsNull(branchId)
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
                    FlowLineTypeEntity flowLineTypeEntity = FlowLineTypeEntity.builder()
                            .name(value.getFirst("name"))
                            .createdBy(UUID.fromString(userId))
                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                            .description(value.getFirst("description"))
                            .build();

                    return flowLineTypeRepository
                            .save(flowLineTypeEntity)
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
        final long Id = Long.parseLong(serverRequest.pathVariable("id"));
        String userId = serverRequest.headers().firstHeader("auid");

        if (userId == null) {
            return responseMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseMsg("Unknown user");
        }

        return serverRequest.formData()
                .flatMap(value -> flowLineTypeRepository.findByIdAndDeletedAtIsNull(Id)
                        .flatMap(entity -> {
                            entity.setName(value.getFirst("name"));
                            entity.setDescription(value.getFirst("description"));
//                            entity.setUpdatedBy(Long.valueOf(value.getFirst("updatedBy")));
                            entity.setUpdatedBy(UUID.fromString(userId));
                            entity.setUpdatedAt(LocalDateTime.now(ZoneId.of(zone)));
                            return flowLineTypeRepository.save(entity);
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

        final long flowLineReportId = Long.parseLong(serverRequest.pathVariable("id"));
        String userId = serverRequest.headers().firstHeader("auid");

        if (userId == null) {
            return responseMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseMsg("Unknown user");
        }

        return serverRequest.formData()
                .flatMap(value -> flowLineTypeRepository.findByIdAndDeletedAtIsNull(flowLineReportId)
                        .flatMap(flowLineEntity -> {
                            //                          flowLineEntity.setDeletedBy(Long.valueOf(value.getFirst("deletedBy")));
                            flowLineEntity.setDeletedBy(UUID.fromString(userId));
                            flowLineEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            return flowLineTypeRepository.save(flowLineEntity)
                                    .thenReturn(flowLineEntity);
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

            return flowLineTypeRepository.findByIdAndDeletedAtIsNull(id)
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
                                flowLineTypeRepository.save(val)
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
