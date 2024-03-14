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
import tuf.webscaf.app.dbContext.master.entity.ProfitCenterEntity;
import tuf.webscaf.app.dbContext.master.repository.LedgerEntryRepository;
import tuf.webscaf.app.dbContext.master.repository.ProfitCenterGroupProfitCenterPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.ProfitCenterRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherProfitCenterGroupPvtRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveLedgerEntryRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveProfitCenterRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherProfitCenterGroupPvtRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@Tag(name = "profitCenterHandler")
public class ProfitCenterHandler {
    @Value("${server.erp_config_module.uri}")
    private String configUri;

    @Autowired
    ProfitCenterRepository profitCenterRepository;

    @Autowired
    SlaveProfitCenterRepository slaveProfitCenterRepository;

    @Autowired
    SlaveLedgerEntryRepository slaveLedgerEntryRepository;

    @Autowired
    ProfitCenterGroupProfitCenterPvtRepository profitCenterGroupProfitCenterPvtRepository;

    @Autowired
    SlaveVoucherProfitCenterGroupPvtRepository slaveVoucherProfitCenterGroupPvtRepository;

    @Autowired
    LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_profit-centers_index")
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
        switch (directionProperty.toLowerCase()) {
            case "id":
                directionProperty = "id";
                break;
            case "name":
                directionProperty = "name";
                break;
            case "description":
                directionProperty = "description";
                break;
            default:
                directionProperty = "id";
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        if (!status.isEmpty()) {
            Flux<SlaveProfitCenterEntity> slaveProfitCenterEntityFlux = slaveProfitCenterRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));

            return slaveProfitCenterEntityFlux
                    .collectList()
                    .flatMap(profitCenterEntity -> slaveProfitCenterRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (profitCenterEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", profitCenterEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        } else {
            Flux<SlaveProfitCenterEntity> slaveProfitCenterEntityFlux = slaveProfitCenterRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);

            return slaveProfitCenterEntityFlux
                    .collectList()
                    .flatMap(profitCenterEntity -> slaveProfitCenterRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (profitCenterEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", profitCenterEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_profit-centers_active_index")
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
        switch (directionProperty.toLowerCase()) {
            case "id":
                directionProperty = "id";
                break;
            case "name":
                directionProperty = "name";
                break;
            case "description":
                directionProperty = "description";
                break;
            default:
                directionProperty = "id";
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        Flux<SlaveProfitCenterEntity> slaveProfitCenterEntityFlux = slaveProfitCenterRepository
                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE);

        return slaveProfitCenterEntityFlux
                .collectList()
                .flatMap(profitCenterEntity -> slaveProfitCenterRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE)
                        .flatMap(count ->
                        {
                            if (profitCenterEntity.isEmpty()) {
                                return responseIndexInfoMsg("Record does not exist", count);
                            } else {
                                return responseIndexSuccessMsg("All Records Fetched Successfully", profitCenterEntity, count);
                            }
                        })
                ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));

    }

    @AuthHasPermission(value = "account_api_v1_profit-centers_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID profitCenterUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveProfitCenterRepository.findByUuidAndDeletedAtIsNull(profitCenterUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist."))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    //Check Branch id In Config Module
    @AuthHasPermission(value = "account_api_v1_profit-centers_branch_show")
    public Mono<ServerResponse> getBranchUUID(ServerRequest serverRequest) {
        UUID branchUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return serverRequest.formData()
                .flatMap(value -> slaveProfitCenterRepository.findFirstByBranchUUIDAndDeletedAtIsNull(branchUUID)
                        .flatMap(value1 -> responseInfoMsg("Unable to Delete Record as the Reference Exists."))
                ).switchIfEmpty(responseErrorMsg("Record Does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist"));
    }

    //Check Company UUID In Config Module
    @AuthHasPermission(value = "account_api_v1_profit-centers_company_show")
    public Mono<ServerResponse> getCompanyUUID(ServerRequest serverRequest) {
        UUID companyUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return serverRequest.formData()
                .flatMap(value -> slaveProfitCenterRepository.findFirstByCompanyUUIDAndDeletedAtIsNull(companyUUID)
                        .flatMap(value1 -> responseInfoMsg("Unable to Delete Record as the Reference Exists."))
                ).switchIfEmpty(responseErrorMsg("Record Does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist"));
    }

    //Show Profit Centers for Voucher Id
    @AuthHasPermission(value = "account_api_v1_profit-centers_voucher_show")
    public Mono<ServerResponse> showVoucherWithProfitCenters(ServerRequest serverRequest) {

        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());

        // Optional Query Parameter Based of Status
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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();


        return slaveVoucherProfitCenterGroupPvtRepository.profitCenterGroupAllMappingExists(voucherUUID)
                .flatMap(all -> {

                    // if voucher is mapped with profit center group of all profit centers
                    if(all){

                        // if status is given in query parameter
                        if (!status.isEmpty()) {
                            Flux<SlaveProfitCenterEntity> slaveProfitCenterEntityFlux = slaveProfitCenterRepository
                                    .indexProfitCenterWithStatus(searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

                            return slaveProfitCenterEntityFlux
                                    .collectList()
                                    .flatMap(profitCenterEntity -> slaveProfitCenterRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                                            .flatMap(count ->
                                            {
                                                if (profitCenterEntity.isEmpty()) {
                                                    return responseIndexInfoMsg("Record does not exist", count);
                                                } else {
                                                    return responseIndexSuccessMsg("All Records Fetched Successfully", profitCenterEntity, count);
                                                }
                                            })
                                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
                        }

                        // if status is not given
                        else {
                            Flux<SlaveProfitCenterEntity> slaveProfitCenterEntityFlux = slaveProfitCenterRepository
                                    .indexProfitCenter(searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

                            return slaveProfitCenterEntityFlux
                                    .collectList()
                                    .flatMap(profitCenterEntity -> slaveProfitCenterRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                                            .flatMap(count ->
                                            {
                                                if (profitCenterEntity.isEmpty()) {
                                                    return responseIndexInfoMsg("Record does not exist", count);
                                                } else {
                                                    return responseIndexSuccessMsg("All Records Fetched Successfully", profitCenterEntity, count);
                                                }
                                            })
                                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
                        }
                    }

                    // if voucher is not mapped with profit center group of all profit centers
                    else {

                        // if status is given in query parameter
                        if (!status.isEmpty()) {
                            Flux<SlaveProfitCenterEntity> slaveProfitCenterEntityFlux = slaveProfitCenterRepository
                                    .showProfitCenterListWithStatus(voucherUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());
                            return slaveProfitCenterEntityFlux
                                    .collectList()
                                    .flatMap(profitCenterEntity -> slaveProfitCenterRepository
                                            .countRecordsWithSearchFilterWithStatus(voucherUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                                            .flatMap(count -> {
                                                if (profitCenterEntity.isEmpty()) {
                                                    return responseInfoMsg("Record does not exist");
                                                } else {
                                                    return responseIndexSuccessMsg("All Records Fetched Successfully", profitCenterEntity, count);
                                                }
                                            })
                                    ).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
                        }

                        // if status is not given
                        else {
                            Flux<SlaveProfitCenterEntity> slaveProfitCenterEntityFlux = slaveProfitCenterRepository
                                    .showProfitCenterList(voucherUUID, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
                            return slaveProfitCenterEntityFlux
                                    .collectList()
                                    .flatMap(profitCenterEntity -> slaveProfitCenterRepository
                                            .countRecordsWithSearchFilter(voucherUUID, searchKeyWord, searchKeyWord)
                                            .flatMap(count -> {
                                                if (profitCenterEntity.isEmpty()) {
                                                    return responseInfoMsg("Record does not exist");
                                                } else {
                                                    return responseIndexSuccessMsg("All Records Fetched Successfully", profitCenterEntity, count);
                                                }
                                            })
                                    ).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
                        }

                    }
                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    //show Profit Centers for Voucher With given Company UUID
    @AuthHasPermission(value = "account_api_v1_profit-centers_voucher_company_show")
    public Mono<ServerResponse> showProfitCentersWithCompany(ServerRequest serverRequest) {

        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());

        UUID companyUUID = UUID.fromString(serverRequest.queryParam("companyUUID").map(String::toString).orElse(""));

        // Optional Query Parameter Based of Status
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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        if (!status.isEmpty()) {
            Flux<SlaveProfitCenterEntity> slaveProfitCenterEntityFlux = slaveProfitCenterRepository
                    .showProfitCenterWithCompanyWithStatus(voucherUUID, companyUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveProfitCenterEntityFlux
                    .collectList()
                    .flatMap(accountEntity -> slaveProfitCenterRepository
                            .countProfitCenterWithCompanyWithStatus(voucherUUID, companyUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (accountEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully!", accountEntity, count);
                                }
                            })).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
        } else {
            Flux<SlaveProfitCenterEntity> slaveProfitCenterEntityFlux = slaveProfitCenterRepository
                    .showProfitCenterWithCompany(voucherUUID, companyUUID, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveProfitCenterEntityFlux
                    .collectList()
                    .flatMap(accountEntity -> slaveProfitCenterRepository
                            .countProfitCenterWithCompany(voucherUUID, companyUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (accountEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully!", accountEntity, count);
                                }
                            })).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
        }
    }

    //show Profit Centers for Voucher With given Branch UUID
    @AuthHasPermission(value = "account_api_v1_profit-centers_voucher_branch_show")
    public Mono<ServerResponse> showProfitCentersWithBranch(ServerRequest serverRequest) {

        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());

        UUID branchUUID = UUID.fromString(serverRequest.queryParam("branchUUID").map(String::toString).orElse(""));

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        // if status is present in query parameter
        if (!status.isEmpty()) {
            Flux<SlaveProfitCenterEntity> slaveProfitCenterEntityFlux = slaveProfitCenterRepository
                    .showProfitCenterWithBranchWithStatus(voucherUUID, branchUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveProfitCenterEntityFlux
                    .collectList()
                    .flatMap(accountEntity -> slaveProfitCenterRepository
                            .countProfitCenterWithBranchWithStatus(voucherUUID, branchUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (accountEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                                }
                            })).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
        }

        // if status is not present
        else {
            Flux<SlaveProfitCenterEntity> slaveProfitCenterEntityFlux = slaveProfitCenterRepository
                    .showProfitCenterWithBranch(voucherUUID, branchUUID, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveProfitCenterEntityFlux
                    .collectList()
                    .flatMap(accountEntity -> slaveProfitCenterRepository
                            .countProfitCenterWithBranch(voucherUUID, branchUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (accountEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                                }
                            })).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_profit-centers_store")
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

                    ProfitCenterEntity profitCenterEntity = ProfitCenterEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .description(value.getFirst("description").trim())
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .companyUUID(UUID.fromString(reqCompanyUUID))
                            .branchUUID(UUID.fromString(reqCompanyUUID))
                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                            .createdBy(UUID.fromString(userId))
                            .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                            .reqBranchUUID(UUID.fromString(reqBranchUUID))
                            .reqCreatedIP(reqIp)
                            .reqCreatedPort(reqPort)
                            .reqCreatedBrowser(reqBrowser)
                            .reqCreatedOS(reqOs)
                            .reqCreatedDevice(reqDevice)
                            .reqCreatedReferer(reqReferer)
                            .build();

                    return profitCenterRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(profitCenterEntity.getName())
                            .flatMap(nameAlreadyExists -> responseInfoMsg("Name Already Exists"))
                            .switchIfEmpty(Mono.defer(() -> profitCenterRepository.save(profitCenterEntity)
                                    .flatMap(value1 -> responseSuccessMsg("Record Stored Successfully", value1))
                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is Something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_profit-centers_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID profitCenterUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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
                .flatMap(value -> profitCenterRepository.findByUuidAndDeletedAtIsNull(profitCenterUUID)
                        .flatMap(previousEntity -> {

                            ProfitCenterEntity updatedProfitCenterEntity = ProfitCenterEntity.builder()
                                    .uuid(previousEntity.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .companyUUID(UUID.fromString(reqCompanyUUID))
                                    .branchUUID(UUID.fromString(reqCompanyUUID))
                                    .createdAt(previousEntity.getCreatedAt())
                                    .createdBy(previousEntity.getCreatedBy())
                                    .updatedBy(UUID.fromString(userId))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousEntity.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousEntity.setDeletedBy(UUID.fromString(userId));
                            previousEntity.setReqDeletedIP(reqIp);
                            previousEntity.setReqDeletedPort(reqPort);
                            previousEntity.setReqDeletedBrowser(reqBrowser);
                            previousEntity.setReqDeletedOS(reqOs);
                            previousEntity.setReqDeletedDevice(reqDevice);
                            previousEntity.setReqDeletedReferer(reqReferer);

                            return profitCenterRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(value.getFirst("name").trim(), previousEntity.getUuid())
                                    .flatMap(nameAlreadyExists -> responseInfoMsg("Name Already Exists"))
                                    .switchIfEmpty(Mono.defer(() -> profitCenterRepository.save(previousEntity)
                                            .then(profitCenterRepository.save(updatedProfitCenterEntity))
                                            .flatMap(value1 -> responseSuccessMsg("Record Updated Successfully!", value1))
                                            .switchIfEmpty(responseInfoMsg("Unable to update record.There is something wrong please try again."))
                                            .onErrorResume(err -> responseErrorMsg("Unable to Update record.Please Contact Developer."))
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist."))
                        .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_profit-centers_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        final UUID profitCenterUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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
        return profitCenterRepository.findByUuidAndDeletedAtIsNull(profitCenterUUID)
                .flatMap(entity -> ledgerEntryRepository.findFirstByProfitCenterUUIDAndDeletedAtIsNull(entity.getUuid())
                        .flatMap(ledgerEntry -> responseInfoMsg("Unable to Delete Record as the Reference Exists."))
                        .switchIfEmpty(Mono.defer(() -> profitCenterGroupProfitCenterPvtRepository.findFirstByProfitCenterUUIDAndDeletedAtIsNull(entity.getUuid())
                                .flatMap(PvtMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists."))))
                        .switchIfEmpty(Mono.defer(() -> {

                            entity.setDeletedBy(UUID.fromString(userId));
                            entity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            entity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            entity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            entity.setReqDeletedIP(reqIp);
                            entity.setReqDeletedPort(reqPort);
                            entity.setReqDeletedBrowser(reqBrowser);
                            entity.setReqDeletedOS(reqOs);
                            entity.setReqDeletedDevice(reqDevice);
                            entity.setReqDeletedReferer(reqReferer);

                            return profitCenterRepository.save(entity)
                                    .flatMap(docType -> responseSuccessMsg("Record Deleted Successfully", entity))
                                    .switchIfEmpty(responseInfoMsg("Unable to Delete record.There is something wrong please try again"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Delete record.Please Contact Developer."));
                        }))

                ).switchIfEmpty(responseInfoMsg("The Requested Record does not Exist."))
                .onErrorResume(ex -> responseErrorMsg("The Requested Record does not Exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_profit-centers_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID profitCenterUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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
                    return profitCenterRepository.findByUuidAndDeletedAtIsNull(profitCenterUUID)
                            .flatMap(previousProfitCenter -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousProfitCenter.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                ProfitCenterEntity updatedProfitCenterEntity = ProfitCenterEntity.builder()
                                        .uuid(previousProfitCenter.getUuid())
                                        .name(previousProfitCenter.getName())
                                        .description(previousProfitCenter.getDescription())
                                        .status(status == true ? true : false)
                                        .companyUUID(previousProfitCenter.getCompanyUUID())
                                        .branchUUID(previousProfitCenter.getBranchUUID())
                                        .createdAt(previousProfitCenter.getCreatedAt())
                                        .createdBy(previousProfitCenter.getCreatedBy())
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .updatedBy(UUID.fromString(userId))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousProfitCenter.getReqCreatedIP())
                                        .reqCreatedPort(previousProfitCenter.getReqCreatedPort())
                                        .reqCreatedBrowser(previousProfitCenter.getReqCreatedBrowser())
                                        .reqCreatedOS(previousProfitCenter.getReqCreatedOS())
                                        .reqCreatedDevice(previousProfitCenter.getReqCreatedDevice())
                                        .reqCreatedReferer(previousProfitCenter.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousProfitCenter.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousProfitCenter.setDeletedBy(UUID.fromString(userId));
                                previousProfitCenter.setReqDeletedIP(reqIp);
                                previousProfitCenter.setReqDeletedPort(reqPort);
                                previousProfitCenter.setReqDeletedBrowser(reqBrowser);
                                previousProfitCenter.setReqDeletedOS(reqOs);
                                previousProfitCenter.setReqDeletedDevice(reqDevice);
                                previousProfitCenter.setReqDeletedReferer(reqReferer);

                                return profitCenterRepository.save(previousProfitCenter)
                                        .then(profitCenterRepository.save(updatedProfitCenterEntity))
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

    public Mono<ServerResponse> responseSuccessMsg(String msg, Object entity) {
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
                0L,
                0L,
                messages,
                Mono.just(entity)
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
