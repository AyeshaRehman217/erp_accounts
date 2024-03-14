package tuf.webscaf.app.http.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.dto.BranchWithBranchProfileDto;
import tuf.webscaf.app.dbContext.master.entity.VoucherBranchPvtEntity;
import tuf.webscaf.app.dbContext.master.repository.VoucherBranchPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherBranchPvtRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
@Tag(name = "voucherBranchHandler")
public class VoucherBranchHandler {
    @Value("${server.erp_config_module.uri}")
    private String configUri;

    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    VoucherBranchPvtRepository voucherBranchPvtRepository;

    @Autowired
    SlaveVoucherBranchPvtRepository slaveVoucherBranchPvtRepository;

    @Autowired
    ApiCallService apiCallService;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    //Check Branch UUID In Config Module
    @AuthHasPermission(value = "account_api_v1_voucher-branch_branch_show")
    public Mono<ServerResponse> getBranchUUID(ServerRequest serverRequest) {
        final UUID branchUUID = UUID.fromString(serverRequest.pathVariable("branchUUID"));

        return serverRequest.formData()
                .flatMap(value -> slaveVoucherBranchPvtRepository.findFirstByBranchUUIDAndDeletedAtIsNull(branchUUID)
                        .flatMap(value1 -> responseInfoMsg("Unable to Delete Record as the Reference Exists!"))
                ).switchIfEmpty(responseErrorMsg("Record Does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist"));
    }

    @AuthHasPermission(value = "account_api_v1_voucher-branch_list_show")
    public Mono<ServerResponse> showList(ServerRequest serverRequest) {
        final UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID"));

        return slaveVoucherBranchPvtRepository.getAllIds(voucherUUID)
                .flatMap(ids -> {
                    List<String> listOfIds = Arrays.asList(ids.split("\\s*,\\s*"));
                    return responseSuccessMsg("Records Fetched Successfully!", listOfIds);
                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist"));

    }

    @AuthHasPermission(value = "account_api_v1_voucher-branch_store")
    public Mono<ServerResponse> store(ServerRequest serverRequest) {

        String userId = serverRequest.headers().firstHeader("auid");
        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID"));
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
                                .flatMap(voucherEntity -> {

//                            // if given voucher is inactive
//                            if (!voucherEntity.getStatus()) {
//                                return responseInfoMsg("Voucher status is inactive");
//                            }

                                    // getting List of Branches From Front
                                    List<String> listOfBranchUUID = value.get("branchUUID");

                                    listOfBranchUUID.removeIf(s -> s.equals(""));

                                    if (!listOfBranchUUID.isEmpty()) {
                                        return apiCallService.getMultipleRecordWithQueryParams(configUri + "api/v1/branches/uuid/list/show", "uuid", listOfBranchUUID)
                                                .flatMap(branchJsonNode -> {

                                                    // Branch UUID List
                                                    List<UUID> branchList = apiCallService.getListUUID(branchJsonNode);

                                                    if (!branchList.isEmpty()) {

                                                        // branch uuid list to show in response
                                                        List<UUID> branchRecords = new ArrayList<>(branchList);

                                                        List<VoucherBranchPvtEntity> listPvt = new ArrayList<>();

                                                        return voucherBranchPvtRepository.findAllByVoucherUUIDAndBranchUUIDInAndDeletedAtIsNull(voucherUUID, branchList)
                                                                .collectList()
                                                                .flatMap(voucherPvtEntity -> {
                                                                    for (VoucherBranchPvtEntity pvtEntity : voucherPvtEntity) {
                                                                        //Removing Existing Branch UUID in Branch Final List to be saved that does not contain already mapped values
                                                                        branchList.remove(pvtEntity.getBranchUUID());
                                                                    }

                                                                    // iterate Branch UUIDs for given Voucher
                                                                    for (UUID branchUUID : branchList) {
                                                                        VoucherBranchPvtEntity voucherBranchPvtEntity = VoucherBranchPvtEntity
                                                                                .builder()
                                                                                .branchUUID(branchUUID)
                                                                                .uuid(UUID.randomUUID())
                                                                                .voucherUUID(voucherUUID)
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
                                                                        listPvt.add(voucherBranchPvtEntity);
                                                                    }

                                                                    return voucherBranchPvtRepository.saveAll(listPvt)
                                                                            .collectList()
                                                                            .flatMap(groupList -> {

                                                                                if (!branchList.isEmpty()) {
                                                                                    return responseSuccessMsg("Record Stored Successfully", branchRecords)
                                                                                            .switchIfEmpty(responseInfoMsg("Unable to Store Record. There is something wrong please try again!"))
                                                                                            .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                                                                } else {
                                                                                    return responseInfoMsg("Record Already Exists", branchRecords);
                                                                                }

                                                                            }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                                            .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                                                });
                                                    } else {
                                                        return responseInfoMsg("Branch Record does not exist");
                                                    }
                                                }).switchIfEmpty(responseInfoMsg("The Entered Branch Does not exist."))
                                                .onErrorResume(ex -> responseErrorMsg("The Entered Branch Does not exist.Please Contact Developer."));
                                    } else {
                                        return responseInfoMsg("Select Branch First");
                                    }
                                }).switchIfEmpty(responseInfoMsg("Voucher does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Voucher does not exist. Please contact developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer."));
    }

    public BranchWithBranchProfileDto branchDtoMapper(JsonNode jsonNode) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        final JsonNode arrNode = jsonNode.get("data");
        JsonNode objectNode = null;
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                objectNode = objNode;
            }
        }
        ObjectReader reader = mapper.readerFor(new TypeReference<BranchWithBranchProfileDto>() {
        });
        BranchWithBranchProfileDto branchDto = null;
        if (!jsonNode.get("data").isEmpty()) {
            try {
                branchDto = reader.readValue(objectNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return branchDto;
    }

    @AuthHasPermission(value = "account_api_v1_voucher-branch_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID"));
        UUID branchUUID = UUID.fromString(serverRequest.queryParam("branchUUID").map(String::toString).orElse(""));
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

        return apiCallService.getDataWithUUID(configUri + "api/v1/branches/show/", branchUUID)
                .flatMap(branchJson -> apiCallService.getUUID(branchJson)
                        .flatMap(branch -> voucherBranchPvtRepository.findFirstByVoucherUUIDAndBranchUUIDAndDeletedAtIsNull(voucherUUID, branchUUID)
                                .flatMap(voucherBranchPvtEntity -> {

                                    voucherBranchPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    voucherBranchPvtEntity.setDeletedBy(UUID.fromString(userId));
                                    voucherBranchPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                    voucherBranchPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                    voucherBranchPvtEntity.setReqDeletedIP(reqIp);
                                    voucherBranchPvtEntity.setReqDeletedPort(reqPort);
                                    voucherBranchPvtEntity.setReqDeletedBrowser(reqBrowser);
                                    voucherBranchPvtEntity.setReqDeletedOS(reqOs);
                                    voucherBranchPvtEntity.setReqDeletedDevice(reqDevice);
                                    voucherBranchPvtEntity.setReqDeletedReferer(reqReferer);

                                    return voucherBranchPvtRepository.save(voucherBranchPvtEntity)
                                            .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", branchDtoMapper(branchJson)))
                                            .switchIfEmpty(responseInfoMsg("Unable to delete the record.There is something wrong please try again."))
                                            .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer."));
                                })
                        )
                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));

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

    public Mono<ServerResponse> responseInfoMsg(String msg, Object entity) {
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
}
