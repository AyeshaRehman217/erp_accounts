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
import tuf.webscaf.app.dbContext.master.entity.CostCenterEntity;
import tuf.webscaf.app.dbContext.master.repository.CostCenterGroupCostCenterPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.CostCenterRepository;
import tuf.webscaf.app.dbContext.master.repository.LedgerEntryRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCostCenterEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveCostCenterRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherCostCenterGroupPvtRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@Tag(name = "costCenterHandler")
public class CostCenterHandler {
    @Value("${server.erp_config_module.uri}")
    private String configUri;

    @Autowired
    CostCenterRepository costCenterRepository;

    @Autowired
    CostCenterGroupCostCenterPvtRepository costCenterGroupCostCenterPvtRepository;

    @Autowired
    SlaveVoucherCostCenterGroupPvtRepository slaveVoucherCostCenterGroupPvtRepository;

    @Autowired
    SlaveCostCenterRepository slaveCostCenterRepository;

    @Autowired
    LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    ApiCallService apiCallService;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_cost-centers_index")
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
            Flux<SlaveCostCenterEntity> slaveCostCenterEntityFlux = slaveCostCenterRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));
            return slaveCostCenterEntityFlux
                    .collectList()
                    .flatMap(costCenterEntity -> slaveCostCenterRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (costCenterEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records fetched successfully", costCenterEntity, count);
                                }
                            })
                    )
                    .switchIfEmpty(responseInfoMsg("Unable to Read Request."))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        } else {
            Flux<SlaveCostCenterEntity> slaveCostCenterEntityFlux = slaveCostCenterRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
            return slaveCostCenterEntityFlux
                    .collectList()
                    .flatMap(costCenterEntity -> slaveCostCenterRepository
                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (costCenterEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records fetched successfully", costCenterEntity, count);
                                }
                            })
                    )
                    .switchIfEmpty(responseInfoMsg("Unable to Read Request."))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_cost-centers_active_index")
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

        Flux<SlaveCostCenterEntity> slaveCostCenterEntityFlux = slaveCostCenterRepository
                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE);
        return slaveCostCenterEntityFlux
                .collectList()
                .flatMap(costCenterEntity -> slaveCostCenterRepository
                        .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE)
                        .flatMap(count ->
                        {
                            if (costCenterEntity.isEmpty()) {
                                return responseIndexInfoMsg("Record does not exist", count);
                            } else {
                                return responseIndexSuccessMsg("All Records Fetched successfully", costCenterEntity, count);
                            }
                        })
                )
                .switchIfEmpty(responseInfoMsg("Unable to Read Request."))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_cost-centers_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID costCenterUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveCostCenterRepository.findByUuidAndDeletedAtIsNull(costCenterUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }


    @AuthHasPermission(value = "account_api_v1_cost-centers_branch_show")
    //Check Branch id In Config Module
    public Mono<ServerResponse> checkIfBranchExists(ServerRequest serverRequest) {
        final UUID branchUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return serverRequest.formData()
                .flatMap(value -> slaveCostCenterRepository.findFirstByBranchUUIDAndDeletedAtIsNull(branchUUID)
                        .flatMap(value1 -> responseInfoMsg("Unable to Delete Record as the Reference exists."))
                ).switchIfEmpty(responseErrorMsg("Record Does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist"));
    }

    @AuthHasPermission(value = "account_api_v1_cost-centers_company_show")
    //Check Company id In Config Module
    public Mono<ServerResponse> checkIfCompanyExists(ServerRequest serverRequest) {
        final UUID companyUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return serverRequest.formData()
                .flatMap(value -> slaveCostCenterRepository.findFirstByCompanyUUIDAndDeletedAtIsNull(companyUUID)
                        .flatMap(value1 -> responseInfoMsg("Unable to Delete Record as the Reference exists."))
                ).switchIfEmpty(responseErrorMsg("Record Does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist"));
    }

    @AuthHasPermission(value = "account_api_v1_cost-centers_voucher_show")
    //show Cost Centers for Voucher Id
    public Mono<ServerResponse> showVoucherWithCostCenters(ServerRequest serverRequest) {

        final UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());

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

        return slaveVoucherCostCenterGroupPvtRepository.costCenterGroupAllMappingExists(voucherUUID)
                .flatMap(all -> {

                    // if voucher is mapped with cost center group of all cost centers
                    if (all) {

                        // if status is given in query parameter
                        if (!status.isEmpty()) {
                            Flux<SlaveCostCenterEntity> slaveCostCenterEntityFlux = slaveCostCenterRepository
                                    .indexCostCenterWithStatus(searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());
                            return slaveCostCenterEntityFlux
                                    .collectList()
                                    .flatMap(costCenterEntity -> slaveCostCenterRepository
                                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                                            .flatMap(count ->
                                            {
                                                if (costCenterEntity.isEmpty()) {
                                                    return responseIndexInfoMsg("Record does not exist", count);
                                                } else {
                                                    return responseIndexSuccessMsg("All Records fetched successfully", costCenterEntity, count);
                                                }
                                            })
                                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
                        }

                        // if status is not given
                        else {
                            Flux<SlaveCostCenterEntity> slaveCostCenterEntityFlux = slaveCostCenterRepository
                                    .indexCostCenter(searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
                            return slaveCostCenterEntityFlux
                                    .collectList()
                                    .flatMap(costCenterEntity -> slaveCostCenterRepository
                                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                                            .flatMap(count ->
                                            {
                                                if (costCenterEntity.isEmpty()) {
                                                    return responseIndexInfoMsg("Record does not exist", count);
                                                } else {
                                                    return responseIndexSuccessMsg("All Records fetched successfully", costCenterEntity, count);
                                                }
                                            })
                                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
                        }
                    }

                    // if voucher is mapped with cost center group of all cost centers
                    else {
                        // if status is given in query parameter
                        if (!status.isEmpty()) {

                            Flux<SlaveCostCenterEntity> slaveCostCenterEntityFlux = slaveCostCenterRepository
                                    .showCostCenterListWithStatus(voucherUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

                            return slaveCostCenterEntityFlux
                                    .collectList()
                                    .flatMap(accountEntity -> slaveCostCenterRepository
                                            .countRecordsWithSearchFilterWithStatus(voucherUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                                            .flatMap(count ->
                                            {

                                                if (accountEntity.isEmpty()) {
                                                    return responseInfoMsg("Record does not exist");
                                                } else {
                                                    return responseIndexSuccessMsg("All Records Fetched Successfully!", accountEntity, count);
                                                }
                                            })).switchIfEmpty(responseInfoMsg("Unable to read the request."))
                                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
                        }

                        // if status is not given
                        else {

                            Flux<SlaveCostCenterEntity> slaveCostCenterEntityFlux = slaveCostCenterRepository
                                    .showCostCenterList(voucherUUID, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

                            return slaveCostCenterEntityFlux
                                    .collectList()
                                    .flatMap(accountEntity -> slaveCostCenterRepository
                                            .countRecordsWithSearchFilter(voucherUUID, searchKeyWord, searchKeyWord)
                                            .flatMap(count ->
                                            {

                                                if (accountEntity.isEmpty()) {
                                                    return responseInfoMsg("Record does not exist");
                                                } else {
                                                    return responseIndexSuccessMsg("All Records Fetched Successfully!", accountEntity, count);
                                                }
                                            })).switchIfEmpty(responseInfoMsg("Unable to read the request."))
                                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
                        }
                    }
                }).switchIfEmpty(responseInfoMsg("Record does not exist."))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_cost-centers_voucher_company_show")
    //show Cost Centers for Voucher With given Company Id
    public Mono<ServerResponse> showCostCentersWithCompany(ServerRequest serverRequest) {
        final UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());
        UUID companyUUID = UUID.fromString(serverRequest.queryParam("companyUUID").map(String::toString).orElse(""));

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

        // if status is given in query parameter
        if (!status.isEmpty()) {

            Flux<SlaveCostCenterEntity> slaveCostCenterEntityFlux = slaveCostCenterRepository
                    .showCostCenterWithCompanyWithStatus(voucherUUID, companyUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveCostCenterEntityFlux
                    .collectList()
                    .flatMap(accountEntity -> slaveCostCenterRepository
                            .countCostCenterWithCompanyWithStatus(voucherUUID, companyUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (accountEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully!", accountEntity, count);
                                }
                            })).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer"));
        }

        // if status is not given
        else {
            Flux<SlaveCostCenterEntity> slaveCostCenterEntityFlux = slaveCostCenterRepository
                    .showCostCenterWithCompany(voucherUUID, companyUUID, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveCostCenterEntityFlux
                    .collectList()
                    .flatMap(accountEntity -> slaveCostCenterRepository
                            .countCostCenterWithCompany(voucherUUID, companyUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (accountEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully!", accountEntity, count);
                                }
                            })).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer"));
        }
    }

    @AuthHasPermission(value = "account_api_v1_cost-centers_voucher_branch_show")
    //show Cost Centers for Voucher With given Branch Id
    public Mono<ServerResponse> showCostCentersWithBranch(ServerRequest serverRequest) {

        final UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());

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

        // if status is given in query parameter
        if (!status.isEmpty()) {
            Flux<SlaveCostCenterEntity> slaveCostCenterEntityFlux = slaveCostCenterRepository
                    .showCostCenterWithBranchWithStatus(voucherUUID, branchUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveCostCenterEntityFlux
                    .collectList()
                    .flatMap(accountEntity -> slaveCostCenterRepository
                            .countCostCenterWithBranchWithStatus(voucherUUID, branchUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (accountEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully!", accountEntity, count);
                                }
                            })).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
        }

        // if status is not given
        else {
            Flux<SlaveCostCenterEntity> slaveCostCenterEntityFlux = slaveCostCenterRepository
                    .showCostCenterWithBranch(voucherUUID, branchUUID, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveCostCenterEntityFlux
                    .collectList()
                    .flatMap(accountEntity -> slaveCostCenterRepository
                            .countCostCenterWithBranch(voucherUUID, branchUUID, searchKeyWord, searchKeyWord)
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

    @AuthHasPermission(value = "account_api_v1_cost-centers_store")
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

                    CostCenterEntity costCenterEntity = CostCenterEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .description(value.getFirst("description").trim())
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .companyUUID(UUID.fromString(reqCompanyUUID))
                            .branchUUID(UUID.fromString(reqBranchUUID))
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


                    return costCenterRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(costCenterEntity.getName())
                            .flatMap(nameAlreadyExists -> responseInfoMsg("Name Already Exists"))
                            .switchIfEmpty(Mono.defer(() -> costCenterRepository.save(costCenterEntity)
                                    .flatMap(value1 -> responseSuccessMsg("Record Stored Successfully", value1))
                                    .switchIfEmpty(responseInfoMsg("Unable to Store record.There is something wrong please try again."))
                                    .onErrorResume(err -> responseErrorMsg("Unable to store record. Please Contact Developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_cost-centers_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID costCenterUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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
                .flatMap(value -> costCenterRepository.findByUuidAndDeletedAtIsNull(costCenterUUID)
                        .flatMap(previousCostCenter -> {

                            CostCenterEntity updatedCostCenterEntity = CostCenterEntity.builder()
                                    .uuid(previousCostCenter.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .companyUUID(UUID.fromString(reqCompanyUUID))
                                    .branchUUID(UUID.fromString(reqBranchUUID))
                                    .createdAt(previousCostCenter.getCreatedAt())
                                    .createdBy(previousCostCenter.getCreatedBy())
                                    .updatedBy(UUID.fromString(userId))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousCostCenter.getReqCreatedIP())
                                    .reqCreatedPort(previousCostCenter.getReqCreatedPort())
                                    .reqCreatedBrowser(previousCostCenter.getReqCreatedBrowser())
                                    .reqCreatedOS(previousCostCenter.getReqCreatedOS())
                                    .reqCreatedDevice(previousCostCenter.getReqCreatedDevice())
                                    .reqCreatedReferer(previousCostCenter.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousCostCenter.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousCostCenter.setDeletedBy(UUID.fromString(userId));
                            previousCostCenter.setReqDeletedIP(reqIp);
                            previousCostCenter.setReqDeletedPort(reqPort);
                            previousCostCenter.setReqDeletedBrowser(reqBrowser);
                            previousCostCenter.setReqDeletedOS(reqOs);
                            previousCostCenter.setReqDeletedDevice(reqDevice);
                            previousCostCenter.setReqDeletedReferer(reqReferer);

                            return costCenterRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedCostCenterEntity.getName(), updatedCostCenterEntity.getUuid())
                                    .flatMap(nameAlreadyExists -> responseInfoMsg("Name Already Exists"))
                                    .switchIfEmpty(Mono.defer(() -> costCenterRepository.save(previousCostCenter)
                                            .then(costCenterRepository.save(updatedCostCenterEntity))
                                            .flatMap(value1 -> responseSuccessMsg("Record Updated Successfully", value1))
                                            .switchIfEmpty(responseInfoMsg("Unable to update record.There is something wrong please try again."))
                                            .onErrorResume(err -> responseErrorMsg("Unable to update record. Please Contact Developer."))
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(err -> responseErrorMsg("Record does not exist. Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_cost-centers_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID costCenterUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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

        return costCenterRepository.findByUuidAndDeletedAtIsNull(costCenterUUID)
                .flatMap(entity -> ledgerEntryRepository.findFirstByCostCenterUUIDAndDeletedAtIsNull(entity.getUuid())
                        .flatMap(ledgerEntry -> responseErrorMsg("Unable to Delete Record as the Reference Exists!"))
                        .switchIfEmpty(Mono.defer(() -> costCenterGroupCostCenterPvtRepository.findFirstByCostCenterUUIDAndDeletedAtIsNull(entity.getUuid())
                                .flatMap(PvtMsg -> responseInfoMsg("Unable to Delete Record as the Reference exists."))))
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

                            return costCenterRepository.save(entity)
                                    .flatMap(costCenterEntity -> responseSuccessMsg("Record Deleted Successfully", costCenterEntity))
                                    .switchIfEmpty(responseInfoMsg("Unable to Delete record.There is something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Delete record. Please Contact Developer."));
                        }))
                ).switchIfEmpty(responseInfoMsg("Record does not Exist."))
                .onErrorResume(ex -> responseErrorMsg("Record does not Exist. Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_cost-centers_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        final UUID costCenterUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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
                    return costCenterRepository.findByUuidAndDeletedAtIsNull(costCenterUUID)
                            .flatMap(previousCostCenter -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousCostCenter.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                CostCenterEntity updatedCostCenterEntity = CostCenterEntity.builder()
                                        .uuid(previousCostCenter.getUuid())
                                        .name(previousCostCenter.getName())
                                        .description(previousCostCenter.getDescription())
                                        .status(status == true ? true : false)
                                        .companyUUID(previousCostCenter.getCompanyUUID())
                                        .branchUUID(previousCostCenter.getBranchUUID())
                                        .createdAt(previousCostCenter.getCreatedAt())
                                        .createdBy(previousCostCenter.getCreatedBy())
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .updatedBy(UUID.fromString(userId))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousCostCenter.getReqCreatedIP())
                                        .reqCreatedPort(previousCostCenter.getReqCreatedPort())
                                        .reqCreatedBrowser(previousCostCenter.getReqCreatedBrowser())
                                        .reqCreatedOS(previousCostCenter.getReqCreatedOS())
                                        .reqCreatedDevice(previousCostCenter.getReqCreatedDevice())
                                        .reqCreatedReferer(previousCostCenter.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousCostCenter.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousCostCenter.setDeletedBy(UUID.fromString(userId));
                                previousCostCenter.setReqDeletedIP(reqIp);
                                previousCostCenter.setReqDeletedPort(reqPort);
                                previousCostCenter.setReqDeletedBrowser(reqBrowser);
                                previousCostCenter.setReqDeletedOS(reqOs);
                                previousCostCenter.setReqDeletedDevice(reqDevice);
                                previousCostCenter.setReqDeletedReferer(reqReferer);

                                return costCenterRepository.save(previousCostCenter)
                                        .then(costCenterRepository.save(updatedCostCenterEntity))
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
