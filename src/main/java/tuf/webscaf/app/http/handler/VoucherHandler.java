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
import tuf.webscaf.app.dbContext.master.entity.VoucherEntity;
import tuf.webscaf.app.dbContext.master.repository.*;
import tuf.webscaf.app.dbContext.slave.dto.SlaveVoucherDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocumentNatureGroupEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveDocumentNatureGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Component
@Tag(name = "voucherHandler")
public class VoucherHandler {

    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    SlaveVoucherRepository slaveVoucherRepository;

    @Autowired
    VoucherTypeCatalogueRepository voucherTypeCatalogueRepository;

    @Autowired
    AccountGroupRepository accountGroupRepository;

    @Autowired
    JobGroupRepository jobGroupRepository;

    @Autowired
    VoucherGroupRepository voucherGroupRepository;

    @Autowired
    ProfitCenterGroupRepository profitCenterGroupRepository;

    @Autowired
    CostCenterGroupRepository costCenterGroupRepository;

    @Autowired
    DocumentNatureGroupRepository documentNatureGroupRepository;

    @Autowired
    SlaveDocumentNatureGroupRepository slaveDocumentNatureGroupRepository;

    @Autowired
    VoucherAccountGroupPvtRepository voucherAccountGroupPvtRepository;

    @Autowired
    VoucherSubAccountGroupPvtRepository voucherSubAccountGroupPvtRepository;

    @Autowired
    VoucherDocumentNatureGroupPvtRepository voucherDocumentNatureGroupPvtRepository;

    @Autowired
    VoucherJobGroupPvtRepository voucherJobGroupPvtRepository;

    @Autowired
    VoucherGroupVoucherPvtRepository voucherGroupVoucherPvtRepository;

    @Autowired
    VoucherProfitCenterGroupPvtRepository voucherProfitCenterGroupPvtRepository;

    @Autowired
    VoucherCostCenterGroupPvtRepository voucherCostCenterGroupPvtRepository;

    @Autowired
    VoucherCalendarGroupPvtRepository voucherCalendarGroupPvtRepository;

    @Autowired
    SlaveVoucherGroupRepository slaveVoucherGroupRepository;

    @Autowired
    VoucherCompanyPvtRepository voucherCompanyPvtRepository;

    @Autowired
    VoucherBranchPvtRepository voucherBranchPvtRepository;

    @Autowired
    ApiCallService apiCallService;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_vouchers_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        if (!status.isEmpty()) {
            Flux<SlaveVoucherDto> voucherDtoFlux = slaveVoucherRepository
                    .showAllVoucherRecordsWithStatus(Boolean.valueOf(status), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return voucherDtoFlux
                    .collectList()
                    .flatMap(voucherDto -> slaveVoucherRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {

                                if (voucherDto.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", voucherDto, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        } else {
            Flux<SlaveVoucherDto> voucherDtoFlux = slaveVoucherRepository
                    .showAllVoucherRecords(searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return voucherDtoFlux
                    .collectList()
                    .flatMap(voucherDto -> slaveVoucherRepository
                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {

                                if (voucherDto.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", voucherDto, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_vouchers_active_index")
    public Mono<ServerResponse> indexWithActiveStatus(ServerRequest serverRequest) {

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));


        Flux<SlaveVoucherEntity> voucherEntityFlux = slaveVoucherRepository
                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE);

        return voucherEntityFlux
                .collectList()
                .flatMap(voucherEntity -> slaveVoucherRepository
                        .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE)
                        .flatMap(count -> {

                            if (voucherEntity.isEmpty()) {
                                return responseIndexInfoMsg("Record does not exist", count);
                            } else {
                                return responseIndexSuccessMsg("All Records Fetched Successfully", voucherEntity, count);
                            }
                        })
                ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));

    }

    @AuthHasPermission(value = "account_api_v1_vouchers_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveVoucherRepository.showVoucherWithUUID(voucherUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    //This Function list vouchers against Company
    @AuthHasPermission(value = "account_api_v1_vouchers_company_mapped_show")
    public Mono<ServerResponse> showVouchersAgainstCompany(ServerRequest serverRequest) {

        UUID companyUUID = UUID.fromString(serverRequest.pathVariable("companyUUID"));
        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseInfoMsg("Invalid Page No");
        }

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
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        if (!status.isEmpty()) {
            Flux<SlaveVoucherEntity> slaveVoucherEntityFlux = slaveVoucherRepository
                    .showMappedVouchersAgainstCompanyWithStatus(companyUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveVoucherEntityFlux
                    .collectList()
                    .flatMap(voucherEntity -> slaveVoucherRepository
                            .countMappedVouchersAgainstCompanyWithStatus(companyUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (voucherEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully!", voucherEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        } else {
            Flux<SlaveVoucherEntity> slaveVoucherEntityFlux = slaveVoucherRepository
                    .showMappedVouchersAgainstCompany(companyUUID, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveVoucherEntityFlux
                    .collectList()
                    .flatMap(voucherEntity -> slaveVoucherRepository
                            .countMappedVouchersAgainstCompany(companyUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (voucherEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully!", voucherEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    //This Function list vouchers against Branch
    @AuthHasPermission(value = "account_api_v1_vouchers_branch_mapped_show")
    public Mono<ServerResponse> showVouchersAgainstBranch(ServerRequest serverRequest) {

        UUID branchUUID = UUID.fromString(serverRequest.pathVariable("branchUUID"));
        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseErrorMsg("Invalid Page No");
        }

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
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        if (!status.isEmpty()) {
            Flux<SlaveVoucherEntity> slaveVoucherEntityFlux = slaveVoucherRepository
                    .showMappedVouchersAgainstBranchWithStatus(branchUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveVoucherEntityFlux
                    .collectList()
                    .flatMap(voucherEntity -> slaveVoucherRepository
                            .countMappedVouchersAgainstBranchWithStatus(branchUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (voucherEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully!", voucherEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        } else {
            Flux<SlaveVoucherEntity> slaveVoucherEntityFlux = slaveVoucherRepository
                    .showMappedVouchersAgainstBranch(branchUUID, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveVoucherEntityFlux
                    .collectList()
                    .flatMap(voucherEntity -> slaveVoucherRepository
                            .countMappedVouchersAgainstBranch(branchUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (voucherEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully!", voucherEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_vouchers_job-group_mapped_show")
    public Mono<ServerResponse> showVouchersAgainstJobGroup(ServerRequest serverRequest) {
        UUID jobGroupUUID = UUID.fromString(serverRequest.pathVariable("jobGroupUUID").trim());
        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseErrorMsg("Invalid Page No");
        }

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
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        if (!status.isEmpty()) {
            Flux<SlaveVoucherEntity> slaveVoucherEntityFlux = slaveVoucherRepository
                    .showMappedVouchersWithStatusAgainstJobGroup(jobGroupUUID, Boolean.valueOf(status), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveVoucherEntityFlux
                    .collectList()
                    .flatMap(voucherEntity -> slaveVoucherRepository
                            .countMappedVouchersWithStatusAgainstJobGroup(jobGroupUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (voucherEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", voucherEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        } else {
            Flux<SlaveVoucherEntity> slaveVoucherEntityFlux = slaveVoucherRepository
                    .showMappedVouchersAgainstJobGroup(jobGroupUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveVoucherEntityFlux
                    .collectList()
                    .flatMap(voucherEntity -> slaveVoucherRepository
                            .countMappedVouchersAgainstJobGroup(jobGroupUUID, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (voucherEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", voucherEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    /**
     * Show Mapped Vouchers Against Account Group UUID
     **/
    @AuthHasPermission(value = "account_api_v1_vouchers_account-group_mapped_show")
    public Mono<ServerResponse> showVouchersAgainstAccountGroup(ServerRequest serverRequest) {
        UUID accountGroupUUID = UUID.fromString(serverRequest.pathVariable("accountGroupUUID").trim());
        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseErrorMsg("Invalid Page No");
        }

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
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        if (!status.isEmpty()) {
            Flux<SlaveVoucherEntity> slaveVoucherEntityFlux = slaveVoucherRepository
                    .showMappedVouchersAgainstAccountGroupWithStatus(accountGroupUUID, Boolean.valueOf(status), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveVoucherEntityFlux
                    .collectList()
                    .flatMap(voucherEntity -> slaveVoucherRepository
                            .countMappedVouchersAgainstAccountGroupWithStatus(accountGroupUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (voucherEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", voucherEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        } else {
            Flux<SlaveVoucherEntity> slaveVoucherEntityFlux = slaveVoucherRepository
                    .showMappedVouchersAgainstAccountGroup(accountGroupUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveVoucherEntityFlux
                    .collectList()
                    .flatMap(voucherEntity -> slaveVoucherRepository
                            .countMappedVouchersAgainstAccountGroup(accountGroupUUID, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (voucherEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", voucherEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    /**
     * Show Mapped Vouchers Against Cost Center Group UUID
     **/
    @AuthHasPermission(value = "account_api_v1_vouchers_cost-center-group_mapped_show")
    public Mono<ServerResponse> showVouchersAgainstCostCenterGroup(ServerRequest serverRequest) {
        UUID costCenterGroupUUID = UUID.fromString(serverRequest.pathVariable("costCenterGroupUUID").trim());
        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseErrorMsg("Invalid Page No");
        }

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
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        if (!status.isEmpty()) {
            Flux<SlaveVoucherEntity> slaveVoucherEntityFlux = slaveVoucherRepository
                    .showMappedVouchersAgainstCostCenterGroupWithStatus(costCenterGroupUUID, Boolean.valueOf(status), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveVoucherEntityFlux
                    .collectList()
                    .flatMap(voucherEntity -> slaveVoucherRepository
                            .countMappedVouchersAgainstCostCenterGroupWithStatus(costCenterGroupUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (voucherEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", voucherEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        } else {
            Flux<SlaveVoucherEntity> slaveVoucherEntityFlux = slaveVoucherRepository
                    .showMappedVouchersAgainstCostCenterGroup(costCenterGroupUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveVoucherEntityFlux
                    .collectList()
                    .flatMap(voucherEntity -> slaveVoucherRepository
                            .countMappedVouchersAgainstCostCenterGroup(costCenterGroupUUID, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (voucherEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", voucherEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    /**
     * Show Mapped Vouchers Against Profit Center Group UUID
     **/
    @AuthHasPermission(value = "account_api_v1_vouchers_profit-center-group_mapped_show")
    public Mono<ServerResponse> showVouchersAgainstProfitCenterGroup(ServerRequest serverRequest) {

        UUID profitCenterGroupUUID = UUID.fromString(serverRequest.pathVariable("profitCenterGroupUUID").trim());

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseErrorMsg("Invalid Page No");
        }

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
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        if (!status.isEmpty()) {
            Flux<SlaveVoucherEntity> slaveVoucherEntityFlux = slaveVoucherRepository
                    .showMappedVouchersAgainstProfitCenterGroupWithStatus(profitCenterGroupUUID, Boolean.valueOf(status), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveVoucherEntityFlux
                    .collectList()
                    .flatMap(voucherEntity -> slaveVoucherRepository
                            .countMappedVouchersAgainstProfitCenterGroupWithStatus(profitCenterGroupUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (voucherEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", voucherEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        } else {
            Flux<SlaveVoucherEntity> slaveVoucherEntityFlux = slaveVoucherRepository
                    .showMappedVouchersAgainstProfitCenterGroup(profitCenterGroupUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveVoucherEntityFlux
                    .collectList()
                    .flatMap(voucherEntity -> slaveVoucherRepository
                            .countMappedVouchersAgainstProfitCenterGroup(profitCenterGroupUUID, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (voucherEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", voucherEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_vouchers_calendar-group_mapped_show")
    public Mono<ServerResponse> showVouchersAgainstCalendarGroup(ServerRequest serverRequest) {

        UUID calendarGroupUUID = UUID.fromString(serverRequest.pathVariable("calendarGroupUUID").trim());

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseErrorMsg("Invalid Page No");
        }

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
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        if (!status.isEmpty()) {
            Flux<SlaveVoucherEntity> slaveVoucherEntityFlux = slaveVoucherRepository
                    .showMappedVouchersAgainstCalendarGroupWithStatus(calendarGroupUUID, Boolean.valueOf(status), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveVoucherEntityFlux
                    .collectList()
                    .flatMap(voucherEntity -> slaveVoucherRepository
                            .countMappedVouchersAgainstCalendarGroupWithStatus(calendarGroupUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (voucherEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", voucherEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        } else {
            Flux<SlaveVoucherEntity> slaveVoucherEntityFlux = slaveVoucherRepository
                    .showMappedVouchersAgainstCalendarGroup(calendarGroupUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveVoucherEntityFlux
                    .collectList()
                    .flatMap(voucherEntity -> slaveVoucherRepository
                            .countMappedVouchersAgainstCalendarGroup(calendarGroupUUID, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (voucherEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", voucherEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    /**
     * THis Function Will be shifted to Document nature Group Handler
     **/
    @AuthHasPermission(value = "account_api_v1_vouchers_document-nature-group_mapped_show")
    public Mono<ServerResponse> showVoucherWithDocumentNatureGroups(ServerRequest serverRequest) {

        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());
        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseErrorMsg("Invalid Page No");
        }

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

        Flux<SlaveDocumentNatureGroupEntity> slaveDocumentNatureGroupEntityFlux = slaveDocumentNatureGroupRepository
                .showDocumentNatureGroupList(voucherUUID, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
        return slaveDocumentNatureGroupEntityFlux
                .collectList()
                .flatMap(documentNatureGroupEntity -> slaveDocumentNatureGroupRepository
                        .countMappedDocumentNatureGroupAgainstVoucher(voucherUUID, searchKeyWord)
                        .flatMap(count ->
                        {
                            if (documentNatureGroupEntity.isEmpty()) {
                                return responseInfoMsg("Record does not exist");
                            } else {
                                return responseIndexSuccessMsg("All Records Fetched Successfully!", documentNatureGroupEntity, count);
                            }
                        })).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_vouchers_store")
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

                    VoucherEntity voucherEntity = VoucherEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .description(value.getFirst("description").trim())
                            .voucherTypeCatalogueUUID(UUID.fromString(value.getFirst("voucherTypeCatalogueUUID").trim()))
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

                    //Check if Voucher Type Catalogue exists
                    return voucherTypeCatalogueRepository.findByUuidAndDeletedAtIsNull(voucherEntity.getVoucherTypeCatalogueUUID())
                            .flatMap(voucherTypeCatalogueEntity -> voucherRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(voucherEntity.getName())//check uniqueness of name
                                    .flatMap(nameMsg -> responseInfoMsg("Name Already Exists"))
                                    .switchIfEmpty(Mono.defer(() -> {

                                        // if given voucher type is inactive
                                        if (!voucherTypeCatalogueEntity.getStatus()) {
                                            return responseInfoMsg("Voucher Type Catalogue status is inactive");
                                        }

                                        return voucherRepository.save(voucherEntity)
                                                .flatMap(voucherData -> responseSuccessMsg("Voucher Stored Successfully", voucherData))
                                                .switchIfEmpty(responseInfoMsg("Unable to Store Record,There is something wrong please try again."))
                                                .onErrorResume(err -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                    }))
                            ).switchIfEmpty(responseInfoMsg("Voucher Type Catalogue Does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Voucher Type Catalogue Does not exist.Please Contact Developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_vouchers_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());
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
                .flatMap(value -> voucherRepository.findByUuidAndDeletedAtIsNull(voucherUUID)
                        .flatMap(previousVoucherEntity -> {

                            VoucherEntity voucherEntity = VoucherEntity.builder()
                                    .uuid(previousVoucherEntity.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .voucherTypeCatalogueUUID(UUID.fromString(value.getFirst("voucherTypeCatalogueUUID").trim()))
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdBy(previousVoucherEntity.getCreatedBy())
                                    .createdAt(previousVoucherEntity.getCreatedAt())
                                    .updatedBy(UUID.fromString(userId))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousVoucherEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousVoucherEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousVoucherEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousVoucherEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousVoucherEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousVoucherEntity.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousVoucherEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousVoucherEntity.setDeletedBy(UUID.fromString(userId));
                            previousVoucherEntity.setReqDeletedIP(reqIp);
                            previousVoucherEntity.setReqDeletedPort(reqPort);
                            previousVoucherEntity.setReqDeletedBrowser(reqBrowser);
                            previousVoucherEntity.setReqDeletedOS(reqOs);
                            previousVoucherEntity.setReqDeletedDevice(reqDevice);
                            previousVoucherEntity.setReqDeletedReferer(reqReferer);

                            return voucherTypeCatalogueRepository.findByUuidAndDeletedAtIsNull(voucherEntity.getVoucherTypeCatalogueUUID())
                                    .flatMap(voucherTypeCatalogueEntity -> voucherRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(voucherEntity.getName(), voucherUUID)
                                            .flatMap(nameMsg -> responseInfoMsg("Name already Exists"))
                                            .switchIfEmpty(Mono.defer(() -> {

                                                // if given voucher type is inactive
                                                if (!voucherTypeCatalogueEntity.getStatus()) {
                                                    return responseInfoMsg("Voucher Type Catalogue status is inactive");
                                                }
                                                return voucherRepository.save(previousVoucherEntity)
                                                        .then(voucherRepository.save(voucherEntity))
                                                        .flatMap(voucherData -> responseSuccessMsg("Voucher Updated Successfully", voucherData))
                                                        .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong please try again."))
                                                        .onErrorResume(err -> responseErrorMsg("Unable to Update Record.Please Contact Developer."));
                                            }))
                                    ).switchIfEmpty(responseInfoMsg("Voucher Type Catalogue Does not exist"))
                                    .onErrorResume(ex -> responseErrorMsg("Voucher Type Catalogue Does not exist.Please Contact Developer."));
                        }).switchIfEmpty(responseInfoMsg("Voucher Does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record Does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_vouchers_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());
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

        return voucherRepository.findByUuidAndDeletedAtIsNull(voucherUUID)
        // Check if Voucher Exists in Voucher Voucher Group Pvt Table
                .flatMap(voucherEntity -> voucherGroupVoucherPvtRepository.findFirstByVoucherUUIDAndDeletedAtIsNull(voucherEntity.getUuid())
                        .flatMap(checkMsg -> responseInfoMsg("Unable to Delete Record as its Reference Exist"))
                        //Check if Voucher Exists in Transactions
                        .switchIfEmpty(Mono.defer(() -> transactionRepository.findFirstByVoucherUUIDAndDeletedAtIsNull(voucherEntity.getUuid())
                                .flatMap(checkMsg1 -> responseInfoMsg("Unable to Delete Record as its Reference Exist"))))
                        //Check if Voucher Exists in Voucher Account Group Mapping
                        .switchIfEmpty(Mono.defer(() -> voucherAccountGroupPvtRepository.findFirstByVoucherUUIDAndDeletedAtIsNull(voucherEntity.getUuid())
                                .flatMap(checkMsg1 -> responseInfoMsg("Unable to Delete Record as its Reference Exist"))))
                        //Check if Voucher Exists in Voucher Sub Account Group Mapping
                        .switchIfEmpty(Mono.defer(() -> voucherSubAccountGroupPvtRepository.findFirstByVoucherUUIDAndDeletedAtIsNull(voucherEntity.getUuid())
                                .flatMap(checkMsg1 -> responseInfoMsg("Unable to Delete Record as its Reference Exist"))))
                        //Check if Voucher Exists in Voucher Job Group Mapping
                        .switchIfEmpty(Mono.defer(() -> voucherJobGroupPvtRepository.findFirstByVoucherUUIDAndDeletedAtIsNull(voucherEntity.getUuid())
                                .flatMap(checkMsg1 -> responseInfoMsg("Unable to Delete Record as its Reference Exist"))))
                        //Check if Voucher Exists in Voucher Profit Center Group Mapping
                        .switchIfEmpty(Mono.defer(() -> voucherProfitCenterGroupPvtRepository.findFirstByVoucherUUIDAndDeletedAtIsNull(voucherEntity.getUuid())
                                .flatMap(checkMsg1 -> responseInfoMsg("Unable to Delete Record as its Reference Exist"))))
                        //Check if Voucher Exists in Voucher Cost Center Group Mapping
                        .switchIfEmpty(Mono.defer(() -> voucherCostCenterGroupPvtRepository.findFirstByVoucherUUIDAndDeletedAtIsNull(voucherEntity.getUuid())
                                .flatMap(checkMsg1 -> responseInfoMsg("Unable to Delete Record as its Reference Exist"))))
//                        //Check if Voucher Exists in Voucher Document Nature Group Mapping
//                        .switchIfEmpty(Mono.defer(() -> voucherDocumentNatureGroupPvtRepository.findFirstByVoucherUUIDAndDeletedAtIsNull(voucherEntity.getUuid())
//                                .flatMap(checkMsg1 -> responseInfoMsg("Unable to Delete Record as its Reference Exist"))))
                        //Check if Voucher Exists in Voucher Calendar Group Mapping
                        .switchIfEmpty(Mono.defer(() -> voucherCalendarGroupPvtRepository.findFirstByVoucherUUIDAndDeletedAtIsNull(voucherEntity.getUuid())
                                .flatMap(checkMsg1 -> responseInfoMsg("Unable to Delete Record as its Reference Exist"))))
                        //Check if Voucher Exists in Voucher Company Mapping
                        .switchIfEmpty(Mono.defer(() -> voucherCompanyPvtRepository.findFirstByVoucherUUIDAndDeletedAtIsNull(voucherEntity.getUuid())
                                .flatMap(checkMsg1 -> responseInfoMsg("Unable to Delete Record as its Reference Exist"))))
                        //Check if Voucher Exists in Voucher Branch Mapping
                        .switchIfEmpty(Mono.defer(() -> voucherBranchPvtRepository.findFirstByVoucherUUIDAndDeletedAtIsNull(voucherEntity.getUuid())
                                .flatMap(checkMsg1 -> responseInfoMsg("Unable to Delete Record as its Reference Exist"))))
                        //Delete Voucher Record
                        .switchIfEmpty(Mono.defer(() -> {
                            voucherEntity.setDeletedBy(UUID.fromString(userId));
                            voucherEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            voucherEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            voucherEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            voucherEntity.setReqDeletedIP(reqIp);
                            voucherEntity.setReqDeletedPort(reqPort);
                            voucherEntity.setReqDeletedBrowser(reqBrowser);
                            voucherEntity.setReqDeletedOS(reqOs);
                            voucherEntity.setReqDeletedDevice(reqDevice);
                            voucherEntity.setReqDeletedReferer(reqReferer);

                            return voucherRepository.save(voucherEntity)
                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                    .switchIfEmpty(responseInfoMsg("Unable to delete record.There is something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to delete record.Please contact developer."));
                        }))
                ).switchIfEmpty(responseInfoMsg("Record Does not exist."))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist.Please Contact Developer."));

    }

    @AuthHasPermission(value = "account_api_v1_vouchers_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID voucherUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
                    return voucherRepository.findByUuidAndDeletedAtIsNull(voucherUUID)
                            .flatMap(val -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((val.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                VoucherEntity updatedVoucherEntity = VoucherEntity.builder()
                                        .uuid(val.getUuid())
                                        .name(val.getName())
                                        .description(val.getDescription())
                                        .voucherTypeCatalogueUUID(val.getVoucherTypeCatalogueUUID())
                                        .status(status == true ? true : false)
                                        .createdAt(val.getCreatedAt())
                                        .createdBy(val.getCreatedBy())
                                        .updatedBy(UUID.fromString(userId))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(val.getReqCreatedIP())
                                        .reqCreatedPort(val.getReqCreatedPort())
                                        .reqCreatedBrowser(val.getReqCreatedBrowser())
                                        .reqCreatedOS(val.getReqCreatedOS())
                                        .reqCreatedDevice(val.getReqCreatedDevice())
                                        .reqCreatedReferer(val.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                // update status
                                val.setDeletedBy(UUID.fromString(userId));
                                val.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                val.setReqDeletedIP(reqIp);
                                val.setReqDeletedPort(reqPort);
                                val.setReqDeletedBrowser(reqBrowser);
                                val.setReqDeletedOS(reqOs);
                                val.setReqDeletedDevice(reqDevice);
                                val.setReqDeletedReferer(reqReferer);

                                return voucherRepository.save(val)
                                        .then(voucherRepository.save(updatedVoucherEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status updated successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status.There is something wrong please try again."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status.Please contact developer."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Requested Record does not exist.Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request."))
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
