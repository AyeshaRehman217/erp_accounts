//package tuf.webscaf.router.api.v1;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.enums.ParameterIn;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Encoding;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.parameters.RequestBody;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import org.springdoc.core.annotations.RouterOperation;
//import org.springdoc.core.annotations.RouterOperations;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.reactive.function.server.RouterFunction;
//import org.springframework.web.reactive.function.server.RouterFunctions;
//import org.springframework.web.reactive.function.server.ServerResponse;
//import tuf.webscaf.app.dbContext.master.dto.CompanyWithCompanyProfileDto;
//import tuf.webscaf.app.dbContext.master.entity.CompanyEntity;
//import tuf.webscaf.app.dbContext.slave.entity.SlaveCompanyEntity;
//import tuf.webscaf.app.http.handler.VoucherCompanyHandler;
//import tuf.webscaf.app.http.validationFilters.voucherCompanyPvtHandler.CheckCompanyListHandlerFilter;
//import tuf.webscaf.app.http.validationFilters.voucherCompanyPvtHandler.DeleteVoucherCompanyPvtHandlerFilter;
//import tuf.webscaf.app.http.validationFilters.voucherCompanyPvtHandler.ShowVoucherCompanyPvtMappedListHandlerFilter;
//import tuf.webscaf.app.http.validationFilters.voucherCompanyPvtHandler.StoreVoucherCompanyPvtHandlerFilter;
//import tuf.webscaf.springDocImpl.VoucherCompanyDocImpl;
//
//import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
//import static org.springframework.web.reactive.function.server.RequestPredicates.*;
//
//
//@Configuration
//public class VoucherCompanyRouter {
//
//    @Bean
//    @RouterOperations(
//            {
//                    @RouterOperation(
//                            path = "/account/api/v1/voucher-company/company/show/{companyUUID}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = VoucherCompanyHandler.class,
//                            beanMethod = "getCompanyUUID",
//                            operation = @Operation(
//                                    operationId = "getCompanyUUID",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveCompanyEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    description = "This Route is used by Config Module Company Delete Function to Check If Company Mapped With Vouchers",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "companyUUID")
//                                    }
//                            )
//                    ),
//                    @RouterOperation(
//                            path = "/account/api/v1/voucher-company/list/show/{voucherUUID}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = VoucherCompanyHandler.class,
//                            beanMethod = "showList",
//                            operation = @Operation(
//                                    operationId = "showList",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = CompanyWithCompanyProfileDto.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    description = "Show the list of Company Ids that are mapped for given Voucher",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID")
//                                    }
//                            )
//                    ),
//                    @RouterOperation(
//                            path = "/account/api/v1/voucher-company/store/{voucherUUID}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.POST,
//                            beanClass = VoucherCompanyHandler.class,
//                            beanMethod = "store",
////                            consumes = { "APPLICATION_FORM_URLENCODED" },
//                            operation = @Operation(
//                                    operationId = "store",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = CompanyEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    requestBody = @RequestBody(
//                                            description = "Create Companies for a Voucher",
//                                            required = true,
//                                            content = @Content(
////                                                    mediaType = "multipart/form-data",
//                                                    mediaType = "application/x-www-form-urlencoded",
//                                                    encoding = {
//                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
//                                                    },
//                                                    schema = @Schema(type = "object", implementation = VoucherCompanyDocImpl.class)
//                                            )),
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.HEADER, name = "auid"),
//                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID")
//
//                                    }
//                            )
//                    ),
//
//                    @RouterOperation(
//                            path = "/account/api/v1/voucher-company/delete/{voucherUUID}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.DELETE,
//                            beanClass = VoucherCompanyHandler.class,
//                            beanMethod = "delete",
////                            consumes = { "APPLICATION_FORM_URLENCODED" },
//                            operation = @Operation(
//                                    operationId = "delete",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = CompanyWithCompanyProfileDto.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    description = "Delete Companies Against a Given Voucher",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "companyUUID"),
//                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
//                                    }
//                            )
//                    )
//            }
//    )
//
//    public RouterFunction<ServerResponse> voucherCompanyRoutes(VoucherCompanyHandler handle) {
//        return RouterFunctions.route(GET("account/api/v1/voucher-company/company/show/{companyUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::getCompanyUUID).filter(new CheckCompanyListHandlerFilter())
//                .and(RouterFunctions.route(GET("account/api/v1/voucher-company/list/show/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showList).filter(new ShowVoucherCompanyPvtMappedListHandlerFilter()))
//                .and(RouterFunctions.route(POST("account/api/v1/voucher-company/store/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreVoucherCompanyPvtHandlerFilter()))
//                .and(RouterFunctions.route(DELETE("account/api/v1/voucher-company/delete/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteVoucherCompanyPvtHandlerFilter()));
//    }
//
//
//}
