package tuf.webscaf.app.http.handler;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CalendarEntity;

import tuf.webscaf.app.dbContext.master.repository.CalendarGroupCalendarPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.CalendarPeriodsRepository;
import tuf.webscaf.app.dbContext.master.repository.CalendarRepository;
import tuf.webscaf.app.dbContext.master.repository.CalendarTypesRepository;
import tuf.webscaf.app.dbContext.slave.dto.SlaveCalendarCalendarPeriodDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveCalendarRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherCalendarGroupPvtRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Tag(name = "calendarHandler")
public class CalendarHandler {

    @Autowired
    CalendarRepository calendarRepository;

    @Autowired
    CalendarTypesRepository calendarTypesRepository;

    @Autowired
    CalendarPeriodsRepository calendarPeriodsRepository;

    @Autowired
    SlaveCalendarRepository slaveCalendarRepository;

    @Autowired
    SlaveVoucherCalendarGroupPvtRepository slaveVoucherCalendarGroupPvtRepository;

    @Autowired
    CalendarGroupCalendarPvtRepository calendarGroupCalendarPvtRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.erp_config_module.uri}")
    private String configUri;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_calendar_index")
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
            Flux<SlaveCalendarEntity> slaveCalendarEntityFlux = slaveCalendarRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));
            return slaveCalendarEntityFlux
                    .collectList()
                    .flatMap(calendarEntityDB -> slaveCalendarRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                        if (calendarEntityDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records Fetched Successfully", calendarEntityDB, count);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        } else {
            Flux<SlaveCalendarEntity> slaveCalendarEntityFlux = slaveCalendarRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
            return slaveCalendarEntityFlux
                    .collectList()
                    .flatMap(calendarEntityDB -> slaveCalendarRepository
                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                        if (calendarEntityDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records Fetched Successfully", calendarEntityDB, count);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_calendar_active_index")
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


        Flux<SlaveCalendarEntity> slaveCalendarEntityFlux = slaveCalendarRepository
                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE);
        return slaveCalendarEntityFlux
                .collectList()
                .flatMap(calendarEntityDB -> slaveCalendarRepository
                        .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE)
                        .flatMap(count -> {
                                    if (calendarEntityDB.isEmpty()) {
                                        return responseIndexInfoMsg("Record does not exist", count);

                                    } else {

                                        return responseIndexSuccessMsg("All Records Fetched Successfully", calendarEntityDB, count);
                                    }
                                }
                        )
                ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));

    }

    @AuthHasPermission(value = "account_api_v1_calendar_calendar-type_index")
    public Mono<ServerResponse> indexWithCalendarType(ServerRequest serverRequest) {

        UUID calendarTypeUUID = UUID.fromString(serverRequest.pathVariable("calendarTypeUUID"));

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


        Flux<SlaveCalendarEntity> slaveCalendarEntityFlux = slaveCalendarRepository
                .findAllByNameContainingIgnoreCaseAndCalendarTypeUUIDAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCalendarTypeUUIDAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, calendarTypeUUID, Boolean.TRUE, searchKeyWord, calendarTypeUUID, Boolean.TRUE);
        return slaveCalendarEntityFlux
                .collectList()
                .flatMap(calendarEntityDB -> slaveCalendarRepository
                        .countByNameContainingIgnoreCaseAndCalendarTypeUUIDAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCalendarTypeUUIDAndStatusAndDeletedAtIsNull(searchKeyWord, calendarTypeUUID, Boolean.TRUE, searchKeyWord, calendarTypeUUID, Boolean.TRUE)
                        .flatMap(count -> {
                                    if (calendarEntityDB.isEmpty()) {
                                        return responseIndexInfoMsg("Record does not exist", count);

                                    } else {

                                        return responseIndexSuccessMsg("All Records Fetched Successfully", calendarEntityDB, count);
                                    }
                                }
                        )
                ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));

    }

    @AuthHasPermission(value = "account_api_v1_calendar_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID calendarUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveCalendarRepository.findByUuidAndDeletedAtIsNull(calendarUUID)
                .flatMap(value1 -> responseSuccessMsg("Record fetched successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_calendar_company_show")
    //Check Company uuid In Config Module
    public Mono<ServerResponse> getCompanyUUID(ServerRequest serverRequest) {
        UUID companyUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return serverRequest.formData()
                .flatMap(value -> slaveCalendarRepository.findFirstByCompanyUUIDAndDeletedAtIsNull(companyUUID)
                        .flatMap(value1 -> responseInfoMsg("Unable to Delete Record as the Reference Exists."))
                ).switchIfEmpty(responseErrorMsg("Record Does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_calendar_voucher_transaction-date_show")
    // show Calendars for voucher id And Transaction Date
    public Mono<ServerResponse> listOfCalendarsAgainstVoucherAndTransactionDate(ServerRequest serverRequest) {
        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());

        String transactionDate = serverRequest.queryParam("transactionDate").map(String::toString).orElse("");

        LocalDateTime startDate = LocalDateTime.parse(transactionDate, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("size").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("page").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        Pageable pageable = PageRequest.of(page, size);

        return slaveVoucherCalendarGroupPvtRepository.calendarGroupAllMappingExists(voucherUUID)
                .flatMap(all -> {

                    // if voucher is mapped with calendar group of all calendars
                    if(all) {

                        // if status is given in query parameter
                        if (!status.isEmpty()) {
                            Flux<SlaveCalendarCalendarPeriodDto> slaveCalendarEntityFlux = slaveCalendarRepository
                                    .listOfCalendarsAgainstTransactionDateWithStatus(startDate, searchKeyWord, searchKeyWord, Boolean.TRUE, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

                            return slaveCalendarEntityFlux
                                    .collectList()
                                    .flatMap(calendarEntity -> slaveCalendarRepository.countCalendarsAgainstTransactionDateWithStatus(startDate, searchKeyWord, searchKeyWord, Boolean.TRUE)
                                            .flatMap(count -> {

                                                //Check if the Given Calendar Is Open status is False
                                                for (SlaveCalendarCalendarPeriodDto calendar : calendarEntity) {

                                                    if (calendar.getIsOpen().equals(false)) {
                                                        return responseIndexInfoMsgWithEntity("Entry is not allowed for given calendar.", calendarEntity, count);
                                                    }
                                                }

                                                if (calendarEntity.isEmpty()) {
                                                    return responseIndexInfoMsgWithEntity("Entry is not allowed for given calendar.", calendarEntity, count);

                                                } else {
                                                    return responseIndexSuccessMsg("All Records Fetched Successfully", calendarEntity, count);
                                                }
                                            })
                                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
                        }

                        // if status is not given
                        else {
                            Flux<SlaveCalendarCalendarPeriodDto> slaveCalendarEntityFlux = slaveCalendarRepository
                                    .listOfCalendarsAgainstTransactionDate(startDate, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

                            return slaveCalendarEntityFlux
                                    .collectList()
                                    .flatMap(calendarEntity -> slaveCalendarRepository.countCalendarsAgainstTransactionDate(startDate, searchKeyWord, searchKeyWord)
                                            .flatMap(count -> {

                                                //Check if the Given Calendar Is Open status is False
                                                for (SlaveCalendarCalendarPeriodDto calendar : calendarEntity) {

                                                    if (calendar.getIsOpen().equals(false)) {
                                                        return responseIndexInfoMsgWithEntity("Entry is not allowed for given calendar.", calendarEntity, count);
                                                    }
                                                }

                                                if (calendarEntity.isEmpty()) {
                                                    return responseIndexInfoMsgWithEntity("Entry is not allowed for given calendar.", calendarEntity, count);

                                                } else {
                                                    return responseIndexSuccessMsg("All Records Fetched Successfully", calendarEntity, count);
                                                }
                                            })
                                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
                        }
                    }


                    // if voucher is not mapped with calendar group of all calendars
                    else {

                        // if status is given in query parameter
                        if (!status.isEmpty()) {
                            Flux<SlaveCalendarCalendarPeriodDto> slaveCalendarEntityFlux = slaveCalendarRepository
                                    .listOfCalendarsAgainstVoucherAndTransactionDateWithStatusFilter(voucherUUID, startDate, searchKeyWord, searchKeyWord, Boolean.valueOf(status), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

                            return slaveCalendarEntityFlux
                                    .collectList()
                                    .flatMap(calendarEntity -> slaveCalendarRepository.countCalendarsAgainstVoucherAndTransactionDateWithStatusFilter(voucherUUID, startDate, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                                            .flatMap(count -> {

                                                //Check if the Given Calendar Is Open status is False
                                                for (SlaveCalendarCalendarPeriodDto calendar : calendarEntity) {

                                                    if (calendar.getIsOpen().equals(false)) {
                                                        return responseIndexInfoMsgWithEntity("Entry is not allowed for given calendar.", calendarEntity, count);
                                                    }
                                                }

                                                if (calendarEntity.isEmpty()) {
                                                    return responseIndexInfoMsgWithEntity("Entry is not allowed for given calendar.", calendarEntity, count);

                                                } else {
                                                    return responseIndexSuccessMsg("All Records fetched successfully!", calendarEntity, count);
                                                }
                                            })
                                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
                        }

                        // if status is not given
                        else {
                            Flux<SlaveCalendarCalendarPeriodDto> slaveCalendarEntityFlux = slaveCalendarRepository
                                    .listOfCalendarsAgainstVoucherAndTransactionDate(voucherUUID, startDate, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

                            return slaveCalendarEntityFlux
                                    .collectList()
                                    .flatMap(calendarEntity -> slaveCalendarRepository.countCalendarsAgainstAndTransactionDate(voucherUUID, startDate, searchKeyWord, searchKeyWord)
                                            .flatMap(count -> {

                                                //Check if the Given Calendar Is Open status is False
                                                for (SlaveCalendarCalendarPeriodDto calendar : calendarEntity) {

                                                    if (calendar.getIsOpen().equals(false)) {
                                                        return responseIndexInfoMsgWithEntity("Entry is not allowed for given calendar.", calendarEntity, count);
                                                    }
                                                }

                                                if (calendarEntity.isEmpty()) {
                                                    return responseIndexInfoMsgWithEntity("Entry is not allowed for given calendar.", calendarEntity, count);

                                                } else {
                                                    return responseIndexSuccessMsg("All Records fetched successfully!", calendarEntity, count);
                                                }
                                            })
                                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
                        }

                    }
                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer"));
    }

    @AuthHasPermission(value = "account_api_v1_calendar_transaction-date_show")
    // show Calendars for voucher id And Transaction Date
    public Mono<ServerResponse> listOfCalendarsAgainstTransactionDate(ServerRequest serverRequest) {

        String transactionDate = serverRequest.queryParam("transactionDate").map(String::toString).orElse("");

        LocalDateTime startDate = LocalDateTime.parse(transactionDate, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        Pageable pageable = PageRequest.of(page, size);


        Flux<SlaveCalendarCalendarPeriodDto> slaveCalendarEntityFlux = slaveCalendarRepository
                .listOfCalendarsAgainstTransactionDateWithStatus(startDate, searchKeyWord, searchKeyWord, Boolean.TRUE, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

        return slaveCalendarEntityFlux
                .collectList()
                .flatMap(calendarEntity -> slaveCalendarRepository.countCalendarsAgainstTransactionDateWithStatus(startDate, searchKeyWord, searchKeyWord, Boolean.TRUE)
                        .flatMap(count -> {

                            //Check if the Given Calendar Is Open status is False
                            for (SlaveCalendarCalendarPeriodDto calendar : calendarEntity) {

                                if (calendar.getIsOpen().equals(false)) {
                                    return responseIndexInfoMsgWithEntity("Entry is not allowed for given calendar.", calendarEntity, count);
                                }
                            }

                            if (calendarEntity.isEmpty()) {
                                return responseIndexInfoMsgWithEntity("Entry is not allowed for given calendar.", calendarEntity, count);

                            } else {
                                return responseIndexSuccessMsg("All Records Fetched Successfully", calendarEntity, count);
                            }
                        })
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));

    }

    @AuthHasPermission(value = "account_api_v1_calendar_store")
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
                    LocalDateTime start_date = LocalDateTime.parse((value.getFirst("startDate")), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                    LocalDateTime end_date = LocalDateTime.parse((value.getFirst("endDate")), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

                    if (start_date.isAfter(end_date)) {
                        return responseInfoMsg("Calendar End Date Must Be After the Start Date!");
                    }

                    CalendarEntity calendarEntity = CalendarEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .description(value.getFirst("description").trim())
                            .calendarTypeUUID(UUID.fromString(value.getFirst("calendarTypeUUID").trim()))
                            .companyUUID(UUID.fromString(reqCompanyUUID))
                            .fiscalYear(LocalDateTime.parse((value.getFirst("fiscalYear")), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
                            .startDate(start_date)
                            .endDate(end_date)
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

                    //Checking If Name Already Exists
                    return calendarRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(calendarEntity.getName())
                            .flatMap(val -> responseInfoMsg("Name Already Exists"))
                            .switchIfEmpty(Mono.defer(() -> calendarRepository.findStartDateAndEndDateIsUnique(start_date, end_date)
                                    .flatMap(val -> responseInfoMsg("Calendar already exist with in duration"))
                            ))
                            .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(configUri + "api/v1/companies/show/", calendarEntity.getCompanyUUID())
                                    .flatMap(companyJson -> apiCallService.getUUID(companyJson)
                                            .flatMap(company -> calendarTypesRepository.findByUuidAndDeletedAtIsNull(calendarEntity.getCalendarTypeUUID())
                                                    .flatMap(calendarTypeEntity -> {

                                                        // if given company is inactive
                                                        if (!apiCallService.getStatus(companyJson)) {
                                                            return responseInfoMsg("Company status is inactive");
                                                        }

                                                        // if given calendar type is inactive
                                                        if (!calendarTypeEntity.getStatus()) {
                                                            return responseInfoMsg("Calendar Type status is inactive");
                                                        }

                                                        return calendarRepository.save(calendarEntity)
                                                                .flatMap(value1 -> responseSuccessMsg("Record Stored Successfully", value1))
                                                                .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please try again."))
                                                                .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                                    }).switchIfEmpty(responseInfoMsg("Calendar Type Does not Exist"))
                                                    .onErrorResume(ex -> responseErrorMsg("Calendar Type Does not Exist.Please Contact Developer."))
                                            ).switchIfEmpty(responseInfoMsg("Company does not Exist"))
                                            .onErrorResume(err -> responseErrorMsg("Company does not Exist. Please Contact Developer.")))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request. Please contact developer"));
    }

    @AuthHasPermission(value = "account_api_v1_calendar_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {

        UUID calendarUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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
                .flatMap(value -> calendarRepository.findByUuidAndDeletedAtIsNull(calendarUUID)
                        .flatMap(previousCalendarEntity -> {
                            LocalDateTime start_date = LocalDateTime.parse((value.getFirst("startDate")), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                            LocalDateTime end_date = LocalDateTime.parse((value.getFirst("endDate")), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

                            if (start_date.isAfter(end_date)) {
                                return responseInfoMsg("Calendar End Date Must Be After the Start Date!");
                            }

                            CalendarEntity calendarEntity = CalendarEntity
                                    .builder()
                                    .uuid(previousCalendarEntity.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .companyUUID(UUID.fromString(reqCompanyUUID))
                                    .calendarTypeUUID(UUID.fromString(value.getFirst("calendarTypeUUID").trim()))
                                    .fiscalYear(LocalDateTime.parse((value.getFirst("fiscalYear")), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
                                    .startDate(start_date)
                                    .endDate(end_date)
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdAt(previousCalendarEntity.getCreatedAt())
                                    .createdBy(previousCalendarEntity.getCreatedBy())
                                    .updatedBy(UUID.fromString(userId))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousCalendarEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousCalendarEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousCalendarEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousCalendarEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousCalendarEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousCalendarEntity.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousCalendarEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousCalendarEntity.setDeletedBy(UUID.fromString(userId));
                            previousCalendarEntity.setReqDeletedIP(reqIp);
                            previousCalendarEntity.setReqDeletedPort(reqPort);
                            previousCalendarEntity.setReqDeletedBrowser(reqBrowser);
                            previousCalendarEntity.setReqDeletedOS(reqOs);
                            previousCalendarEntity.setReqDeletedDevice(reqDevice);
                            previousCalendarEntity.setReqDeletedReferer(reqReferer);

                            // Checking If Name Already Exists
                            return calendarRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(calendarEntity.getName(), calendarUUID)
                                    .flatMap(val -> responseInfoMsg("Name Already Exists"))
                                    .switchIfEmpty(Mono.defer(() -> calendarRepository.findStartDateAndEndDateIsUniqueAndUuidIsNot(calendarEntity.getStartDate(), calendarEntity.getEndDate(), calendarUUID)
                                            .flatMap(val -> responseInfoMsg("Calendar already exist with in duration"))))
                                    .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(configUri + "api/v1/companies/show/", calendarEntity.getCompanyUUID())
                                            .flatMap(companyJson -> apiCallService.getUUID(companyJson)
                                                    .flatMap(company -> calendarTypesRepository.findByUuidAndDeletedAtIsNull(calendarEntity.getCalendarTypeUUID())
                                                            .flatMap(calendarTypeEntity -> {

                                                                // if given company is inactive
                                                                if (!apiCallService.getStatus(companyJson)) {
                                                                    return responseInfoMsg("Company status is inactive");
                                                                }

                                                                // if given calendar type is inactive
                                                                if (!calendarTypeEntity.getStatus()) {
                                                                    return responseInfoMsg("Calendar Type status is inactive");
                                                                }

                                                                return calendarRepository.save(previousCalendarEntity)
                                                                        .then(calendarRepository.save(calendarEntity))
                                                                        .flatMap(value1 -> responseSuccessMsg("Record Updated successfully!", value1))
                                                                        .switchIfEmpty(responseInfoMsg("Unable to update record.There is something wrong please try again."))
                                                                        .onErrorResume(err -> responseErrorMsg("Unable to update record.Please Contact Developer."));
                                                            })
                                                            .switchIfEmpty(responseInfoMsg("Calendar Type Does not Exist."))
                                                            .onErrorResume(ex -> responseErrorMsg("Calendar Type Does not Exist.Please Contact Developer."))
                                                    ).switchIfEmpty(responseInfoMsg("Company does not Exist"))
                                                    .onErrorResume(err -> responseErrorMsg("Company does not Exist. Please Contact Developer.")))
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Calendar Does Not Exist"))
                        .onErrorResume(err -> responseErrorMsg("Calendar Does Not Exist. Please contact developer"))
                ).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please contact developer"));
    }

    @AuthHasPermission(value = "account_api_v1_calendar_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        UUID calendarUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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

        //Check If Record Exists in Calendar Period
        return calendarRepository.findByUuidAndDeletedAtIsNull(calendarUUID)
                //if Exists Check Record Exist in Calendar
                .flatMap(calendarEntity -> calendarPeriodsRepository.findFirstByCalendarUUIDAndDeletedAtIsNull(calendarUUID)
                        .flatMap(value2 -> responseInfoMsg("Unable to Delete Record as the reference exist"))
                        //Check if Calendar Reference Exists in Calendar Group Calendar Pvt
                        .switchIfEmpty(Mono.defer(() -> calendarGroupCalendarPvtRepository.findFirstByCalendarUUIDAndDeletedAtIsNull(calendarEntity.getUuid())
                                .flatMap(calendarGroupCalendarPvtEntity -> responseInfoMsg("Unable to Delete Record as the reference exist"))))
                        // Allow deleting Record
                        .switchIfEmpty(Mono.defer(() -> {

                            calendarEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            calendarEntity.setDeletedBy(UUID.fromString(userId));
                            calendarEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            calendarEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            calendarEntity.setReqDeletedIP(reqIp);
                            calendarEntity.setReqDeletedPort(reqPort);
                            calendarEntity.setReqDeletedBrowser(reqBrowser);
                            calendarEntity.setReqDeletedOS(reqOs);
                            calendarEntity.setReqDeletedDevice(reqDevice);
                            calendarEntity.setReqDeletedReferer(reqReferer);

                            return calendarRepository.save(calendarEntity)
                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                    .switchIfEmpty(responseInfoMsg("Unable to Delete Record.There is something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Delete Record.Please Contact Developer."));
                        }))
                ).switchIfEmpty(responseInfoMsg("Record Does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_calendar_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID calendarUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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
                    return calendarRepository.findByUuidAndDeletedAtIsNull(calendarUUID)
                            .flatMap(previousCalendarEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousCalendarEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                CalendarEntity updatedEntity = CalendarEntity
                                        .builder()
                                        .uuid(previousCalendarEntity.getUuid())
                                        .name(previousCalendarEntity.getName())
                                        .description(previousCalendarEntity.getDescription())
                                        .calendarTypeUUID(previousCalendarEntity.getCalendarTypeUUID())
                                        .companyUUID(previousCalendarEntity.getCompanyUUID())
                                        .fiscalYear(previousCalendarEntity.getFiscalYear())
                                        .startDate(previousCalendarEntity.getStartDate())
                                        .endDate(previousCalendarEntity.getEndDate())
                                        .status(status == true ? true : false)
                                        .createdBy(previousCalendarEntity.getCreatedBy())
                                        .createdAt(previousCalendarEntity.getCreatedAt())
                                        .updatedBy(UUID.fromString(userId))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousCalendarEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousCalendarEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousCalendarEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousCalendarEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousCalendarEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousCalendarEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousCalendarEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousCalendarEntity.setDeletedBy(UUID.fromString(userId));
                                previousCalendarEntity.setReqDeletedIP(reqIp);
                                previousCalendarEntity.setReqDeletedPort(reqPort);
                                previousCalendarEntity.setReqDeletedBrowser(reqBrowser);
                                previousCalendarEntity.setReqDeletedOS(reqOs);
                                previousCalendarEntity.setReqDeletedDevice(reqDevice);
                                previousCalendarEntity.setReqDeletedReferer(reqReferer);

                                return calendarRepository.save(previousCalendarEntity)
                                        .then(calendarRepository.save(updatedEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status updated successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status"))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status.There is something wrong please try again."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Record does not exist.Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please contact developer."));
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

    public Mono<ServerResponse> responseIndexInfoMsgWithEntity(String msg, Object entity, Long totalDataRowsWithFilter) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.INFO,
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