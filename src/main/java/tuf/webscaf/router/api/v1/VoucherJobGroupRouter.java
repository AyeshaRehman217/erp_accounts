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
import tuf.webscaf.app.dbContext.master.entity.JobGroupEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveJobGroupEntity;
import tuf.webscaf.app.http.handler.VoucherJobGroupHandler;
import tuf.webscaf.app.http.validationFilters.voucherJobGroupHandler.DeleteVoucherJobGroupPvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.voucherJobGroupHandler.ShowVoucherJobGroupPvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.voucherJobGroupHandler.StoreVoucherJobGroupPvtHandlerFilter;
import tuf.webscaf.springDocImpl.JobGroupDocImpl;
import tuf.webscaf.springDocImpl.VoucherJobGroupDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;


@Configuration
public class VoucherJobGroupRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/account/api/v1/voucher-job-group/un-mapped/show/{voucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = VoucherJobGroupHandler.class,
                            beanMethod = "showUnMappedJobGroupsAgainstVoucher",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showUnMappedJobGroupsAgainstVoucher",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveJobGroupEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Job Groups that are Not Mapped for given Voucher",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY,name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY,name = "skw", description = "Search with name"),
                                            @Parameter(in = ParameterIn.QUERY,name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/voucher-job-group/mapped/show/{voucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = VoucherJobGroupHandler.class,
                            beanMethod = "showUnMappedJobGroupsAgainstVoucher",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showUnMappedJobGroupsAgainstVoucher",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveJobGroupEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Job Groups that are Mapped for given Voucher",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY,name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY,name = "skw", description = "Search with name"),
                                            @Parameter(in = ParameterIn.QUERY,name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/voucher-job-group/store/{voucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = VoucherJobGroupHandler.class,
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
                                                            implementation = JobGroupEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    requestBody = @RequestBody(
                                            description = "Create Job Groups for a Voucher",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = VoucherJobGroupDocImpl.class)
                                            )),
                                    description = "Store Job Groups that are mapped for given Voucher",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/account/api/v1/voucher-job-group/delete/{voucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = VoucherJobGroupHandler.class,
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
                                                            implementation = JobGroupEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Delete Job Group Against Voucher",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    },
                                    requestBody = @RequestBody(
                                            description = "Delete Job Group for a Voucher",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = JobGroupDocImpl.class)
                                            ))
                            )
                    )
            }
    )

    public RouterFunction<ServerResponse> voucherJobGroupRoutes(VoucherJobGroupHandler handle) {
        return RouterFunctions.route(GET("account/api/v1/voucher-job-group/un-mapped/show/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showUnMappedJobGroupsAgainstVoucher).filter(new ShowVoucherJobGroupPvtHandlerFilter())
                .and(RouterFunctions.route(GET("account/api/v1/voucher-job-group/mapped/show/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showMappedJobGroupsAgainstVoucher).filter(new ShowVoucherJobGroupPvtHandlerFilter()))
                .and(RouterFunctions.route(POST("account/api/v1/voucher-job-group/store/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreVoucherJobGroupPvtHandlerFilter()))
                .and(RouterFunctions.route(DELETE("account/api/v1/voucher-job-group/delete/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteVoucherJobGroupPvtHandlerFilter()));
    }

}
