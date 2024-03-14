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
import tuf.webscaf.app.dbContext.master.entity.ProfitCenterGroupEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterGroupEntity;
import tuf.webscaf.app.http.handler.VoucherProfitCenterGroupHandler;
import tuf.webscaf.app.http.validationFilters.voucherProfitCenterGroupHandler.DeleteVoucherProfitCenterGroupPvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.voucherProfitCenterGroupHandler.ShowVoucherProfitCenterGroupPvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.voucherProfitCenterGroupHandler.StoreVoucherProfitCenterGroupPvtHandlerFilter;
import tuf.webscaf.springDocImpl.ProfitCenterGroupDocImpl;
import tuf.webscaf.springDocImpl.VoucherProfitCenterGroupDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;


@Configuration
public class VoucherProfitCenterGroupRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/account/api/v1/voucher-profit-center-group/un-mapped/show/{voucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = VoucherProfitCenterGroupHandler.class,
                            beanMethod = "showUnMappedProfitCenterGroupAgainstVoucher",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showUnMappedProfitCenterGroupAgainstVoucher",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveProfitCenterGroupEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Profit Center Groups that are not mapped for given Voucher",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/voucher-profit-center-group/mapped/show/{voucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = VoucherProfitCenterGroupHandler.class,
                            beanMethod = "showMappedProfitCenterGroupAgainstVoucher",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showMappedProfitCenterGroupAgainstVoucher",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveProfitCenterGroupEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Profit Center Groups that are not mapped for given Voucher",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/voucher-profit-center-group/store/{voucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = VoucherProfitCenterGroupHandler.class,
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
                                                            implementation = ProfitCenterGroupEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    requestBody = @RequestBody(
                                            description = "Create Profit Center Groups for a Voucher",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = VoucherProfitCenterGroupDocImpl.class)
                                            )),
                                    description = "Store Profit Center Group for given Voucher",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/account/api/v1/voucher-profit-center-group/delete/{voucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = VoucherProfitCenterGroupHandler.class,
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
                                                            implementation = ProfitCenterGroupEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Delete Profit Center Group for given Voucher",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    },
                                    requestBody = @RequestBody(
                                            description = "Delete Profit Center Group for a Voucher",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = ProfitCenterGroupDocImpl.class)
                                            ))
                            )
                    )
            }
    )

    public RouterFunction<ServerResponse> voucherProfitCenterGroupRoutes(VoucherProfitCenterGroupHandler handle) {
        return RouterFunctions.route(GET("account/api/v1/voucher-profit-center-group/un-mapped/show/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showUnMappedProfitCenterGroupAgainstVoucher).filter(new ShowVoucherProfitCenterGroupPvtHandlerFilter())
                .and(RouterFunctions.route(GET("account/api/v1/voucher-profit-center-group/mapped/show/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showMappedProfitCenterGroupAgainstVoucher).filter(new ShowVoucherProfitCenterGroupPvtHandlerFilter()))
                .and(RouterFunctions.route(POST("account/api/v1/voucher-profit-center-group/store/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreVoucherProfitCenterGroupPvtHandlerFilter()))
                .and(RouterFunctions.route(DELETE("account/api/v1/voucher-profit-center-group/delete/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteVoucherProfitCenterGroupPvtHandlerFilter()));
    }

}
