package tuf.webscaf.router.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import tuf.webscaf.app.dbContext.master.entity.BranchEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveBranchEntity;
import tuf.webscaf.app.http.handler.VoucherBranchHandler;
import tuf.webscaf.app.http.validationFilters.voucherBranchHandler.CheckBranchListHandlerFilter;
import tuf.webscaf.app.http.validationFilters.voucherBranchHandler.DeleteVoucherBranchPvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.voucherBranchHandler.ShowVoucherBranchPvtMappedListHandlerFilter;
import tuf.webscaf.app.http.validationFilters.voucherBranchHandler.StoreVoucherBranchPvtHandlerFilter;
import tuf.webscaf.springDocImpl.BranchDocImpl;
import tuf.webscaf.springDocImpl.VoucherBranchDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;


@Configuration
public class VoucherBranchRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/account/api/v1/voucher-branch/list/show/{voucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = VoucherBranchHandler.class,
                            beanMethod = "showList",
                            operation = @Operation(
                                    operationId = "showList",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveBranchEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show the list of Branch Ids that are mapped for given Voucher",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/voucher-branch/branch/show/{branchUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = VoucherBranchHandler.class,
                            beanMethod = "getBranchUUID",
                            operation = @Operation(
                                    operationId = "getBranchUUID",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveBranchEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This Route is used by Config Module to Check If Branch Mapped With Vouchers",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "branchUUID")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/voucher-branch/store/{voucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = VoucherBranchHandler.class,
                            beanMethod = "store",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "store",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = BranchEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    requestBody = @RequestBody(
                                            description = "Create Branches for a Voucher",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = VoucherBranchDocImpl.class)
                                            )),
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/account/api/v1/voucher-branch/delete/{voucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = VoucherBranchHandler.class,
                            beanMethod = "delete",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "delete",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = BranchEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.HEADER, name = "auid"),
                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "branchUUID")
                                    }
                            )
                    )
            }
    )

    public RouterFunction<ServerResponse> voucherBranchRoutes(VoucherBranchHandler handle) {
        return RouterFunctions.route(GET("account/api/v1/voucher-branch/list/show/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showList).filter(new ShowVoucherBranchPvtMappedListHandlerFilter())
                .and(RouterFunctions.route(GET("account/api/v1/voucher-branch/branch/show/{branchUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::getBranchUUID).filter(new CheckBranchListHandlerFilter()))
                .and(RouterFunctions.route(POST("account/api/v1/voucher-branch/store/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreVoucherBranchPvtHandlerFilter()))
                .and(RouterFunctions.route(DELETE("account/api/v1/voucher-branch/delete/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteVoucherBranchPvtHandlerFilter()));
    }

}
