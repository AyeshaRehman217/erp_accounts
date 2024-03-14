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
import tuf.webscaf.app.dbContext.master.entity.CalendarTypesEntity;
import tuf.webscaf.app.dbContext.master.entity.TransactionStatusEntity;
import tuf.webscaf.app.dbContext.master.repository.CalendarRepository;
import tuf.webscaf.app.dbContext.master.repository.CalendarTypesRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarTypeEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveCalendarTypesRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Tag(name = "calendarTypeHandler")
public class CalendarTypeHandler {

    @Autowired
    CalendarTypesRepository calendarTypesRepository;

    @Autowired
    SlaveCalendarTypesRepository slaveCalendarTypeRepository;

    @Autowired
    CalendarRepository calendarRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_calendar-type_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        if (!status.isEmpty()) {
            Flux<SlaveCalendarTypeEntity> slaveCalendarTypeEntityFlux = slaveCalendarTypeRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));

            return slaveCalendarTypeEntityFlux
                    .collectList()
                    .flatMap(calendarTypeEntityDB -> slaveCalendarTypeRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                        if (calendarTypeEntityDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records Fetched Successfully", calendarTypeEntityDB, count);
                                        }
                                    }
                            )
                    )
                    .switchIfEmpty(responseInfoMsg("Record does not exist."))
                    .onErrorResume(err -> responseErrorMsg("Record does not exist. Please Contact Developer."));
        } else {
            Flux<SlaveCalendarTypeEntity> slaveCalendarTypeEntityFlux = slaveCalendarTypeRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);

            return slaveCalendarTypeEntityFlux
                    .collectList()
                    .flatMap(calendarTypeEntityDB -> slaveCalendarTypeRepository
                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                        if (calendarTypeEntityDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records Fetched Successfully", calendarTypeEntityDB, count);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Record does not exist."))
                    .onErrorResume(err -> responseErrorMsg("Record does not exist. Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_calendar-type_active_index")
    public Mono<ServerResponse> indexWithActiveStatus(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        Flux<SlaveCalendarTypeEntity> slaveCalendarTypeEntityFlux = slaveCalendarTypeRepository
                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE);

        return slaveCalendarTypeEntityFlux
                .collectList()
                .flatMap(calendarTypeEntityDB -> slaveCalendarTypeRepository
                        .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE)
                        .flatMap(count -> {
                                    if (calendarTypeEntityDB.isEmpty()) {
                                        return responseIndexInfoMsg("Record does not exist", count);

                                    } else {

                                        return responseIndexSuccessMsg("All Records Fetched Successfully", calendarTypeEntityDB, count);
                                    }
                                }
                        )
                )
                .switchIfEmpty(responseInfoMsg("Record does not exist."))
                .onErrorResume(err -> responseErrorMsg("Record does not exist. Please Contact Developer."));

    }

    @AuthHasPermission(value = "account_api_v1_calendar-type_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID calendarTypeUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveCalendarTypeRepository.findByUuidAndDeletedAtIsNull(calendarTypeUUID)
                .flatMap(value1 -> responseSuccessMsg("Record fetched successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist."))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_calendar-type_store")
    public Mono<ServerResponse> store(ServerRequest serverRequest) {
        String userId = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userId == null) {
            return responseErrorMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseErrorMsg("Unknown user");

        }
        return serverRequest.formData()
                .flatMap(value -> {

                            CalendarTypesEntity calendarTypesEntity = CalendarTypesEntity.builder()
                                    .uuid(UUID.randomUUID())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .periods(Integer.valueOf(value.getFirst("periods")))
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdBy(UUID.fromString(userId))
                                    .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqCreatedIP(reqIp)
                                    .reqCreatedPort(reqPort)
                                    .reqCreatedBrowser(reqBrowser)
                                    .reqCreatedOS(reqOs)
                                    .reqCreatedDevice(reqDevice)
                                    .reqCreatedReferer(reqReferer)
                                    .build();

                            return calendarTypesRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(calendarTypesEntity.getName())
                                    .flatMap(nameExists -> responseInfoMsg("Calendar Type Already Exists With the same Name"))
                                    .switchIfEmpty(Mono.defer(() -> calendarTypesRepository.save(calendarTypesEntity)
                                            .flatMap(entity -> responseSuccessMsg("Record Stored Successfully", entity))
                                            .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please try again"))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Store Record. Please Contact Developer."))
                                    ));
                        }
                ).switchIfEmpty(responseInfoMsg("Unable to read Request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read Request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_calendar-type_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID calendarTypeUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());
        String userId = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userId == null) {
            return responseErrorMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseErrorMsg("Unknown user");

        }

        return serverRequest.formData()
                .flatMap(value -> calendarTypesRepository.findByUuidAndDeletedAtIsNull(calendarTypeUUID)
                        .flatMap(previousCalendarType -> {

                            CalendarTypesEntity calendarTypesEntity = CalendarTypesEntity.builder()
                                    .uuid(previousCalendarType.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .periods(Integer.valueOf(value.getFirst("periods")))
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdBy(previousCalendarType.getCreatedBy())
                                    .createdAt(previousCalendarType.getCreatedAt())
                                    .updatedBy(UUID.fromString(userId))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousCalendarType.getReqCreatedIP())
                                    .reqCreatedPort(previousCalendarType.getReqCreatedPort())
                                    .reqCreatedBrowser(previousCalendarType.getReqCreatedBrowser())
                                    .reqCreatedOS(previousCalendarType.getReqCreatedOS())
                                    .reqCreatedDevice(previousCalendarType.getReqCreatedDevice())
                                    .reqCreatedReferer(previousCalendarType.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousCalendarType.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousCalendarType.setDeletedBy(UUID.fromString(userId));
                            previousCalendarType.setReqDeletedIP(reqIp);
                            previousCalendarType.setReqDeletedPort(reqPort);
                            previousCalendarType.setReqDeletedBrowser(reqBrowser);
                            previousCalendarType.setReqDeletedOS(reqOs);
                            previousCalendarType.setReqDeletedDevice(reqDevice);
                            previousCalendarType.setReqDeletedReferer(reqReferer);

                            return calendarTypesRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(calendarTypesEntity.getName(), calendarTypeUUID)
                                    //Check if name exist
                                    .flatMap(entity -> responseInfoMsg("Record Already Exists with the same Name"))
                                    //Check if name not exist
                                    .switchIfEmpty(Mono.defer(() -> calendarTypesRepository.save(previousCalendarType)
                                            .flatMap(entity -> calendarTypesRepository.save(calendarTypesEntity)
                                                    .flatMap(calendarTypeDB -> responseSuccessMsg("Record Updated Successfully", calendarTypeDB))
                                                    .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong.Please try again!"))
                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))
                                            )
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Calendar Type Does not Exist."))
                        .onErrorResume(ex -> responseErrorMsg("Calendar Type Does not Exist.Please Contact Developer."))
                )
                .switchIfEmpty(responseInfoMsg("Unable to read Request. "))
                .onErrorResume(err -> responseErrorMsg("Unable to read Request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_calendar-type_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID calendarTypeUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());
        String userId = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userId == null) {
            return responseErrorMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseErrorMsg("Unknown user");

        }

        return calendarTypesRepository.findByUuidAndDeletedAtIsNull(calendarTypeUUID)
                .flatMap(calendarTypeEntity -> calendarRepository.findFirstByCalendarTypeUUIDAndDeletedAtIsNull(calendarTypeUUID)
                        .flatMap(value2 -> responseInfoMsg("Unable to Delete Record as the Reference exists"))
                        .switchIfEmpty(Mono.defer(() -> {
                            calendarTypeEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            calendarTypeEntity.setDeletedBy(UUID.fromString(userId));
                            calendarTypeEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            calendarTypeEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            calendarTypeEntity.setReqDeletedIP(reqIp);
                            calendarTypeEntity.setReqDeletedPort(reqPort);
                            calendarTypeEntity.setReqDeletedBrowser(reqBrowser);
                            calendarTypeEntity.setReqDeletedOS(reqOs);
                            calendarTypeEntity.setReqDeletedDevice(reqDevice);
                            calendarTypeEntity.setReqDeletedReferer(reqReferer);

                            return calendarTypesRepository.save(calendarTypeEntity)
                                    .flatMap(entity -> responseSuccessMsg("Record deleted successfully", entity))
                                    .switchIfEmpty(responseInfoMsg("Unable to Delete Record.There is something wrong please try again"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Delete Record. Please Contact Developer."));
                        }))
                ).onErrorResume(err -> responseInfoMsg("Record does not exist"))
                .switchIfEmpty(responseErrorMsg("Record does not exist. Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_calendar-type_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        final UUID calendarTypeUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

        String userId = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userId == null) {
            return responseWarningMsg("Unknown User");
        } else {
            if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                return responseWarningMsg("Unknown User!");
            }
        }
        return serverRequest.formData()
                .flatMap(value -> {
                    boolean status = Boolean.parseBoolean(value.getFirst("status"));
                    return calendarTypesRepository.findByUuidAndDeletedAtIsNull(calendarTypeUUID)
                            .flatMap(previousCalendarType -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousCalendarType.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                CalendarTypesEntity calendarTypesEntity = CalendarTypesEntity.builder()
                                        .uuid(previousCalendarType.getUuid())
                                        .name(previousCalendarType.getName())
                                        .description(previousCalendarType.getDescription())
                                        .periods(previousCalendarType.getPeriods())
                                        .status(status == true ? true : false)
                                        .createdBy(previousCalendarType.getCreatedBy())
                                        .createdAt(previousCalendarType.getCreatedAt())
                                        .updatedBy(UUID.fromString(userId))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousCalendarType.getReqCreatedIP())
                                        .reqCreatedPort(previousCalendarType.getReqCreatedPort())
                                        .reqCreatedBrowser(previousCalendarType.getReqCreatedBrowser())
                                        .reqCreatedOS(previousCalendarType.getReqCreatedOS())
                                        .reqCreatedDevice(previousCalendarType.getReqCreatedDevice())
                                        .reqCreatedReferer(previousCalendarType.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousCalendarType.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousCalendarType.setDeletedBy(UUID.fromString(userId));
                                previousCalendarType.setReqDeletedIP(reqIp);
                                previousCalendarType.setReqDeletedPort(reqPort);
                                previousCalendarType.setReqDeletedBrowser(reqBrowser);
                                previousCalendarType.setReqDeletedOS(reqOs);
                                previousCalendarType.setReqDeletedDevice(reqDevice);
                                previousCalendarType.setReqDeletedReferer(reqReferer);

                                return calendarTypesRepository.save(previousCalendarType)
                                        .then(calendarTypesRepository.save(calendarTypesEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status updated successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status"))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status.There is something wrong please try again."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Record does not exist.Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please contact developer."));
    }

    public Mono<ServerResponse> responseErrorMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.ERROR,
                        msg
                )
        );

        return appresponse.set(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.empty()
        );
    }

    public Mono<ServerResponse> responseInfoMsg(String msg) {
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

    public Mono<ServerResponse> responseSuccessMsg(String msg, Object entity) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.SUCCESS,
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
                Mono.just(entity)
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

    public Mono<ServerResponse> responseWarningMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.WARNING,
                        msg)
        );


        return appresponse.set(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                HttpStatus.UNPROCESSABLE_ENTITY.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.empty()
        );
    }

}