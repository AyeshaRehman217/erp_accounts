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
import tuf.webscaf.app.dbContext.master.entity.VoucherGroupVoucherPvtEntity;
import tuf.webscaf.app.dbContext.master.entity.VoucherEntity;
import tuf.webscaf.app.dbContext.master.entity.VoucherGroupVoucherPvtEntity;
import tuf.webscaf.app.dbContext.master.repository.VoucherGroupRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherGroupVoucherPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherGroupVoucherPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
@Tag(name = "voucherGroupVoucherHandler")
public class VoucherGroupVoucherHandler {

    @Autowired
    VoucherGroupVoucherPvtRepository voucherGroupVoucherPvtRepository;

    @Autowired
    SlaveVoucherGroupVoucherPvtRepository slaveVoucherGroupVoucherPvtRepository;

    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    SlaveVoucherRepository slaveVoucherRepository;

    @Autowired
    VoucherGroupRepository voucherGroupRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_voucher-group-voucher_un-mapped_show")
    public Mono<ServerResponse> showUnMappedVouchersAgainstVoucherGroups(ServerRequest serverRequest) {

        UUID voucherGroupUUID = UUID.fromString(serverRequest.pathVariable("voucherGroupUUID").trim());

        // Optional Query Parameter of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        // if status is present in query parameter
        if (!status.isEmpty()){
            Flux<SlaveVoucherEntity> slaveVoucherEntityFlux = slaveVoucherGroupVoucherPvtRepository
                    .showUnMappedVoucherListAgainstVoucherGroupWithStatus(voucherGroupUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveVoucherEntityFlux
                    .collectList()
                    .flatMap(voucherEntityDB -> slaveVoucherRepository.countUnMappedVoucherAgainstVoucherGroupWithStatus(voucherGroupUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {

                                        if (voucherEntityDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records Fetched Successfully", voucherEntityDB, count);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }

        // if status is not present
        else {
            Flux<SlaveVoucherEntity> slaveVoucherEntityFlux = slaveVoucherGroupVoucherPvtRepository
                    .showUnMappedVoucherListAgainstVoucherGroup(voucherGroupUUID, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveVoucherEntityFlux
                    .collectList()
                    .flatMap(voucherEntityDB -> slaveVoucherRepository.countUnMappedVoucherAgainstVoucherGroup(voucherGroupUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count -> {

                                        if (voucherEntityDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records Fetched Successfully", voucherEntityDB, count);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_voucher-group-voucher_mapped_show")
    public Mono<ServerResponse> showMappedVouchersAgainstVoucherGroups(ServerRequest serverRequest) {

        UUID voucherGroupUUID = UUID.fromString(serverRequest.pathVariable("voucherGroupUUID").trim());
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
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        if (!status.isEmpty()) {
            Flux<SlaveVoucherEntity> slaveVoucherEntityFlux = slaveVoucherRepository
                    .showVoucherListWithStatusAgainstVoucherGroup(voucherGroupUUID, Boolean.valueOf(status), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveVoucherEntityFlux
                    .collectList()
                    .flatMap(voucherEntity -> slaveVoucherRepository
                            .countMappedVoucherListWithStatusAgainstVoucherGroup(voucherGroupUUID, searchKeyWord, Boolean.valueOf(status))
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
                    .showVoucherListAgainstVoucherGroup(voucherGroupUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveVoucherEntityFlux
                    .collectList()
                    .flatMap(voucherEntity -> slaveVoucherRepository
                            .countMappedVoucherAgainstVoucherGroup(voucherGroupUUID, searchKeyWord)
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

    @AuthHasPermission(value = "account_api_v1_voucher-group-voucher_store")
    public Mono<ServerResponse> store(ServerRequest serverRequest) {

        String userId = serverRequest.headers().firstHeader("auid");
        UUID voucherGroupUUID = UUID.fromString(serverRequest.pathVariable("voucherGroupUUID").trim());
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
                .flatMap(value -> voucherGroupRepository.findByUuidAndDeletedAtIsNull(voucherGroupUUID)
                        .flatMap(voucherGroupEntity -> {

                            // if given voucher Group is inactive
                            if (!voucherGroupEntity.getStatus()) {
                                return responseInfoMsg("voucher Group status is inactive");
                            }

                            //getting List of Voucher Group id From Front
                            List<String> listVoucher = new LinkedList<>(value.get("voucherUUID"));

                            //removing any empty String from the Front List
                            listVoucher.removeIf(s -> s.equals(""));

                            //Creating an Empty List to add all the UUID from Front
                            List<UUID> l_list = new ArrayList<>();
                            //Looping Through all the Voucher list and add in Empty List
                            for (String groupId : listVoucher) {
                                l_list.add(UUID.fromString(groupId));
                            }

                            //If the List is not empty do all the stuff
                            if (!l_list.isEmpty()) {
                                //Check if Voucher Group Records exist
                                return voucherRepository.findAllByUuidInAndStatusAndDeletedAtIsNull(l_list, Boolean.TRUE)
                                        .collectList()
                                        .flatMap(existingVouchers -> {
                                            // voucher UUID List
                                            List<UUID> voucherList = new ArrayList<>();
                                            //If the Voucher Group UUID exists fetch and save it in another list
                                            for (VoucherEntity vouchers : existingVouchers) {

                                                // add uuid in vouchers list
                                                voucherList.add(vouchers.getUuid());
                                            }

                                            //check if Final Voucher Group list is not empty
                                            if (!voucherList.isEmpty()) {

                                                // voucher uuid list to show in response
                                                List<UUID> returningVoucherRecords = new ArrayList<>(voucherList);

                                                List<VoucherGroupVoucherPvtEntity> listPvt = new ArrayList<>();
                                                //All the existing records that exist in Pvt Table
                                                return voucherGroupVoucherPvtRepository.findAllByVoucherGroupUUIDAndVoucherUUIDInAndDeletedAtIsNull(voucherGroupUUID, voucherList)
                                                        .collectList()
                                                        .flatMap(removelist -> {
                                                            for (VoucherGroupVoucherPvtEntity pvtEntity : removelist) {
                                                                //removing records from the List from front that contain already mapped uuid's
                                                                voucherList.remove(pvtEntity.getVoucherUUID());
                                                            }

                                                            for (UUID voucher : voucherList) {
                                                                VoucherGroupVoucherPvtEntity voucherWithGroupEntity = VoucherGroupVoucherPvtEntity
                                                                        .builder()
                                                                        .voucherGroupUUID(voucherGroupUUID)
                                                                        .voucherUUID(voucher)
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

                                                                listPvt.add(voucherWithGroupEntity);
                                                            }

                                                            return voucherGroupVoucherPvtRepository.saveAll(listPvt)
                                                                    .collectList()
                                                                    .flatMap(groupList -> voucherGroupVoucherPvtRepository.findByVoucherGroupUUIDAndDeletedAtIsNull(voucherGroupUUID)
                                                                            .collectList()
                                                                            .flatMap(mappedRecords -> {
                                                                                List<UUID> resultList = new ArrayList<>();
                                                                                for (VoucherGroupVoucherPvtEntity entity : mappedRecords) {
                                                                                    resultList.add(entity.getVoucherUUID());
                                                                                }

                                                                                return voucherRepository.findAllByUuidInAndDeletedAtIsNull(resultList)
                                                                                        .collectList()
                                                                                        .flatMap(voucherGroupRecords -> {
                                                                                            if (!voucherList.isEmpty()) {
                                                                                                return responseSuccessMsg("Record Stored Successfully!", returningVoucherRecords);
                                                                                            } else {
                                                                                                return responseSuccessMsg("Record Already exists", returningVoucherRecords);
                                                                                            }
                                                                                        });
                                                                            }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                                            .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."))
                                                                    ).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                                    .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                                        }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                        .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                            } else {
                                                return responseInfoMsg("Voucher Group Record does not exist");
                                            }
                                        }).switchIfEmpty(responseInfoMsg("The Entered Voucher Group Does not exist."))
                                        .onErrorResume(ex -> responseErrorMsg("The Entered Voucher Group Does not exist.Please Contact Developer."));
                            } else {
                                return responseInfoMsg("Select Vouchers First");
                            }
                        }).switchIfEmpty(responseInfoMsg("Voucher Group does not exist"))
                        .onErrorResume(err -> responseErrorMsg("Voucher Group does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_voucher-group-voucher_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID voucherGroupUUID = UUID.fromString(serverRequest.pathVariable("voucherGroupUUID").trim());
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
                .flatMap(value -> voucherGroupRepository.findByUuidAndDeletedAtIsNull(voucherGroupUUID)
                        .flatMap(voucherGroup -> voucherRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(value.getFirst("voucherUUID").trim()))
                                .flatMap(voucherEntity -> voucherGroupVoucherPvtRepository
                                        .findFirstByVoucherGroupUUIDAndVoucherUUIDAndDeletedAtIsNull(voucherGroup.getUuid(), voucherEntity.getUuid())
                                        .flatMap(voucherGroupVoucherPvtEntity -> {

                                            voucherGroupVoucherPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                            voucherGroupVoucherPvtEntity.setDeletedBy(UUID.fromString(userId));
                                            voucherGroupVoucherPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                            voucherGroupVoucherPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                            voucherGroupVoucherPvtEntity.setReqDeletedIP(reqIp);
                                            voucherGroupVoucherPvtEntity.setReqDeletedPort(reqPort);
                                            voucherGroupVoucherPvtEntity.setReqDeletedBrowser(reqBrowser);
                                            voucherGroupVoucherPvtEntity.setReqDeletedOS(reqOs);
                                            voucherGroupVoucherPvtEntity.setReqDeletedDevice(reqDevice);
                                            voucherGroupVoucherPvtEntity.setReqDeletedReferer(reqReferer);

                                            return voucherGroupVoucherPvtRepository.save(voucherGroupVoucherPvtEntity)
                                                    .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully!", voucherEntity))
                                                    .switchIfEmpty(responseInfoMsg("Unable to delete the record.There is something wrong please try again."))
                                                    .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer."));
                                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                        .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                                ).switchIfEmpty(responseInfoMsg("Voucher does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Voucher does not exist.Please Contact Developer."))
                        )
                        .switchIfEmpty(responseInfoMsg("Voucher Group does not exist"))
                        .onErrorResume(err -> responseErrorMsg("Voucher Group does not exist.Please Contact Developer."))
                )
                .switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));

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
