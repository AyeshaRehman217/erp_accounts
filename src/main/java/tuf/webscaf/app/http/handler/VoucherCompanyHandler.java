//package tuf.webscaf.app.http.handler;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.ObjectReader;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.server.ServerRequest;
//import org.springframework.web.reactive.function.server.ServerResponse;
//import reactor.core.publisher.Mono;
//import tuf.webscaf.app.dbContext.master.dto.CompanyWithCompanyProfileDto;
//import tuf.webscaf.app.dbContext.master.entity.BranchEntity;
//import tuf.webscaf.app.dbContext.master.entity.VoucherBranchPvtEntity;
//import tuf.webscaf.app.dbContext.master.entity.VoucherCompanyPvtEntity;
//import tuf.webscaf.app.dbContext.master.repository.VoucherBranchPvtRepository;
//import tuf.webscaf.app.dbContext.master.repository.VoucherCompanyPvtRepository;
//import tuf.webscaf.app.dbContext.master.repository.VoucherRepository;
//import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherCompanyPvtRepository;
//import tuf.webscaf.app.service.ApiCallService;
//import tuf.webscaf.app.verification.module.AuthHasPermission;
//import tuf.webscaf.config.service.response.AppResponse;
//import tuf.webscaf.config.service.response.AppResponseMessage;
//import tuf.webscaf.config.service.response.CustomResponse;
//
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.*;
//
//@Component
//@Tag(name = "voucherCompanyHandler")
//public class VoucherCompanyHandler {
//
//    @Value("${server.erp_config_module.uri}")
//    private String configUri;
//
//    @Autowired
//    VoucherRepository voucherRepository;
//
//    @Autowired
//    VoucherCompanyPvtRepository voucherCompanyPvtRepository;
//
//    @Autowired
//    SlaveVoucherCompanyPvtRepository slaveVoucherCompanyPvtRepository;
//
//    @Autowired
//    VoucherBranchPvtRepository voucherBranchPvtRepository;
//
//    @Autowired
//    ApiCallService apiCallService;
//
//    @Autowired
//    CustomResponse appresponse;
//
//    @Value("${server.zone}")
//    private String zone;
//
//    //Check Company id In Config Module
//    @AuthHasPermission(value = "account_api_v1_voucher-company_company_show")
//    public Mono<ServerResponse> getCompanyUUID(ServerRequest serverRequest) {
//        UUID companyUUID = UUID.fromString(serverRequest.pathVariable("companyUUID"));
//
//        return serverRequest.formData()
//                .flatMap(value -> slaveVoucherCompanyPvtRepository.findFirstByCompanyUUIDAndDeletedAtIsNull(companyUUID)
//                        .flatMap(value1 -> responseInfoMsg("Unable to Delete Record as the Reference Exists!"))
//                ).switchIfEmpty(responseErrorMsg("Record Does not exist"))
//                .onErrorResume(ex -> responseErrorMsg("Record Does not exist"));
//    }
//
//    @AuthHasPermission(value = "account_api_v1_voucher-company_list_show")
//    public Mono<ServerResponse> showList(ServerRequest serverRequest) {
//        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID"));
//        return slaveVoucherCompanyPvtRepository.getAllIds(voucherUUID)
//                .flatMap(ids -> {
//                    List<String> listOfIds = Arrays.asList(ids.split("\\s*,\\s*"));
//                    return responseSuccessMsg("Records Fetched Successfully!", listOfIds);
//                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
//                .onErrorResume(err -> responseErrorMsg("Record does not exist"));
//
//    }
//
//    @AuthHasPermission(value = "account_api_v1_voucher-company_store")
//    public Mono<ServerResponse> store(ServerRequest serverRequest) {
//
//        String userId = serverRequest.headers().firstHeader("auid");
//        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID"));
//        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
//        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
//        String reqIp = serverRequest.headers().firstHeader("reqIp");
//        String reqPort = serverRequest.headers().firstHeader("reqPort");
//        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
//        String reqOs = serverRequest.headers().firstHeader("reqOs");
//        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
//        String reqReferer = serverRequest.headers().firstHeader("reqReferer");
//
//        if (userId == null) {
//            return responseErrorMsg("Unknown user");
//        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//            return responseErrorMsg("Unknown user");
//        }
//
//        return serverRequest.formData()
//                .flatMap(value -> voucherRepository.findByUuidAndDeletedAtIsNull(voucherUUID)
//                        .flatMap(financialVoucherEntity -> {
//
//                            //getting List of Campuses From Front
//                            List<String> listOfCompanyUUID = new LinkedList<>(value.get("companyUUID"));
//
//                            listOfCompanyUUID.removeIf(s -> s.equals(""));
//
//                            if (!listOfCompanyUUID.isEmpty()) {
//                                return apiCallService.getData(configUri + "api/v1/info/show")
//                                        .flatMap(moduleJsonNode -> apiCallService.getModuleId(moduleJsonNode)
//                                                // if config module Id exists, check if branch records exist
//                                                .flatMap(moduleId -> apiCallService.getMultipleRecordWithQueryParams(configUri + "api/v1/companies/list/show", "uuid", listOfCompanyUUID)
//                                                        .flatMap(companyJsonNode -> {
//                                                            // Campus UUID List
//                                                            List<UUID> companyList = new ArrayList<>(apiCallService.getListUUID(companyJsonNode));
//
//                                                            if (!companyList.isEmpty()) {
//
//                                                                // Campus UUID List to Get Records in Response
//                                                                List<String> finalCompanyList = new ArrayList<>();
//
//                                                                for (UUID companyUUID : companyList) {
//                                                                    finalCompanyList.add(companyUUID.toString());
//                                                                }
//
//                                                                // campus uuid list to show in response
//                                                                List<UUID> companyRecords = new ArrayList<>(companyList);
//
//                                                                List<VoucherCompanyPvtEntity> listPvt = new ArrayList<>();
//
//                                                                return voucherCompanyPvtRepository.findAllByVoucherUUIDAndCompanyUUIDInAndDeletedAtIsNull(voucherUUID, companyList)
//                                                                        .collectList()
//                                                                        .flatMap(voucherPvtEntity -> {
//
//                                                                            for (VoucherCompanyPvtEntity pvtEntity : voucherPvtEntity) {
//                                                                                //Removing Existing Company UUID in Company Final List to be saved that does not contain already mapped values
//                                                                                companyList.remove(pvtEntity.getCompanyUUID());
//                                                                            }
//
//                                                                            // iterate Company UUIDs for given Voucher
//                                                                            for (UUID companyUUIDs : companyList) {
//
//                                                                                VoucherCompanyPvtEntity voucherCompanyPvtEntity = VoucherCompanyPvtEntity
//                                                                                        .builder()
//                                                                                        .companyUUID(companyUUIDs)
//                                                                                        .uuid(UUID.randomUUID())
//                                                                                        .voucherUUID(voucherUUID)
//                                                                                        .createdBy(UUID.fromString(userId))
//                                                                                        .createdAt(LocalDateTime.now(ZoneId.of(zone)))
//                                                                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
//                                                                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
//                                                                                        .reqCreatedIP(reqIp)
//                                                                                        .reqCreatedPort(reqPort)
//                                                                                        .reqCreatedBrowser(reqBrowser)
//                                                                                        .reqCreatedOS(reqOs)
//                                                                                        .reqCreatedDevice(reqDevice)
//                                                                                        .reqCreatedReferer(reqReferer)
//                                                                                        .build();
//
//                                                                                listPvt.add(voucherCompanyPvtEntity);
//
//                                                                            }
//
//                                                                            return voucherCompanyPvtRepository.saveAll(listPvt)
//                                                                                    .collectList()
//                                                                                    .flatMap(groupList -> {
//
//                                                                                        if (!companyList.isEmpty()) {
//                                                                                            return responseSuccessMsg("Record Stored Successfully", companyRecords)
//                                                                                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record,There is something wrong please try again!"))
//                                                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
//                                                                                        } else {
//                                                                                            return responseSuccessMsg("Record Already Exists", companyRecords);
//                                                                                        }
//
//                                                                                    }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
//                                                                                    .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
//                                                                        });
//                                                            } else {
//                                                                return responseInfoMsg("Company Record does not exist");
//                                                            }
//                                                        }).switchIfEmpty(responseInfoMsg("The Entered Company Does not exist."))
//                                                        .onErrorResume(ex -> responseErrorMsg("The Entered Company Does not exist.Please Contact Developer."))
//                                                ).switchIfEmpty(responseInfoMsg("Unable to Connect to Config Module."))
//                                                .onErrorResume(ex -> responseErrorMsg("Unable to Connect to Config Module.Please Contact Developer."))
//                                        ).switchIfEmpty(responseInfoMsg("Unable to Connect to Config Module."))
//                                        .onErrorResume(ex -> responseErrorMsg("Unable to Connect to Config Module.Please Contact Developer."));
//                            } else {
//                                return responseInfoMsg("Select Company First");
//                            }
//                        }).switchIfEmpty(responseInfoMsg("Voucher does not exist"))
//                        .onErrorResume(err -> responseErrorMsg("Voucher does not exist. Please contact developer."))
//                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
//                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer."));
//    }
//
//    public CompanyWithCompanyProfileDto companyDtoMapper(JsonNode jsonNode) {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JavaTimeModule());
//        final JsonNode arrNode = jsonNode.get("data");
//        JsonNode objectNode = null;
//        if (arrNode.isArray()) {
//            for (final JsonNode objNode : arrNode) {
//                objectNode = objNode;
//            }
//        }
//        ObjectReader reader = mapper.readerFor(new TypeReference<CompanyWithCompanyProfileDto>() {
//        });
//        CompanyWithCompanyProfileDto companyDto = null;
//        if (!jsonNode.get("data").isEmpty()) {
//            try {
//                companyDto = reader.readValue(objectNode);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return companyDto;
//    }
//
//    @AuthHasPermission(value = "account_api_v1_voucher-company_delete")
//    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
//        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID"));
//        UUID companyUUID = UUID.fromString(serverRequest.queryParam("companyUUID").map(String::toString).orElse(""));
//        String userId = serverRequest.headers().firstHeader("auid");
//        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
//        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
//        String reqIp = serverRequest.headers().firstHeader("reqIp");
//        String reqPort = serverRequest.headers().firstHeader("reqPort");
//        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
//        String reqOs = serverRequest.headers().firstHeader("reqOs");
//        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
//        String reqReferer = serverRequest.headers().firstHeader("reqReferer");
//
//        if (userId == null) {
//            return responseErrorMsg("Unknown user");
//        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//            return responseErrorMsg("Unknown user");
//        }
//
//        return voucherRepository.findByUuidAndDeletedAtIsNull(voucherUUID)
//                .flatMap(financialVoucherEntity -> apiCallService.getDataWithUUID(configUri + "api/v1/companies/show/", companyUUID)
//                        .flatMap(companyJson -> apiCallService.getUUID(companyJson)
//                                .flatMap(checkInBranches -> voucherBranchPvtRepository.findAllByVoucherUUIDAndDeletedAtIsNull(financialVoucherEntity.getUuid())
//                                        .collectList()
//                                        .flatMap(branchVoucherEntity -> {
//                                            //getting mapped branches against the same Voucher from Voucher Branch pvt
//                                            List<UUID> listOfBranchFromPvt = new ArrayList<>();
//
//                                            for (VoucherBranchPvtEntity voucherBranch : branchVoucherEntity) {
//                                                listOfBranchFromPvt.add(voucherBranch.getBranchUUID());
//                                            }
//
//                                            //get All the Branches against the given company UUID from Config Module
//                                            return apiCallService.getDataWithUUID(configUri + "api/v1/uuid/company/branches/show/", companyUUID)
//                                                    .flatMap(branchList -> {
//
//                                                        //if the branch lie against the given company then Unable to Delete Record
//                                                        if (!Collections.disjoint(apiCallService.getListUUID(branchList), listOfBranchFromPvt)) {
//                                                            return responseInfoMsg("Unable to Delete Record as the Reference Exists!");
//                                                        } else {
//                                                            return voucherCompanyPvtRepository.findFirstByVoucherUUIDAndCompanyUUIDAndDeletedAtIsNull(voucherUUID, companyUUID)
//                                                                    .flatMap(voucherCompanyPvtEntity -> {
//
//                                                                        voucherCompanyPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//                                                                        voucherCompanyPvtEntity.setDeletedBy(UUID.fromString(userId));
//                                                                        voucherCompanyPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
//                                                                        voucherCompanyPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
//                                                                        voucherCompanyPvtEntity.setReqDeletedIP(reqIp);
//                                                                        voucherCompanyPvtEntity.setReqDeletedPort(reqPort);
//                                                                        voucherCompanyPvtEntity.setReqDeletedBrowser(reqBrowser);
//                                                                        voucherCompanyPvtEntity.setReqDeletedOS(reqOs);
//                                                                        voucherCompanyPvtEntity.setReqDeletedDevice(reqDevice);
//                                                                        voucherCompanyPvtEntity.setReqDeletedReferer(reqReferer);
//
//                                                                        return voucherCompanyPvtRepository.save(voucherCompanyPvtEntity)
//                                                                                .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", companyDtoMapper(companyJson)))
//                                                                                .switchIfEmpty(responseInfoMsg("Unable to delete the record.There is something wrong please try again."))
//                                                                                .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer."));
//                                                                    }).switchIfEmpty(responseInfoMsg("Record does not exist"))
//                                                                    .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
//                                                        }
//                                                    });
//                                        })
//
//                                )
//                        ).switchIfEmpty(responseInfoMsg("Company Does not exist"))
//                        .onErrorResume(ex -> responseErrorMsg("Company Does not Exist.Please Contact Developer."))
//                )
//                .switchIfEmpty(responseInfoMsg("Voucher Does not exist."))
//                .onErrorResume(ex -> responseErrorMsg("Voucher Does not exist.Please Contact Developer."));
//    }
//
//    public Mono<ServerResponse> responseErrorMsg(String msg) {
//        var messages = List.of(
//                new AppResponseMessage(
//                        AppResponse.Response.ERROR,
//                        msg
//                )
//        );
//
//        return appresponse.set(
//                HttpStatus.BAD_REQUEST.value(),
//                HttpStatus.BAD_REQUEST.name(),
//                null,
//                "eng",
//                "token",
//                0L,
//                0L,
//                messages,
//                Mono.empty()
//        );
//    }
//
//    public Mono<ServerResponse> responseSuccessMsg(String msg, Object entity) {
//        var messages = List.of(
//                new AppResponseMessage(
//                        AppResponse.Response.SUCCESS,
//                        msg
//                )
//        );
//
//        return appresponse.set(
//                HttpStatus.OK.value(),
//                HttpStatus.OK.name(),
//                null,
//                "eng",
//                "token",
//                0L,
//                0L,
//                messages,
//                Mono.just(entity)
//        );
//    }
//
//
//    public Mono<ServerResponse> responseInfoMsg(String msg) {
//        var messages = List.of(
//                new AppResponseMessage(
//                        AppResponse.Response.INFO,
//                        msg
//                )
//        );
//
//        return appresponse.set(
//                HttpStatus.OK.value(),
//                HttpStatus.OK.name(),
//                null,
//                "eng",
//                "token",
//                0L,
//                0L,
//                messages,
//                Mono.empty()
//        );
//    }
//
//    public Mono<ServerResponse> responseIndexSuccessMsg(String msg, Object entity, Long totalDataRowsWithFilter) {
//        var messages = List.of(
//                new AppResponseMessage(
//                        AppResponse.Response.SUCCESS,
//                        msg)
//        );
//
//        return appresponse.set(
//                HttpStatus.OK.value(),
//                HttpStatus.OK.name(),
//                null,
//                "eng",
//                "token",
//                totalDataRowsWithFilter,
//                0L,
//                messages,
//                Mono.just(entity)
//        );
//    }
//}
