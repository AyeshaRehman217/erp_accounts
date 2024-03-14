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
import tuf.webscaf.app.dbContext.master.entity.CalendarGroupCalendarPvtEntity;
import tuf.webscaf.app.dbContext.master.repository.CalendarGroupCalendarPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.CalendarGroupRepository;
import tuf.webscaf.app.dbContext.master.repository.CalendarRepository;
import tuf.webscaf.app.dbContext.slave.dto.SlaveCalendarGroupCalendarDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveCalendarGroupCalendarDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveCalendarGroupCalendarPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveCalendarGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveCalendarRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Tag(name = "calendarGroupCalendarPvtHandler")
public class CalendarGroupCalendarPvtHandler {

    @Autowired
    CalendarRepository calendarRepository;

    @Autowired
    SlaveCalendarRepository slaveCalendarRepository;

    @Autowired
    CalendarGroupCalendarPvtRepository calendarGroupCalendarPvtRepository;

    @Autowired
    SlaveCalendarGroupCalendarPvtRepository slaveCalendarGroupCalendarPvtRepository;

    @Autowired
    CalendarGroupRepository calendarGroupRepository;

    @Autowired
    SlaveCalendarGroupRepository slaveCalendarGroupRepository;

    @Autowired
    CustomResponse appResponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_calendar-group-calendar_un-mapped_show")
    public Mono<ServerResponse> showUnMappedCalendarsAgainstCalendarGroup(ServerRequest serverRequest) {

        UUID calendarGroupUUID = UUID.fromString(serverRequest.pathVariable("calendarGroupUUID").trim());

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;

        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));


        if (!status.isEmpty()) {
            Flux<SlaveCalendarEntity> slaveCalendarEntityFluxWithStatus = slaveCalendarRepository
                    .showUnMappedCalendarListWithStatusFilter(calendarGroupUUID, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveCalendarEntityFluxWithStatus
                    .collectList()
                    .flatMap(calendarEntityDB -> slaveCalendarRepository.countUnMappedCalendarRecordsWithStatusFilter(calendarGroupUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                        if (calendarEntityDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records fetched successfully!", calendarEntityDB, count);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        } else {
            Flux<SlaveCalendarEntity> slaveCalendarEntityFlux = slaveCalendarRepository
                    .showUnMappedCalendarList(calendarGroupUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveCalendarEntityFlux
                    .collectList()
                    .flatMap(calendarEntityDB -> slaveCalendarRepository.countUnMappedCalendarRecords(calendarGroupUUID, searchKeyWord)
                            .flatMap(count -> {
                                        if (calendarEntityDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records fetched successfully!", calendarEntityDB, count);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        }

    }

    @AuthHasPermission(value = "account_api_v1_calendar-group-calendar_mapped_show")
    // show Calendars for Calendar Group Id
    public Mono<ServerResponse> showMappedCalendarsAgainstCalendarGroup(ServerRequest serverRequest) {
        UUID calendarGroupUUID = UUID.fromString(serverRequest.pathVariable("calendarGroupUUID").trim());

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        Pageable pageable = PageRequest.of(page, size);

        return slaveCalendarGroupRepository.findByUuidAndDeletedAtIsNull(calendarGroupUUID)
                // if calendar group is mapped with all
                .flatMap(calendarGroupEntity -> slaveCalendarGroupCalendarPvtRepository.findByCalendarGroupUUIDAndAllAndDeletedAtIsNull(calendarGroupUUID, true)
                        .flatMap(allCalendarsMapped -> {

                            SlaveCalendarGroupCalendarDto slaveCalendarGroupCalendarDto = SlaveCalendarGroupCalendarDto.builder()
                                    .id(allCalendarsMapped.getId())
                                    .version(allCalendarsMapped.getVersion())
                                    .uuid(allCalendarsMapped.getUuid())
                                    .name("all")
                                    .calendarGroupName(calendarGroupEntity.getName())
                                    .calendarGroupUUID(allCalendarsMapped.getCalendarGroupUUID())
                                    .all(allCalendarsMapped.getAll())
                                    .createdAt(allCalendarsMapped.getCreatedAt())
                                    .createdBy(allCalendarsMapped.getCreatedBy())
                                    .updatedBy(allCalendarsMapped.getUpdatedBy())
                                    .updatedAt(allCalendarsMapped.getUpdatedAt())
                                    .reqCompanyUUID(allCalendarsMapped.getReqCompanyUUID())
                                    .reqBranchUUID(allCalendarsMapped.getReqBranchUUID())
                                    .reqCreatedIP(allCalendarsMapped.getReqCreatedIP())
                                    .reqCreatedPort(allCalendarsMapped.getReqCreatedPort())
                                    .reqCreatedBrowser(allCalendarsMapped.getReqCreatedBrowser())
                                    .reqCreatedOS(allCalendarsMapped.getReqCreatedOS())
                                    .reqCreatedDevice(allCalendarsMapped.getReqCreatedDevice())
                                    .reqCreatedReferer(allCalendarsMapped.getReqCreatedReferer())
                                    .reqUpdatedIP(allCalendarsMapped.getReqUpdatedIP())
                                    .reqUpdatedPort(allCalendarsMapped.getReqUpdatedPort())
                                    .reqUpdatedBrowser(allCalendarsMapped.getReqUpdatedBrowser())
                                    .reqUpdatedOS(allCalendarsMapped.getReqUpdatedOS())
                                    .reqUpdatedDevice(allCalendarsMapped.getReqUpdatedDevice())
                                    .reqUpdatedReferer(allCalendarsMapped.getReqUpdatedReferer())
                                    .build();

                            // if status is given in query parameter
                            if (!status.isEmpty()) {
                                return slaveCalendarRepository
                                        .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                                        .flatMap(count -> responseIndexSuccessMsg("All Records Fetched Successfully", slaveCalendarGroupCalendarDto, count))
                                        .switchIfEmpty(responseErrorMsg("Record does not exist"))
                                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                            }

                            // if status is not given
                            else {
                                return slaveCalendarRepository
                                        .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                                        .flatMap(count -> responseIndexSuccessMsg("All Records Fetched Successfully", slaveCalendarGroupCalendarDto, count))
                                        .switchIfEmpty(responseErrorMsg("Record does not exist"))
                                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                            }
                        })

                        // get only mapped records from pvt
                        .switchIfEmpty(Mono.defer(() -> {

                            // if status is given in query parameter
                            if (!status.isEmpty()) {
                                Flux<SlaveCalendarEntity> slaveCalendarEntityFlux = slaveCalendarRepository
                                        .showMappedCalendarsAgainstCalendarGroupWithFilter(calendarGroupUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
                                return slaveCalendarEntityFlux
                                        .collectList()
                                        .flatMap(calendarEntity -> slaveCalendarRepository.countCalendarsAgainstCalendarGroupWithStatusFilter(calendarGroupUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                                                .flatMap(count -> {
                                                    if (calendarEntity.isEmpty()) {
                                                        return responseIndexInfoMsg("Record does not exist", count);

                                                    } else {

                                                        return responseIndexSuccessMsg("All Records Fetched Successfully", calendarEntity, count);
                                                    }
                                                })
                                        ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                        .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                            }

                            // if status is not given
                            else {

                                Flux<SlaveCalendarEntity> slaveCalendarEntityFlux = slaveCalendarRepository
                                        .showMappedCalendarsAgainstCalendarGroup(calendarGroupUUID, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

                                return slaveCalendarEntityFlux
                                        .collectList()
                                        .flatMap(calendarEntity -> slaveCalendarRepository.countCalendarsAgainstCalendarGroup(calendarGroupUUID, searchKeyWord, searchKeyWord)
                                                .flatMap(count -> {
                                                    if (calendarEntity.isEmpty()) {
                                                        return responseIndexInfoMsg("Record does not exist", count);

                                                    } else {

                                                        return responseIndexSuccessMsg("All Records Fetched Successfully", calendarEntity, count);
                                                    }
                                                })
                                        ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                        .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));

                            }
                        }))
                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));

    }

    @AuthHasPermission(value = "account_api_v1_calendar-group-calendar_store")
    public Mono<ServerResponse> store(ServerRequest serverRequest) {

        String userId = serverRequest.headers().firstHeader("auid");
        final UUID calendarGroupUUID = UUID.fromString(serverRequest.pathVariable("calendarGroupUUID").trim());
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
                .flatMap(value -> calendarGroupRepository.findByUuidAndDeletedAtIsNull(calendarGroupUUID)
                                .flatMap(calendarGroupEntity -> {

//                            // if given calendar group is inactive
//                            if (!calendarGroupEntity.getStatus()) {
//                                return responseInfoMsg("Calendar Group status is inactive");
//                            }

                                    Boolean all = Boolean.valueOf(value.getFirst("all"));

                                    // mapping with all records
                                    if (all) {

                                        CalendarGroupCalendarPvtEntity calendarGroupCalendarPvtEntity = CalendarGroupCalendarPvtEntity.builder()
                                                .uuid(UUID.randomUUID())
                                                .calendarGroupUUID(calendarGroupUUID)
                                                .all(all)
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

                                        return calendarGroupCalendarPvtRepository.findFirstByCalendarGroupUUIDAndAllAndDeletedAtIsNull(calendarGroupUUID, true)
                                                .flatMap(recordAlreadyExists -> responseInfoMsg("Record Already Exists"))
                                                .switchIfEmpty(Mono.defer(() -> calendarGroupCalendarPvtRepository.findAllByCalendarGroupUUIDAndDeletedAtIsNull(calendarGroupUUID)
                                                        .collectList()
                                                        .flatMap(previouslyMappedCalendars -> {

                                                            for (CalendarGroupCalendarPvtEntity pvtEntity : previouslyMappedCalendars) {
                                                                pvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                                                pvtEntity.setDeletedBy(UUID.fromString(userId));
                                                                pvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                                                pvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                                                pvtEntity.setReqDeletedIP(reqIp);
                                                                pvtEntity.setReqDeletedPort(reqPort);
                                                                pvtEntity.setReqDeletedBrowser(reqBrowser);
                                                                pvtEntity.setReqDeletedOS(reqOs);
                                                                pvtEntity.setReqDeletedDevice(reqDevice);
                                                                pvtEntity.setReqDeletedReferer(reqReferer);
                                                            }

                                                            return calendarGroupCalendarPvtRepository.saveAll(previouslyMappedCalendars)
                                                                    .then(calendarGroupCalendarPvtRepository.save(calendarGroupCalendarPvtEntity))
                                                                    .flatMap(allCalendarsMapped -> responseSuccessMsg("All Calendars Are Mapped Successfully With Given Calendar Group", all))
                                                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please try again."))
                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                                        })
                                                ));
                                    }

                                    // if all is not selected
                                    else {

                                        //getting List of Calendars From Front
                                        List<String> listOfCalendarUUID = value.get("calendarUUID");

                                        listOfCalendarUUID.removeIf(s -> s.equals(""));

                                        List<UUID> l_list = new ArrayList<>();
                                        for (String getCalendarUUID : listOfCalendarUUID) {
                                            l_list.add(UUID.fromString(getCalendarUUID));
                                        }

                                        if (!l_list.isEmpty()) {
                                            return calendarRepository.findAllByUuidInAndDeletedAtIsNull(l_list)
                                                    .collectList()
                                                    .flatMap(existingCalendars -> {
                                                        // Calendar UUID List
                                                        List<UUID> calendarList = new ArrayList<>();

                                                        for (CalendarEntity calendar : existingCalendars) {
                                                            calendarList.add(calendar.getUuid());
                                                        }

                                                        if (!calendarList.isEmpty()) {

                                                            // calendar uuid list to show in response
                                                            List<UUID> calendarRecords = new ArrayList<>(calendarList);

                                                            List<CalendarGroupCalendarPvtEntity> listPvt = new ArrayList<>();

                                                            return calendarGroupCalendarPvtRepository.findAllByCalendarGroupUUIDAndCalendarUUIDInAndDeletedAtIsNull(calendarGroupUUID, calendarList)
                                                                    .collectList()
                                                                    .flatMap(calendarGroupPvtEntity -> {
                                                                        for (CalendarGroupCalendarPvtEntity pvtEntity : calendarGroupPvtEntity) {
                                                                            //Removing Existing Calendar UUID in Calendar Final List to be saved that does not contain already mapped values
                                                                            calendarList.remove(pvtEntity.getCalendarUUID());
                                                                        }

                                                                        // iterate Calendar UUIDs for given Calendar Group
                                                                        for (UUID calendarUUID : calendarList) {
                                                                            CalendarGroupCalendarPvtEntity calendarGroupCalendarPvtEntity = CalendarGroupCalendarPvtEntity
                                                                                    .builder()
                                                                                    .calendarUUID(calendarUUID)
                                                                                    .uuid(UUID.randomUUID())
                                                                                    .calendarGroupUUID(calendarGroupUUID)
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
                                                                            listPvt.add(calendarGroupCalendarPvtEntity);
                                                                        }

                                                                        return calendarGroupCalendarPvtRepository.saveAll(listPvt)
                                                                                .collectList()
                                                                                .flatMap(groupList -> {

                                                                                    if (!calendarList.isEmpty()) {
                                                                                        return responseSuccessMsg("Record Stored Successfully", calendarRecords)
                                                                                                .switchIfEmpty(responseInfoMsg("Unable to Store Record. There is something wrong please try again."))
                                                                                                .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                                                                    } else {
                                                                                        return responseInfoMsg("Record Already Exists", calendarRecords);
                                                                                    }

                                                                                }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                                                .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                                                    });
                                                        } else {
                                                            return responseInfoMsg("Calendar Record does not exist");
                                                        }
                                                    }).switchIfEmpty(responseInfoMsg("The Entered Calendar Does not exist."))
                                                    .onErrorResume(ex -> responseErrorMsg("The Entered Calendar Does not exist.Please Contact Developer."));
                                        } else {
                                            return responseInfoMsg("Select Calendar First");
                                        }

                                    }
                                }).switchIfEmpty(responseInfoMsg("Calendar Group Record does not exist"))
                                .onErrorResume(err -> responseInfoMsg("Calendar Group Record does not exist. Please contact developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer."));
    }

    @AuthHasPermission(value = "account_api_v1_calendar-group-calendar_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        UUID calendarGroupUUID = UUID.fromString(serverRequest.pathVariable("calendarGroupUUID").trim());

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

                    Boolean all = Boolean.valueOf(value.getFirst("all"));

                    // if all is given
                    if (all) {
                        return calendarGroupRepository.findByUuidAndDeletedAtIsNull(calendarGroupUUID)
                                .flatMap(calendarGroupEntity -> calendarGroupCalendarPvtRepository.findFirstByCalendarGroupUUIDAndAllAndDeletedAtIsNull(calendarGroupUUID, true)
                                        .flatMap(calendarGroupCalendarPvtEntity -> {

                                            SlaveCalendarGroupCalendarDto slaveCalendarGroupCalendarDto = SlaveCalendarGroupCalendarDto.builder()
                                                    .id(calendarGroupCalendarPvtEntity.getId())
                                                    .version(calendarGroupCalendarPvtEntity.getVersion())
                                                    .uuid(calendarGroupCalendarPvtEntity.getUuid())
                                                    .name("all")
                                                    .calendarGroupName(calendarGroupEntity.getName())
                                                    .calendarGroupUUID(calendarGroupCalendarPvtEntity.getCalendarGroupUUID())
                                                    .all(calendarGroupCalendarPvtEntity.getAll())
                                                    .createdAt(calendarGroupCalendarPvtEntity.getCreatedAt())
                                                    .createdBy(calendarGroupCalendarPvtEntity.getCreatedBy())
                                                    .updatedBy(calendarGroupCalendarPvtEntity.getUpdatedBy())
                                                    .updatedAt(calendarGroupCalendarPvtEntity.getUpdatedAt())
                                                    .reqCompanyUUID(calendarGroupCalendarPvtEntity.getReqCompanyUUID())
                                                    .reqBranchUUID(calendarGroupCalendarPvtEntity.getReqBranchUUID())
                                                    .reqCreatedIP(calendarGroupCalendarPvtEntity.getReqCreatedIP())
                                                    .reqCreatedPort(calendarGroupCalendarPvtEntity.getReqCreatedPort())
                                                    .reqCreatedBrowser(calendarGroupCalendarPvtEntity.getReqCreatedBrowser())
                                                    .reqCreatedOS(calendarGroupCalendarPvtEntity.getReqCreatedOS())
                                                    .reqCreatedDevice(calendarGroupCalendarPvtEntity.getReqCreatedDevice())
                                                    .reqCreatedReferer(calendarGroupCalendarPvtEntity.getReqCreatedReferer())
                                                    .reqUpdatedIP(calendarGroupCalendarPvtEntity.getReqUpdatedIP())
                                                    .reqUpdatedPort(calendarGroupCalendarPvtEntity.getReqUpdatedPort())
                                                    .reqUpdatedBrowser(calendarGroupCalendarPvtEntity.getReqUpdatedBrowser())
                                                    .reqUpdatedOS(calendarGroupCalendarPvtEntity.getReqUpdatedOS())
                                                    .reqUpdatedDevice(calendarGroupCalendarPvtEntity.getReqUpdatedDevice())
                                                    .reqUpdatedReferer(calendarGroupCalendarPvtEntity.getReqUpdatedReferer())
                                                    .build();

                                            calendarGroupCalendarPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                            calendarGroupCalendarPvtEntity.setDeletedBy(UUID.fromString(userId));
                                            calendarGroupCalendarPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                            calendarGroupCalendarPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                            calendarGroupCalendarPvtEntity.setReqDeletedIP(reqIp);
                                            calendarGroupCalendarPvtEntity.setReqDeletedPort(reqPort);
                                            calendarGroupCalendarPvtEntity.setReqDeletedBrowser(reqBrowser);
                                            calendarGroupCalendarPvtEntity.setReqDeletedOS(reqOs);
                                            calendarGroupCalendarPvtEntity.setReqDeletedDevice(reqDevice);
                                            calendarGroupCalendarPvtEntity.setReqDeletedReferer(reqReferer);

                                            return calendarGroupCalendarPvtRepository.save(calendarGroupCalendarPvtEntity)
                                                    .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", slaveCalendarGroupCalendarDto))
                                                    .switchIfEmpty(responseInfoMsg("Unable to delete the record.There is something wrong please try again"))
                                                    .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer."));
                                        })
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                    }

                    // if all is not given
                    else {
                        return calendarRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(value.getFirst("calendarUUID").trim()))
                                .flatMap(calendarEntity -> calendarGroupCalendarPvtRepository.findFirstByCalendarGroupUUIDAndCalendarUUIDAndDeletedAtIsNull(calendarGroupUUID, calendarEntity.getUuid())
                                        .flatMap(calendarGroupCalendarPvtEntity -> {

                                            calendarGroupCalendarPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                            calendarGroupCalendarPvtEntity.setDeletedBy(UUID.fromString(userId));
                                            calendarGroupCalendarPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                            calendarGroupCalendarPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                            calendarGroupCalendarPvtEntity.setReqDeletedIP(reqIp);
                                            calendarGroupCalendarPvtEntity.setReqDeletedPort(reqPort);
                                            calendarGroupCalendarPvtEntity.setReqDeletedBrowser(reqBrowser);
                                            calendarGroupCalendarPvtEntity.setReqDeletedOS(reqOs);
                                            calendarGroupCalendarPvtEntity.setReqDeletedDevice(reqDevice);
                                            calendarGroupCalendarPvtEntity.setReqDeletedReferer(reqReferer);

                                            return calendarGroupCalendarPvtRepository.save(calendarGroupCalendarPvtEntity)
                                                    .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", calendarEntity))
                                                    .switchIfEmpty(responseInfoMsg("Unable to delete the record.There is something wrong please try again!"))
                                                    .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer."));
                                        })
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                    }

                }).switchIfEmpty(responseInfoMsg("Unable to read request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));

    }

    public Mono<ServerResponse> responseErrorMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.ERROR,
                        msg
                )
        );

        return appResponse.set(
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

        return appResponse.set(
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


    public Mono<ServerResponse> responseInfoMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.INFO,
                        msg
                )
        );

        return appResponse.set(
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

    public Mono<ServerResponse> responseInfoMsg(String msg, Object entity) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.INFO,
                        msg
                )
        );

        return appResponse.set(
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


        return appResponse.set(
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

        return appResponse.set(
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


        return appResponse.set(
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
