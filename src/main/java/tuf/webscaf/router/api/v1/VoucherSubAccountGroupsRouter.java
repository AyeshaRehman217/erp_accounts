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
import tuf.webscaf.app.dbContext.master.entity.SubAccountGroupEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveSubAccountGroupEntity;
import tuf.webscaf.app.http.handler.VoucherSubAccountGroupHandler;
import tuf.webscaf.app.http.validationFilters.voucherSubAccountGroupHandler.DeleteVoucherSubAccountGroupHandlerFilter;
import tuf.webscaf.app.http.validationFilters.voucherSubAccountGroupHandler.ShowVoucherSubAccountGroupHandlerFilter;
import tuf.webscaf.app.http.validationFilters.voucherSubAccountGroupHandler.StoreVoucherSubAccountGroupHandlerFilter;
import tuf.webscaf.springDocImpl.SubAccountGroupDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;


@Configuration
public class VoucherSubAccountGroupsRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/account/api/v1/voucher-sub-account-groups/un-mapped/show/{voucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = VoucherSubAccountGroupHandler.class,
                            beanMethod = "showUnMappedSubAccountGroupsAgainstVoucher",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showUnMappedSubAccountGroupsAgainstVoucher",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveSubAccountGroupEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Account Groups that are not Mapped for given Voucher",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY,name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY,name = "skw", description = "Search with name and description"),
                                            @Parameter(in = ParameterIn.QUERY,name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/voucher-sub-account-groups/mapped/show/{voucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = VoucherSubAccountGroupHandler.class,
                            beanMethod = "showMappedSubAccountGroupsAgainstVoucher",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showMappedSubAccountGroupsAgainstVoucher",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveSubAccountGroupEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Account Groups that are Mapped for given Voucher",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY,name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY,name = "skw", description = "Search with name and description"),
                                            @Parameter(in = ParameterIn.QUERY,name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/voucher-sub-account-groups/store/{voucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = VoucherSubAccountGroupHandler.class,
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
                                                            implementation = SubAccountGroupEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Store Account Groups for given Voucher",
                                    requestBody = @RequestBody(
                                            description = "Create Account Groups for a Voucher",
                                            required = true,
                                            content = @Content(
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = SubAccountGroupDocImpl.class)
                                            )),
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/account/api/v1/voucher-sub-account-groups/delete/{voucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = VoucherSubAccountGroupHandler.class,
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
                                                            implementation = SubAccountGroupEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Delete Account Group for given Voucher",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "subAccountGroupUUID")
                                    }
                            )
                    )
            }
    )

    public RouterFunction<ServerResponse> voucherSubAccountGroupRoutes(VoucherSubAccountGroupHandler handle) {
        return RouterFunctions.route(GET("account/api/v1/voucher-sub-account-groups/un-mapped/show/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showUnMappedSubAccountGroupsAgainstVoucher).filter(new ShowVoucherSubAccountGroupHandlerFilter())
                .and(RouterFunctions.route(GET("account/api/v1/voucher-sub-account-groups/mapped/show/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showMappedSubAccountGroupsAgainstVoucher).filter(new ShowVoucherSubAccountGroupHandlerFilter()))
                .and(RouterFunctions.route(POST("account/api/v1/voucher-sub-account-groups/store/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreVoucherSubAccountGroupHandlerFilter()))
                .and(RouterFunctions.route(DELETE("account/api/v1/voucher-sub-account-groups/delete/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteVoucherSubAccountGroupHandlerFilter()));
    }

}
