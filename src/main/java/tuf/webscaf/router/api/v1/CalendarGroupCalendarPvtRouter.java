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
import tuf.webscaf.app.dbContext.master.entity.CalendarEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarEntity;
import tuf.webscaf.app.http.handler.CalendarGroupCalendarPvtHandler;
import tuf.webscaf.app.http.validationFilters.calendarGroupCalendarHandler.DeleteCalendarGroupCalendarPvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.calendarGroupCalendarHandler.ShowCalendarGroupCalendarPvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.calendarGroupCalendarHandler.StoreCalendarGroupCalendarPvtHandlerFilter;
import tuf.webscaf.springDocImpl.CalendarDocImpl;
import tuf.webscaf.springDocImpl.CalendarGroupCalendarImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class CalendarGroupCalendarPvtRouter {
    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/account/api/v1/calendar-group-calendar/un-mapped/show/{calendarGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CalendarGroupCalendarPvtHandler.class,
                            beanMethod = "showUnMappedCalendarsAgainstCalendarGroup",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showUnMappedCalendarsAgainstCalendarGroup",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveCalendarEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Calendars that are not Mapped against given Calendar Group",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "calendarGroupUUID"),
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
                            path = "/account/api/v1/calendar-group-calendar/mapped/show/{calendarGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CalendarGroupCalendarPvtHandler.class,
                            beanMethod = "showMappedCalendarsAgainstCalendarGroup",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showMappedCalendarsAgainstCalendarGroup",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveCalendarEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Calendars that are not Mapped against given Calendar Group",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "calendarGroupUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY,name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY,name = "skw", description = "Search with name or description"),
                                            @Parameter(in = ParameterIn.QUERY,name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/calendar-group-calendar/store/{calendarGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = CalendarGroupCalendarPvtHandler.class,
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
                                                            implementation = CalendarEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Store Multiple Calendars for Calendar Group",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "calendarGroupUUID"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    },
                                    requestBody = @RequestBody(
                                            description = "Create Calendars for a Calendar Group",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = CalendarDocImpl.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/calendar-group-calendar/delete/{calendarGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = CalendarGroupCalendarPvtHandler.class,
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
                                                            implementation = CalendarEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Delete Calendars against Given Calendar Group UUID",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "calendarGroupUUID"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    },
                                    requestBody = @RequestBody(
                                            description = "Delete Calendar for a Calendar Group",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = CalendarGroupCalendarImpl.class)
                                            ))
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> calendarGroupCalendarsRoutes(CalendarGroupCalendarPvtHandler handle) {
        return RouterFunctions.route(GET("account/api/v1/calendar-group-calendar/un-mapped/show/{calendarGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showUnMappedCalendarsAgainstCalendarGroup).filter(new ShowCalendarGroupCalendarPvtHandlerFilter())
                .and(RouterFunctions.route(GET("account/api/v1/calendar-group-calendar/mapped/show/{calendarGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showMappedCalendarsAgainstCalendarGroup).filter(new ShowCalendarGroupCalendarPvtHandlerFilter()))
                .and(RouterFunctions.route(POST("account/api/v1/calendar-group-calendar/store/{calendarGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreCalendarGroupCalendarPvtHandlerFilter()))
                .and(RouterFunctions.route(DELETE("account/api/v1/calendar-group-calendar/delete/{calendarGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteCalendarGroupCalendarPvtHandlerFilter()));
    }

}
