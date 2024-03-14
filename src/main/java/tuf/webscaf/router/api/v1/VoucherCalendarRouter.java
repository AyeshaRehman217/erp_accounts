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
//import tuf.webscaf.app.dbContext.master.entity.CalendarEntity;
//import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherCalendarPvtEntity;
//import tuf.webscaf.app.http.handler.VoucherCalendarPvtHandler;
//import tuf.webscaf.springDocImpl.CalendarDocImpl;
//
//import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
//import static org.springframework.web.reactive.function.server.RequestPredicates.*;
//
//
//@Configuration
//public class VoucherCalendarRouter {
//
//    @Bean
//    @RouterOperations(
//            {
//                    @RouterOperation(
//                            path = "/account/api/v1/voucher-calendar/existing/show/{voucherId}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = VoucherCalendarPvtHandler.class,
//                            beanMethod = "show",
////                            consumes = { "APPLICATION_FORM_URLENCODED" },
//                            operation = @Operation(
//                                    operationId = "show",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveVoucherCalendarPvtEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    description = "Show Calendar that are not mapped for given Voucher",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "voucherId"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "status")
//                                    }
//                            )
//                    ),
//                    @RouterOperation(
//                            path = "/account/api/v1/voucher-calendar/store/{voucherId}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.POST,
//                            beanClass = VoucherCalendarPvtHandler.class,
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
//                                                            implementation = CalendarEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    requestBody = @RequestBody(
//                                            description = "Create Calendar for a Voucher",
//                                            required = true,
//                                            content = @Content(
////                                                    mediaType = "multipart/form-data",
//                                                    mediaType = "application/x-www-form-urlencoded",
//                                                    encoding = {
//                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
//                                                    },
//                                                    schema = @Schema(type = "object", implementation = CalendarDocImpl.class)
//                                            )),
//                                    description = "Store Calendar for given Voucher",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "voucherId"),
//                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
//                                    }
//                            )
//                    )
//
////                    @RouterOperation(
////                            path = "/account/api/v1/voucher-calendar/delete/{voucherTypeId}",
////                            produces = {
////                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
////                            },
////                            method = RequestMethod.DELETE,
////                            beanClass = VoucherTypeCalendarPvtHandler.class,
////                            beanMethod = "delete",
//////                            consumes = { "APPLICATION_FORM_URLENCODED" },
////                            operation = @Operation(
////                                    operationId = "delete",
////                                    security = {@SecurityRequirement(name = "bearer")},
////                                    responses = {
////                                            @ApiResponse(
////                                                    responseCode = "200",
////                                                    description = "successful operation",
////                                                    content = @Content(schema = @Schema(
////                                                            implementation = CalendarEntity.class
////                                                    ))
////                                            ),
////                                            @ApiResponse(responseCode = "404", description = "Records not found!",
////                                                    content = @Content(schema = @Schema(hidden = true))
////                                            )
////                                    },
////                                    description = "Delete Calendar for given Voucher Type",
////                                    parameters = {
////                                            @Parameter(in = ParameterIn.PATH, name = "voucherTypeId"),
////                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
////                                    },
////                                    requestBody = @RequestBody(
////                                            description = "Delete Profit Center Group for a Voucher",
////                                            required = true,
////                                            content = @Content(
//////                                                    mediaType = "multipart/form-data",
////                                                    mediaType = "application/x-www-form-urlencoded",
////                                                    encoding = {
////                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
////                                                    },
////                                                    schema = @Schema(type = "object", implementation = ProfitCenterGroupDocImpl.class)
////                                            ))
////                            )
////                    )
//            }
//    )
//
//    public RouterFunction<ServerResponse> voucherTypeCalendarRoutes(VoucherCalendarPvtHandler handle) {
//        return RouterFunctions.route(GET("account/api/v1/voucher-calendar/existing/show/{voucherId}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show)
//                .and(RouterFunctions.route(POST("account/api/v1/voucher-calendar/store/{voucherId}").and(accept(APPLICATION_FORM_URLENCODED)), handle::store));
////                .and(RouterFunctions.route(DELETE("account/api/v1/voucher-calendar/delete/{voucherTypeId}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete));
//    }
//
//}
