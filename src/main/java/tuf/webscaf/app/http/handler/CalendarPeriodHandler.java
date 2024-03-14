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
import tuf.webscaf.app.dbContext.master.entity.CalendarPeriodEntity;
import tuf.webscaf.app.dbContext.master.repository.CalendarPeriodsRepository;

import tuf.webscaf.app.dbContext.master.repository.CalendarRepository;
import tuf.webscaf.app.dbContext.master.repository.CalendarTypesRepository;
import tuf.webscaf.app.dbContext.master.repository.TransactionRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarPeriodEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveCalendarPeriodsRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveCalendarTypesRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Tag(name = "calendarPeriodHandler")
public class CalendarPeriodHandler {

    @Autowired
    CalendarPeriodsRepository calendarPeriodsRepository;

    @Autowired
    SlaveCalendarPeriodsRepository slaveCalendarPeriodsRepository;

    @Autowired
    SlaveCalendarTypesRepository slaveCalendarTypesRepository;

    @Autowired
    CalendarTypesRepository calendarTypesRepository;

    @Autowired
    CalendarRepository calendarRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.erp_emp_financial_module.uri}")
    private String empFinancialModuleUri;

    @Value("${server.erp_student_financial_module.uri}")
    private String studentFinancialModuleUri;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_calendar-period_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter of Status
        String status = serverRequest.queryParam("isOpenStatus").map(String::toString).orElse("").trim();

        //Optional Query Parameter of Calendar UUID
        String calendarUUID = serverRequest.queryParam("calendarUUID").map(String::toString).orElse("").trim();

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

        // if both is open and calendar uuid are given
        if (!status.isEmpty() && !calendarUUID.isEmpty()) {
            Flux<SlaveCalendarPeriodEntity> slaveCalendarPeriodEntityFlux = slaveCalendarPeriodsRepository
                    .findAllByNameContainingIgnoreCaseAndCalendarUUIDAndIsOpenAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCalendarUUIDAndIsOpenAndDeletedAtIsNull(pageable, searchKeyWord, UUID.fromString(calendarUUID), Boolean.TRUE, searchKeyWord, UUID.fromString(calendarUUID), Boolean.TRUE);
            return slaveCalendarPeriodEntityFlux
                    .collectList()
                    .flatMap(periodEntity -> slaveCalendarPeriodsRepository
                            .countByNameContainingIgnoreCaseAndCalendarUUIDAndIsOpenAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCalendarUUIDAndIsOpenAndDeletedAtIsNull(searchKeyWord, UUID.fromString(calendarUUID), Boolean.TRUE, searchKeyWord, UUID.fromString(calendarUUID), Boolean.TRUE)
                            .flatMap(count -> {

                                if (periodEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", periodEntity, count);
                                }

                            })).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }


        // if calendar uuid is given
        else if (!calendarUUID.isEmpty()) {
            Flux<SlaveCalendarPeriodEntity> slaveCalendarPeriodEntityFlux = slaveCalendarPeriodsRepository
                    .findAllByNameContainingIgnoreCaseAndCalendarUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCalendarUUIDAndDeletedAtIsNull(pageable, searchKeyWord, UUID.fromString(calendarUUID), searchKeyWord, UUID.fromString(calendarUUID));
            return slaveCalendarPeriodEntityFlux
                    .collectList()
                    .flatMap(periodEntity -> slaveCalendarPeriodsRepository
                            .countByNameContainingIgnoreCaseAndCalendarUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCalendarUUIDAndDeletedAtIsNull(searchKeyWord, UUID.fromString(calendarUUID), searchKeyWord, UUID.fromString(calendarUUID))
                            .flatMap(count -> {

                                if (periodEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", periodEntity, count);
                                }

                            })).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }

        // if is open is given
        else if (!status.isEmpty()) {
            Flux<SlaveCalendarPeriodEntity> slaveCalendarPeriodEntityFlux = slaveCalendarPeriodsRepository
                    .findAllByNameContainingIgnoreCaseAndIsOpenAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndIsOpenAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));
            return slaveCalendarPeriodEntityFlux
                    .collectList()
                    .flatMap(periodEntity -> slaveCalendarPeriodsRepository
                            .countByNameContainingIgnoreCaseAndIsOpenAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndIsOpenAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {

                                if (periodEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", periodEntity, count);
                                }

                            })).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }

        // if none of query params are given
        else {
            Flux<SlaveCalendarPeriodEntity> slaveCalendarPeriodEntityFlux = slaveCalendarPeriodsRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
            return slaveCalendarPeriodEntityFlux
                    .collectList()
                    .flatMap(periodEntity -> slaveCalendarPeriodsRepository
                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {

                                if (periodEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", periodEntity, count);
                                }

                            })).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_calendar-period_active_index")
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


        Flux<SlaveCalendarPeriodEntity> slaveCalendarPeriodEntityFlux = slaveCalendarPeriodsRepository
                .findAllByNameContainingIgnoreCaseAndIsOpenAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndIsOpenAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE);
        return slaveCalendarPeriodEntityFlux
                .collectList()
                .flatMap(periodEntity -> slaveCalendarPeriodsRepository
                        .countByNameContainingIgnoreCaseAndIsOpenAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndIsOpenAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE)
                        .flatMap(count -> {

                            if (periodEntity.isEmpty()) {
                                return responseIndexInfoMsg("Record does not exist", count);
                            } else {
                                return responseIndexSuccessMsg("All Records Fetched Successfully", periodEntity, count);
                            }

                        })).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));

    }


    @AuthHasPermission(value = "account_api_v1_calendar-period_calendar_index")
    public Mono<ServerResponse> indexWithCalendar(ServerRequest serverRequest) {

        UUID calendarUUID = UUID.fromString(serverRequest.pathVariable("calendarUUID"));

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


        Flux<SlaveCalendarPeriodEntity> slaveCalendarPeriodEntityFlux = slaveCalendarPeriodsRepository
                .findAllByNameContainingIgnoreCaseAndCalendarUUIDAndIsOpenAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCalendarUUIDAndIsOpenAndDeletedAtIsNull(pageable, searchKeyWord, calendarUUID, Boolean.TRUE, searchKeyWord, calendarUUID, Boolean.TRUE);
        return slaveCalendarPeriodEntityFlux
                .collectList()
                .flatMap(periodEntity -> slaveCalendarPeriodsRepository
                        .countByNameContainingIgnoreCaseAndCalendarUUIDAndIsOpenAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCalendarUUIDAndIsOpenAndDeletedAtIsNull(searchKeyWord, calendarUUID, Boolean.TRUE, searchKeyWord, calendarUUID, Boolean.TRUE)
                        .flatMap(count -> {

                            if (periodEntity.isEmpty()) {
                                return responseIndexInfoMsg("Record does not exist", count);
                            } else {
                                return responseIndexSuccessMsg("All Records Fetched Successfully", periodEntity, count);
                            }

                        })).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));

    }

    @AuthHasPermission(value = "account_api_v1_calendar-period_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID calendarPeriodUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveCalendarPeriodsRepository.findByUuidAndDeletedAtIsNull(calendarPeriodUUID)
                .flatMap(value1 -> responseSuccessMsg("Record fetched successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist."))
                .onErrorResume(err -> responseErrorMsg("Record does not exist. Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_calendar-period_transaction-date_show")
    public Mono<ServerResponse> showWithTransactionDate(ServerRequest serverRequest) {
        String transactionDate = serverRequest.queryParam("transactionDate").map(String::toString).orElse("");

        LocalDateTime startDate = LocalDateTime.parse(transactionDate, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        return slaveCalendarPeriodsRepository.showRecordWithTransactionDate(startDate)
                .flatMap(value1 -> responseSuccessMsg("Record fetched successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist."))
                .onErrorResume(err -> responseErrorMsg("Record does not exist. Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_calendar-period_store")
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

        List<String> calenderPeriodValues = new ArrayList<String>();
        List<Integer> calenderPeriodNoList = new ArrayList<>();
        return serverRequest.formData()
                .flatMap(value -> calendarRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(value.getFirst("calendarUUID").trim()))
                        // get all records with given calenderId
                        .flatMap(calendarEntity -> calendarPeriodsRepository.findAllByCalendarUUIDAndDeletedAtIsNull(calendarEntity.getUuid())
                                .collectList()
                                .flatMap(calendarPeriods -> {

                                    for (CalendarPeriodEntity entity : calendarPeriods) {
                                        // check if name exists with given calenderId and add it in list
                                        if ((value.getFirst("name").trim()).equals(entity.getName())) {
                                            calenderPeriodValues.add(entity.getName());
                                        }
                                        if (Integer.valueOf(value.getFirst("periodNo")).equals(entity.getPeriodNo())) {
                                            calenderPeriodNoList.add(entity.getPeriodNo());
                                        }
                                    }

                                    // if name exist in any of records with given calenderId
                                    if (!calenderPeriodValues.isEmpty()) {
                                        return responseInfoMsg("Name Already Exists");
                                    }

                                    // If periodNo already exist in any of records with given calendarId
                                    if (!calenderPeriodNoList.isEmpty()) {
                                        return responseInfoMsg("Period No Already Exists");
                                    }
                                    // when name is unique with given calenderId, store the record
                                    else {

                                        Integer getQuarter = null;
                                        if (value.containsKey("quarter") && value.getFirst("quarter") != "") {
                                            getQuarter = Integer.valueOf(value.getFirst("quarter"));
                                        }

                                        LocalDateTime startDate = LocalDateTime.parse((value.getFirst("startDate")), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                                        LocalDateTime endDate = LocalDateTime.parse((value.getFirst("endDate")), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

                                        LocalDateTime calendarStartDate = calendarEntity.getStartDate();
                                        LocalDateTime calendarEndDate = calendarEntity.getEndDate();

                                        //If period start date or end date is not within the calendar start date or end date

                                        int periodStartDateCalenderStartDateDif = startDate.compareTo(calendarStartDate);
                                        if (periodStartDateCalenderStartDateDif < 0) {
                                            return responseInfoMsg("Calendar Period Start Date is before Calendar Start Date");
                                        }

                                        int periodStartDateCalenderEndDateDif = startDate.compareTo(calendarEndDate);
                                        if (periodStartDateCalenderEndDateDif > 0) {
                                            return responseInfoMsg("Calendar Period Start Date is after Calendar End Date");
                                        }

                                        int periodEndDateCalenderStartDateDif = endDate.compareTo(calendarStartDate);
                                        if (periodEndDateCalenderStartDateDif < 0) {
                                            return responseInfoMsg("Calendar Period End Date is before Calendar Start Date");
                                        }

                                        int periodEndDateCalenderEndDateDif = endDate.compareTo(calendarEndDate);
                                        if (periodEndDateCalenderEndDateDif > 0) {
                                            return responseInfoMsg("Calendar Period End Date is after Calendar End Date");
                                        }


                                        CalendarPeriodEntity calendarPeriodEntity = CalendarPeriodEntity.builder()
                                                .uuid(UUID.randomUUID())
                                                .name(value.getFirst("name").trim())
                                                .prefix(value.getFirst("prefix").trim())
                                                .description(value.getFirst("description").trim())
                                                .isOpenAuto(Boolean.valueOf(value.getFirst("isOpenAuto")))
                                                .adjustments(Boolean.valueOf(value.getFirst("adjustments")))
                                                .periodNo(Integer.valueOf(value.getFirst("periodNo")))
                                                .quarter(getQuarter)
                                                .calendarUUID(calendarEntity.getUuid())
                                                .startDate(startDate)
                                                .endDate(endDate)
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

                                        return calendarPeriodsRepository.findStartDateAndEndDateIsUnique(startDate, endDate)
                                                .flatMap(val -> responseInfoMsg("Calendar Period already exist with in duration"))
                                                .switchIfEmpty(Mono.defer(() -> calendarTypesRepository.findByUuidAndDeletedAtIsNull(calendarEntity.getCalendarTypeUUID())
                                                        .flatMap(calendarTypeEntity -> {

                                                            // if given calendar is inactive
                                                            if (!calendarEntity.getStatus()) {
                                                                return responseInfoMsg("Calendar status is inactive");
                                                            }

                                                            if ((calendarPeriods.size() < calendarTypeEntity.getPeriods())) {
                                                                return calendarPeriodsRepository.save(calendarPeriodEntity)
                                                                        .flatMap(saveEntity -> responseSuccessMsg("Record Stored Successfully", saveEntity))
                                                                        .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please try again."))
                                                                        .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer"));
                                                            } else {
                                                                return responseInfoMsg("Unable to save Calender Period.Calender Period exceeds the Periods for given Calendar!");
                                                            }
                                                        })
                                                ));
                                    }
                                })
                        ).switchIfEmpty(responseInfoMsg("Calendar Does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Calendar Does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_calendar-period_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID calendarPeriodUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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

        List<String> calenderPeriodValues = new ArrayList<String>();
        List<Integer> calenderPeriodNoList = new ArrayList<>();
        return serverRequest.formData()
                .flatMap(value -> calendarPeriodsRepository.findByUuidAndDeletedAtIsNull(calendarPeriodUUID)
                        .flatMap(previousCalendarPeriod -> calendarRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(value.getFirst("calendarUUID").trim()))
                                // get all records with given calenderId, with current Id excluded
                                .flatMap(calendarEntity -> calendarPeriodsRepository.findAllByCalendarUUIDAndDeletedAtIsNullAndUuidIsNot(calendarEntity.getUuid(), previousCalendarPeriod.getUuid())
                                        .collectList()
                                        .flatMap(calendarPeriods -> {

                                            for (CalendarPeriodEntity calenderPeriodEntity : calendarPeriods) {
                                                // check if name exists with given calenderId and add it in list
                                                if ((value.getFirst("name").trim()).equals(calenderPeriodEntity.getName())) {
                                                    calenderPeriodValues.add(previousCalendarPeriod.getName());
                                                }
                                                if (Integer.valueOf(value.getFirst("periodNo")).equals(calenderPeriodEntity.getPeriodNo())) {
                                                    calenderPeriodNoList.add(calenderPeriodEntity.getPeriodNo());
                                                }
                                            }

                                            // if name exist in any of records with given calenderId
                                            if (!calenderPeriodValues.isEmpty()) {
                                                return responseInfoMsg("Name Already Exists");
                                            }

                                            // If periodNo already exist in any of records with given calendarId
                                            if (!calenderPeriodNoList.isEmpty()) {
                                                return responseInfoMsg("Period No Already Exists");
                                            }

                                            // when name is unique with given calenderId, update the record

                                            else {

                                                Integer getQuarter = null;
                                                if (value.containsKey("quarter") && (value.getFirst("quarter") != "")) {
                                                    getQuarter = Integer.valueOf(value.getFirst("quarter"));
                                                }

                                                LocalDateTime startDate = LocalDateTime.parse((value.getFirst("startDate")), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                                                LocalDateTime endDate = LocalDateTime.parse((value.getFirst("endDate")), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

                                                LocalDateTime calendarStartDate = calendarEntity.getStartDate();
                                                LocalDateTime calendarEndDate = calendarEntity.getEndDate();

                                                //If period start date or end date is not within the calendar start date or end date

                                                int periodStartDateCalenderStartDateDif = startDate.compareTo(calendarStartDate);
                                                if (periodStartDateCalenderStartDateDif < 0) {
                                                    return responseInfoMsg("Calendar Period Start Date is before Calendar Start Date");
                                                }

                                                int periodStartDateCalenderEndDateDif = startDate.compareTo(calendarEndDate);
                                                if (periodStartDateCalenderEndDateDif > 0) {
                                                    return responseInfoMsg("Calendar Period Start Date is after Calendar End Date");
                                                }

                                                int periodEndDateCalenderStartDateDif = endDate.compareTo(calendarStartDate);
                                                if (periodEndDateCalenderStartDateDif < 0) {
                                                    return responseInfoMsg("Calendar Period End Date is before Calendar Start Date");
                                                }

                                                int periodEndDateCalenderEndDateDif = endDate.compareTo(calendarEndDate);
                                                if (periodEndDateCalenderEndDateDif > 0) {
                                                    return responseInfoMsg("Calendar Period End Date is after Calendar End Date");
                                                }


                                                CalendarPeriodEntity calendarPeriodEntity = CalendarPeriodEntity.builder()
                                                        .uuid(previousCalendarPeriod.getUuid())
                                                        .name(value.getFirst("name").trim())
                                                        .prefix(value.getFirst("prefix").trim())
                                                        .description(value.getFirst("description").trim())
                                                        .isOpenAuto(Boolean.valueOf(value.getFirst("isOpenAuto")))
                                                        .adjustments(Boolean.valueOf(value.getFirst("adjustments")))
                                                        .periodNo(Integer.valueOf(value.getFirst("periodNo")))
                                                        .quarter(getQuarter)
                                                        .calendarUUID(calendarEntity.getUuid())
                                                        .startDate(startDate)
                                                        .endDate(endDate)
                                                        .createdBy(previousCalendarPeriod.getCreatedBy())
                                                        .createdAt(previousCalendarPeriod.getCreatedAt())
                                                        .updatedBy(UUID.fromString(userId))
                                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                                        .reqCreatedIP(previousCalendarPeriod.getReqCreatedIP())
                                                        .reqCreatedPort(previousCalendarPeriod.getReqCreatedPort())
                                                        .reqCreatedBrowser(previousCalendarPeriod.getReqCreatedBrowser())
                                                        .reqCreatedOS(previousCalendarPeriod.getReqCreatedOS())
                                                        .reqCreatedDevice(previousCalendarPeriod.getReqCreatedDevice())
                                                        .reqCreatedReferer(previousCalendarPeriod.getReqCreatedReferer())
                                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                                        .reqUpdatedIP(reqIp)
                                                        .reqUpdatedPort(reqPort)
                                                        .reqUpdatedBrowser(reqBrowser)
                                                        .reqUpdatedOS(reqOs)
                                                        .reqUpdatedDevice(reqDevice)
                                                        .reqUpdatedReferer(reqReferer)
                                                        .build();

                                                previousCalendarPeriod.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                                previousCalendarPeriod.setDeletedBy(UUID.fromString(userId));
                                                previousCalendarPeriod.setReqDeletedIP(reqIp);
                                                previousCalendarPeriod.setReqDeletedPort(reqPort);
                                                previousCalendarPeriod.setReqDeletedBrowser(reqBrowser);
                                                previousCalendarPeriod.setReqDeletedOS(reqOs);
                                                previousCalendarPeriod.setReqDeletedDevice(reqDevice);
                                                previousCalendarPeriod.setReqDeletedReferer(reqReferer);

                                                return calendarPeriodsRepository.findStartDateAndEndDateIsUniqueAndUuidIsNot(startDate, endDate, calendarPeriodUUID)
                                                        .flatMap(val -> responseInfoMsg("Calendar Period already exist with in duration"))
                                                        .switchIfEmpty(Mono.defer(() -> calendarTypesRepository.findByUuidAndDeletedAtIsNull(calendarEntity.getCalendarTypeUUID())
                                                                .flatMap(calendarTypeEntity -> calendarPeriodsRepository.findAllByCalendarUUIDAndDeletedAtIsNull(calendarPeriodEntity.getCalendarUUID())
                                                                        .collectList()
                                                                        .flatMap(calendarPeriodList -> {

                                                                            // if given calendar is inactive
                                                                            if (!calendarEntity.getStatus()) {
                                                                                return responseInfoMsg("Calendar status is inactive");
                                                                            }

                                                                            if ((calendarPeriods.size() < calendarTypeEntity.getPeriods())) {
                                                                                return calendarPeriodsRepository.save(previousCalendarPeriod)
                                                                                        .then(calendarPeriodsRepository.save(calendarPeriodEntity))
                                                                                        .flatMap(saveEntity -> responseSuccessMsg("Record Updated Successfully", saveEntity))
                                                                                        .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong please try again."))
                                                                                        .onErrorResume(ex -> responseErrorMsg("Unable to Update Record. Please contact developer"));
                                                                            } else {
                                                                                return responseInfoMsg("Unable to update Calender Period. Calender Period exceeds the Periods for given Calendar.");
                                                                            }
                                                                        }).switchIfEmpty(responseInfoMsg("Calendar doest not exist"))
                                                                        .onErrorResume(ex -> responseErrorMsg("Calendar doest not exist. Please contact developer"))
                                                                ).switchIfEmpty(responseInfoMsg("Calendar Type Does not exist."))
                                                                .onErrorResume(ex -> responseErrorMsg("Calendar Type Does not exist.Please Contact Developer."))
                                                        ));
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Calendar Does not exist."))
                                .onErrorResume(ex -> responseErrorMsg("Calendar Does not exist.Please Contact Developer."))
                        ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to Read Request.There is something wrong please try again."))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request. Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_calendar-period_adjustments_store")
    public Mono<ServerResponse> storeAdjustmentEntry(ServerRequest serverRequest) {
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

        List<String> calenderPeriodValues = new ArrayList<String>();
        List<Integer> calenderPeriodNoList = new ArrayList<>();
        return serverRequest.formData()
                .flatMap(value -> calendarRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(value.getFirst("calendarUUID").trim()))
//                        get all records with given calenderId
                                .flatMap(calendarEntity -> calendarPeriodsRepository.findAllByCalendarUUIDAndDeletedAtIsNull(calendarEntity.getUuid())
                                        .collectList()
                                        .flatMap(calendarPeriods -> {

                                            Boolean getAdjustmentRecord = Boolean.valueOf(value.getFirst("adjustments"));

                                            if (getAdjustmentRecord) {
                                                for (CalendarPeriodEntity entity : calendarPeriods) {
                                                    // check if name exists with given calenderId and add it in list
                                                    if ((value.getFirst("name").trim()).equals(entity.getName())) {
                                                        calenderPeriodValues.add(entity.getName());
                                                    }

                                                    if (Integer.valueOf(value.getFirst("periodNo")).equals(entity.getPeriodNo())) {
                                                        calenderPeriodNoList.add(entity.getPeriodNo());
                                                    }
                                                }

                                                // if name exist in any of records with given calenderId
                                                if (!calenderPeriodValues.isEmpty()) {
                                                    return responseInfoMsg("Name Already Exists!");
                                                }

                                                // If periodNo already exist in any of records with given calendarId
                                                else if (!calenderPeriodNoList.isEmpty()) {
                                                    return responseInfoMsg("Period No Already Exists!");
                                                } else {
                                                    Integer getQuarter = null;
                                                    if (value.containsKey("quarter")) {
                                                        if (value.getFirst("quarter") != "") {
                                                            getQuarter = Integer.valueOf(value.getFirst("quarter"));
                                                        }
                                                    }

                                                    Boolean gettingIsOpenAuto = true;
                                                    if (value.getFirst("isOpenAuto") != "") {
                                                        gettingIsOpenAuto = Boolean.valueOf(value.getFirst("isOpenAuto"));
                                                    }


                                                    LocalDateTime startDate = LocalDateTime.parse((value.getFirst("startDate")), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                                                    LocalDateTime endDate = LocalDateTime.parse((value.getFirst("endDate")), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

                                                    LocalDateTime calendarStartDate = calendarEntity.getStartDate();
                                                    LocalDateTime calendarEndDate = calendarEntity.getEndDate();

                                                    //If period start date or end date is not within the calendar start date or end date

                                                    int periodStartDateCalenderStartDateDif = startDate.compareTo(calendarStartDate);
                                                    if (periodStartDateCalenderStartDateDif < 0) {
                                                        return responseInfoMsg("Calendar Period Start Date is before Calendar Start Date!");
                                                    }

                                                    int periodStartDateCalenderEndDateDif = startDate.compareTo(calendarEndDate);
                                                    if (periodStartDateCalenderEndDateDif > 0) {
                                                        return responseInfoMsg("Calendar Period Start Date is after Calendar End Date!");
                                                    }

                                                    int periodEndDateCalenderStartDateDif = endDate.compareTo(calendarStartDate);
                                                    if (periodEndDateCalenderStartDateDif < 0) {
                                                        return responseInfoMsg("Calendar Period End Date is before Calendar Start Date!");
                                                    }

                                                    int periodEndDateCalenderEndDateDif = endDate.compareTo(calendarEndDate);
                                                    if (periodEndDateCalenderEndDateDif > 0) {
                                                        return responseInfoMsg("Calendar Period End Date is after Calendar End Date!");
                                                    }


                                                    CalendarPeriodEntity calendarPeriodEntity = CalendarPeriodEntity.builder()
                                                            .name(value.getFirst("name").trim())
                                                            .uuid(UUID.randomUUID())
                                                            .prefix(value.getFirst("prefix").trim())
                                                            .description(value.getFirst("description").trim())
                                                            .isOpenAuto(gettingIsOpenAuto)
                                                            .adjustments(getAdjustmentRecord)
                                                            .periodNo(Integer.valueOf(value.getFirst("periodNo")))
                                                            .quarter(getQuarter)
                                                            .calendarUUID(calendarEntity.getUuid())
                                                            .startDate(startDate)
                                                            .endDate(endDate)
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


                                                    return calendarPeriodsRepository.findStartDateAndEndDateIsUniqueAndAdjustmentIsTrue(calendarPeriodEntity.getStartDate(), calendarPeriodEntity.getEndDate())
                                                            .flatMap(calendarAdjustmentEntity -> responseInfoMsg("The Adjustment Entry Already Exists against this Duration in Calendar period"))
                                                            .switchIfEmpty(Mono.defer(() -> calendarTypesRepository.findByUuidAndDeletedAtIsNull(calendarEntity.getCalendarTypeUUID())
                                                                    .flatMap(calendarTypeEntity -> {

                                                                        if (calendarPeriods.size() < calendarTypeEntity.getPeriods()) {
                                                                            return calendarPeriodsRepository.save(calendarPeriodEntity)
                                                                                    .flatMap(saveEntity -> responseSuccessMsg("Record stored successfully!", saveEntity))
                                                                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please try again."))
                                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                                                        } else {
                                                                            return responseInfoMsg("Unable to save Calender Period.Calender Period exceeds the Periods for given Calendar!");
                                                                        }
                                                                    })));
                                                }
                                            } else {
                                                return responseInfoMsg("Adjustment Entry is not allowed for the Given year.");
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Calendar Does not exist"))
                                .onErrorResume(ex -> responseErrorMsg("Create Calendar First.Please Contact Developer."))
                ).switchIfEmpty(Mono.defer(() -> responseInfoMsg("Unable to Read Request")))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_calendar-period_adjustments_update")
    public Mono<ServerResponse> updateAdjustmentEntry(ServerRequest serverRequest) {
        UUID calendarPeriodUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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

        List<String> calenderPeriodValues = new ArrayList<String>();
        List<Integer> calenderPeriodNoList = new ArrayList<>();
        return serverRequest.formData()
                .flatMap(value -> calendarPeriodsRepository.findByUuidAndDeletedAtIsNull(calendarPeriodUUID)
                        .flatMap(previousCalendarPeriod -> calendarRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(value.getFirst("calendarUUID").trim()))
                                // get all records with given calenderId, with current Id excluded
                                .flatMap(calendarEntity -> calendarPeriodsRepository.findAllByCalendarUUIDAndDeletedAtIsNullAndUuidIsNot(calendarEntity.getUuid(), previousCalendarPeriod.getUuid())
                                        .collectList()
                                        .flatMap(calendarPeriods -> {
                                            Boolean getAdjustmentRecord = Boolean.valueOf(value.getFirst("adjustments"));

                                            if (getAdjustmentRecord) {
                                                for (CalendarPeriodEntity calenderPeriodEntity : calendarPeriods) {
                                                    // check if name exists with given calenderId and add it in list
                                                    if ((value.getFirst("name").trim()).equals(calenderPeriodEntity.getName())) {
                                                        calenderPeriodValues.add(previousCalendarPeriod.getName());
                                                    }
                                                    if (Integer.valueOf(value.getFirst("periodNo")).equals(calenderPeriodEntity.getPeriodNo())) {
                                                        calenderPeriodNoList.add(calenderPeriodEntity.getPeriodNo());
                                                    }
                                                }

                                                // if name exist in any of records with given calenderId
                                                if (!calenderPeriodValues.isEmpty()) {
                                                    return responseInfoMsg("Name Already Exists");
                                                }
                                                // If periodNo already exist in any of records with given calendarId
                                                else if (!calenderPeriodNoList.isEmpty()) {
                                                    return responseInfoMsg("Period No Already Exists");
                                                }
                                                // when name is unique with given calenderId, update the record
                                                else {

                                                    Integer getQuarter = null;
                                                    if (value.containsKey("quarter") && (value.getFirst("quarter") != "")) {
                                                        getQuarter = Integer.valueOf(value.getFirst("quarter"));
                                                    }

                                                    LocalDateTime startDate = LocalDateTime.parse((value.getFirst("startDate")), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                                                    LocalDateTime endDate = LocalDateTime.parse((value.getFirst("endDate")), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

                                                    LocalDateTime calendarStartDate = calendarEntity.getStartDate();
                                                    LocalDateTime calendarEndDate = calendarEntity.getEndDate();

                                                    //If period start date or end date is not within the calendar start date or end date

                                                    int periodStartDateCalenderStartDateDif = startDate.compareTo(calendarStartDate);
                                                    if (periodStartDateCalenderStartDateDif < 0) {
                                                        return responseInfoMsg("Calendar Period Start Date is before Calendar Start Date");
                                                    }

                                                    int periodStartDateCalenderEndDateDif = startDate.compareTo(calendarEndDate);
                                                    if (periodStartDateCalenderEndDateDif > 0) {
                                                        return responseInfoMsg("Calendar Period Start Date is after Calendar End Date");
                                                    }

                                                    int periodEndDateCalenderStartDateDif = endDate.compareTo(calendarStartDate);
                                                    if (periodEndDateCalenderStartDateDif < 0) {
                                                        return responseInfoMsg("Calendar Period End Date is before Calendar Start Date");
                                                    }

                                                    int periodEndDateCalenderEndDateDif = endDate.compareTo(calendarEndDate);
                                                    if (periodEndDateCalenderEndDateDif > 0) {
                                                        return responseInfoMsg("Calendar Period End Date is after Calendar End Date");
                                                    }


                                                    CalendarPeriodEntity calendarPeriodEntity = CalendarPeriodEntity.builder()
                                                            .name(value.getFirst("name").trim())
                                                            .uuid(previousCalendarPeriod.getUuid())
                                                            .prefix(value.getFirst("prefix").trim())
                                                            .description(value.getFirst("description").trim())
                                                            .isOpenAuto(Boolean.valueOf(value.getFirst("isOpenAuto")))
                                                            .adjustments(getAdjustmentRecord)
                                                            .periodNo(Integer.valueOf(value.getFirst("periodNo")))
                                                            .quarter(getQuarter)
                                                            .calendarUUID(calendarEntity.getUuid())
                                                            .startDate(startDate)
                                                            .endDate(endDate)
                                                            .createdAt(previousCalendarPeriod.getCreatedAt())
                                                            .createdBy(previousCalendarPeriod.getCreatedBy())
                                                            .updatedBy(UUID.fromString(userId))
                                                            .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                                            .reqCreatedIP(previousCalendarPeriod.getReqCreatedIP())
                                                            .reqCreatedPort(previousCalendarPeriod.getReqCreatedPort())
                                                            .reqCreatedBrowser(previousCalendarPeriod.getReqCreatedBrowser())
                                                            .reqCreatedOS(previousCalendarPeriod.getReqCreatedOS())
                                                            .reqCreatedDevice(previousCalendarPeriod.getReqCreatedDevice())
                                                            .reqCreatedReferer(previousCalendarPeriod.getReqCreatedReferer())
                                                            .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                                            .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                                            .reqUpdatedIP(reqIp)
                                                            .reqUpdatedPort(reqPort)
                                                            .reqUpdatedBrowser(reqBrowser)
                                                            .reqUpdatedOS(reqOs)
                                                            .reqUpdatedDevice(reqDevice)
                                                            .reqUpdatedReferer(reqReferer)
                                                            .build();

                                                    previousCalendarPeriod.setDeletedBy(UUID.fromString(userId));
                                                    previousCalendarPeriod.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));

                                                    return calendarPeriodsRepository.findStartDateAndEndDateIsUniqueAndUuidIsNotAndAdjustmentIsTrue(calendarPeriodEntity.getStartDate(), calendarPeriodEntity.getEndDate(), calendarPeriodUUID)
                                                            .flatMap(calendarAdjustmentEntity -> responseInfoMsg("The Adjustment Entry Already Exists against this Duration in Calendar period"))
                                                            .switchIfEmpty(Mono.defer(() -> calendarTypesRepository.findByUuidAndDeletedAtIsNull(calendarEntity.getCalendarTypeUUID())
                                                                    .flatMap(calendarTypeEntity -> {

                                                                        if (calendarPeriods.size() < calendarTypeEntity.getPeriods()) {
                                                                            return calendarPeriodsRepository.save(previousCalendarPeriod)
                                                                                    .then(calendarPeriodsRepository.save(calendarPeriodEntity))
                                                                                    .flatMap(saveEntity -> responseSuccessMsg("Record Stored Successfully", saveEntity))
                                                                                    .switchIfEmpty(responseInfoMsg("Unable to Save Record.There is something wrong please try again."))
                                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                                                        } else {
                                                                            return responseInfoMsg("Unable to save Calender Period.Calender Period exceeds the Periods for given Calendar");
                                                                        }
                                                                    })
                                                            ));
                                                }
                                            } else {
                                                return responseInfoMsg("Adjustment Entry is not allowed for the Given year.");
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Calendar Does not exist."))
                                .onErrorResume(ex -> responseErrorMsg("Calendar Does not exist.Please Contact Developer."))
                        ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to Read Request.There is something wrong please try again."))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request. Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_calendar-period_period-no_show")
    public Mono<ServerResponse> showPeriodNo(ServerRequest serverRequest) {
        UUID calendarUUID = UUID.fromString(serverRequest.pathVariable("calendarUUID").trim());
        Integer periodNo = serverRequest.queryParam("periodNo").map(Integer::parseInt).orElse(null);

        return serverRequest.formData()
                .flatMap(value -> calendarPeriodsRepository.findAllByCalendarUUIDAndDeletedAtIsNull(calendarUUID)
                        .collectList()
                        .flatMap(calendarPeriods -> {
                            List<Integer> calenderPeriodNoList = new ArrayList<>();
                            for (CalendarPeriodEntity calenderPeriodEntity : calendarPeriods) {

                                if (periodNo.equals(calenderPeriodEntity.getPeriodNo())) {
                                    calenderPeriodNoList.add(calenderPeriodEntity.getPeriodNo());
                                }

                            }
                            // If periodNo already exist in any of records with given calendarId
                            if (!calenderPeriodNoList.isEmpty()) {
                                return responseInfoMsg("Period No Already Exists");
                            } else {
                                return responseInfoMsg("Unique Period No");
                            }
                        })
                ).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_calendar-period_period-no_list")
    public Mono<ServerResponse> showPeriodNoList(ServerRequest serverRequest) {
        UUID calendarUUID = UUID.fromString(serverRequest.pathVariable("calendarUUID").trim());

        return calendarRepository.findByUuidAndDeletedAtIsNull(calendarUUID)
                .flatMap(calendarEntity -> calendarTypesRepository.findByUuidAndDeletedAtIsNull(calendarEntity.getCalendarTypeUUID())
                        .flatMap(calendarTypesEntity ->
                                calendarPeriodsRepository.findAllByCalendarUUIDAndDeletedAtIsNull(calendarEntity.getUuid())
                                        .collectList()
                                        .flatMap(calendarPeriodEntities -> {
                                            List<Integer> result = new ArrayList<>();
                                            for (int i = 1; i < calendarTypesEntity.getPeriods() + 1; i++) {
                                                result.add(i);
                                            }
                                            List<Integer> calendarPeriodsList = new ArrayList<>();
                                            for (CalendarPeriodEntity calendarPeriodEntity : calendarPeriodEntities) {
                                                calendarPeriodsList.add(calendarPeriodEntity.getPeriodNo());
                                            }
                                            result.removeAll(calendarPeriodsList);
                                            return responseSuccessMsg("Records Fetched Successfully!", result);
                                        }).switchIfEmpty(responseInfoMsg("Unable to fetch records.There is something wrong."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to fetch records.Please Contact Developer."))
                        ).switchIfEmpty(responseInfoMsg("Calendar Type does not exist!"))
                        .onErrorResume(err -> responseErrorMsg("Calendar Type does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Calendar does not exist!"))
                .onErrorResume(err -> responseErrorMsg("Calendar does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_calendar-period_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID calendarPeriodUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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

        return calendarPeriodsRepository.findByUuidAndDeletedAtIsNull(calendarPeriodUUID)
                .flatMap(calendarPeriodEntity -> transactionRepository.findFirstByCalendarPeriodUUIDAndDeletedAtIsNull(calendarPeriodEntity.getUuid())
                        .flatMap(transactionCheckMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists."))
                        //Checks if Calendar Period Reference exists in Financial Transactions in Emp Financial Module
                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(empFinancialModuleUri + "api/v1/financial-transactions/calendar-period/show/", calendarPeriodEntity.getUuid())
                                .flatMap(jsonNode -> apiCallService.checkResponse(jsonNode)
                                        .flatMap(checkCalendarPeriodApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
                        //Checks if Calendar Period Reference exists in Financial Transactions in Student Financial Module
                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(studentFinancialModuleUri + "api/v1/financial-transactions/calendar-period/show/", calendarPeriodEntity.getUuid())
                                .flatMap(jsonNode -> apiCallService.checkResponse(jsonNode)
                                        .flatMap(checkCalendarPeriodApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
                        .switchIfEmpty(Mono.defer(() -> {

                            calendarPeriodEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            calendarPeriodEntity.setDeletedBy(UUID.fromString(userId));
                            calendarPeriodEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            calendarPeriodEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            calendarPeriodEntity.setReqDeletedIP(reqIp);
                            calendarPeriodEntity.setReqDeletedPort(reqPort);
                            calendarPeriodEntity.setReqDeletedBrowser(reqBrowser);
                            calendarPeriodEntity.setReqDeletedOS(reqOs);
                            calendarPeriodEntity.setReqDeletedDevice(reqDevice);
                            calendarPeriodEntity.setReqDeletedReferer(reqReferer);

                            return calendarPeriodsRepository.save(calendarPeriodEntity)
                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                    .switchIfEmpty(responseInfoMsg("Unable to Delete the Record.There is something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Delete the Record.Please Contact Developer."));
                        }))
                ).switchIfEmpty(responseInfoMsg("Record does not exist."))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_calendar-period_open_update")
    public Mono<ServerResponse> isOpen(ServerRequest serverRequest) {
        UUID calendarPeriodUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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
                return responseWarningMsg("Unknown User");
            }
        }
        return serverRequest.formData()
                .flatMap(value -> {
                    Boolean isOpen = Boolean.parseBoolean(value.getFirst("isOpen"));
                    return calendarPeriodsRepository.findByUuidAndDeletedAtIsNull(calendarPeriodUUID)
                            .flatMap(previousCalendarPeriod -> {
                                // If status is not Boolean value
                                if (isOpen != false && isOpen != true) {
                                    return responseInfoMsg("Is Open Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousCalendarPeriod.getIsOpen() ? true : false) == isOpen)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                CalendarPeriodEntity calendarPeriodEntity = CalendarPeriodEntity.builder()
                                        .name(previousCalendarPeriod.getName())
                                        .uuid(previousCalendarPeriod.getUuid())
                                        .prefix(previousCalendarPeriod.getPrefix())
                                        .description(previousCalendarPeriod.getDescription())
                                        .isOpen(isOpen == true ? true : false)
                                        .isOpenAuto(previousCalendarPeriod.getIsOpenAuto())
                                        .adjustments(previousCalendarPeriod.getAdjustments())
                                        .periodNo(previousCalendarPeriod.getPeriodNo())
                                        .quarter(previousCalendarPeriod.getQuarter())
                                        .calendarUUID(previousCalendarPeriod.getCalendarUUID())
                                        .startDate(previousCalendarPeriod.getStartDate())
                                        .endDate(previousCalendarPeriod.getEndDate())
                                        .createdAt(previousCalendarPeriod.getCreatedAt())
                                        .createdBy(previousCalendarPeriod.getCreatedBy())
                                        .updatedBy(UUID.fromString(userId))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousCalendarPeriod.getReqCreatedIP())
                                        .reqCreatedPort(previousCalendarPeriod.getReqCreatedPort())
                                        .reqCreatedBrowser(previousCalendarPeriod.getReqCreatedBrowser())
                                        .reqCreatedOS(previousCalendarPeriod.getReqCreatedOS())
                                        .reqCreatedDevice(previousCalendarPeriod.getReqCreatedDevice())
                                        .reqCreatedReferer(previousCalendarPeriod.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousCalendarPeriod.setDeletedBy(UUID.fromString(userId));
                                previousCalendarPeriod.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousCalendarPeriod.setReqDeletedIP(reqIp);
                                previousCalendarPeriod.setReqDeletedPort(reqPort);
                                previousCalendarPeriod.setReqDeletedBrowser(reqBrowser);
                                previousCalendarPeriod.setReqDeletedOS(reqOs);
                                previousCalendarPeriod.setReqDeletedDevice(reqDevice);
                                previousCalendarPeriod.setReqDeletedReferer(reqReferer);

                                return calendarPeriodsRepository.save(previousCalendarPeriod)
                                        .then(calendarPeriodsRepository.save(calendarPeriodEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Is Open Status updated successfully", statusUpdate))
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