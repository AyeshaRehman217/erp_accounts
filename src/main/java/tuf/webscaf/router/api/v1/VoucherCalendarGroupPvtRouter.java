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
import tuf.webscaf.app.dbContext.master.entity.CalendarGroupEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarGroupEntity;
import tuf.webscaf.app.http.handler.VoucherCalendarGroupPvtHandler;
import tuf.webscaf.app.http.validationFilters.voucherCalendarGroupHandler.DeleteVoucherCalendarGroupPvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.voucherCalendarGroupHandler.ShowVoucherCalendarGroupPvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.voucherCalendarGroupHandler.StoreVoucherCalendarGroupPvtHandlerFilter;
import tuf.webscaf.springDocImpl.CalendarGroupDocImpl;
import tuf.webscaf.springDocImpl.VoucherCalendarGroupDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;


@Configuration
public class VoucherCalendarGroupPvtRouter {
//
//    @Bean
//    @RouterOperations(
//            {
//                    @RouterOperation(
//                            path = "/account/api/v1/voucher-calendar-group/un-mapped/show/{voucherUUID}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = VoucherCalendarGroupPvtHandler.class,
//                            beanMethod = "showUnMappedCalendarGroupsAgainstVoucher",
////                            consumes = { "APPLICATION_FORM_URLENCODED" },
//                            operation = @Operation(
//                                    operationId = "showUnMappedCalendarGroupsAgainstVoucher",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveCalendarGroupEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    description = "Show Calendar Groups that are not Mapped for given Voucher",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
//                                            @Parameter(in = ParameterIn.QUERY,name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
//                                            @Parameter(in = ParameterIn.QUERY,name = "skw", description = "Search with name"),
//                                            @Parameter(in = ParameterIn.QUERY,name = "status")
//                                    }
//                            )
//                    ),
//                    @RouterOperation(
//                            path = "/account/api/v1/voucher-calendar-group/mapped/show/{voucherUUID}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = VoucherCalendarGroupPvtHandler.class,
//                            beanMethod = "showMappedCalendarGroupsAgainstVoucher",
////                            consumes = { "APPLICATION_FORM_URLENCODED" },
//                            operation = @Operation(
//                                    operationId = "showMappedCalendarGroupsAgainstVoucher",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveCalendarGroupEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    description = "Show Calendar Groups that are Mapped for given Voucher",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
//                                            @Parameter(in = ParameterIn.QUERY,name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
//                                            @Parameter(in = ParameterIn.QUERY,name = "skw", description = "Search with name"),
//                                            @Parameter(in = ParameterIn.QUERY,name = "status")
//                                    }
//                            )
//                    ),
//                    @RouterOperation(
//                            path = "/account/api/v1/voucher-calendar-group/store/{voucherUUID}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.POST,
//                            beanClass = VoucherCalendarGroupPvtHandler.class,
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
//                                                            implementation = CalendarGroupEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    description = "Store Calendar Groups for given Voucher",
//                                    requestBody = @RequestBody(
//                                            description = "Create Calendar Groups for a Voucher",
//                                            required = true,
//                                            content = @Content(
////                                                    mediaType = "multipart/form-data",
//                                                    mediaType = "application/x-www-form-urlencoded",
//                                                    encoding = {
//                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
//                                                    },
//                                                    schema = @Schema(type = "object", implementation = VoucherCalendarGroupDocImpl.class)
//                                            )),
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
//                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
//                                    }
//                            )
//                    ),
//
//                    @RouterOperation(
//                            path = "/account/api/v1/voucher-calendar-group/delete/{voucherUUID}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.DELETE,
//                            beanClass = VoucherCalendarGroupPvtHandler.class,
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
//                                                            implementation = CalendarGroupEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    description = "Delete Calendar Groups for given Voucher",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
//                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
//                                    },
//                                    requestBody = @RequestBody(
//                                            description = "Delete Calendar Group for a Voucher",
//                                            required = true,
//                                            content = @Content(
////                                                    mediaType = "multipart/form-data",
//                                                    mediaType = "application/x-www-form-urlencoded",
//                                                    encoding = {
//                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
//                                                    },
//                                                    schema = @Schema(type = "object", implementation = CalendarGroupDocImpl.class)
//                                            ))
//                            )
//                    )
//            }
//    )
//
//    public RouterFunction<ServerResponse> voucherCalendarGroupRoutes(VoucherCalendarGroupPvtHandler handle) {
//        return RouterFunctions.route(GET("account/api/v1/voucher-calendar-group/un-mapped/show/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showUnMappedCalendarGroupsAgainstVoucher).filter(new ShowVoucherCalendarGroupPvtHandlerFilter())
//                .and(RouterFunctions.route(GET("account/api/v1/voucher-calendar-group/mapped/show/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showMappedCalendarGroupsAgainstVoucher).filter(new ShowVoucherCalendarGroupPvtHandlerFilter()))
//                .and(RouterFunctions.route(POST("account/api/v1/voucher-calendar-group/store/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreVoucherCalendarGroupPvtHandlerFilter()))
//                .and(RouterFunctions.route(DELETE("account/api/v1/voucher-calendar-group/delete/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteVoucherCalendarGroupPvtHandlerFilter()));
//    }

}
