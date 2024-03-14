package tuf.webscaf.app.http.validationFilters.incomeSummaryHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.util.ArrayList;
import java.util.List;


public class UpdateIncomeSummaryHandlerFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {

        return request.formData()
                .flatMap(value -> {
                    List<AppResponseMessage> messages = new ArrayList<>();

                    if (value.getFirst("name").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Name Field is Required"
                                )
                        );
                    }

                    if (!value.getFirst("name")
                            .matches("^[a-zA-Z]+(?:\\s+[a-zA-Z]+)*$")) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "No Digits and Special Characters are allowed In name Field!"
                                )
                        );
                    }

                    if (value.getFirst("closingDate").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Closing Date Field is Required"
                                )
                        );
                    }

                    if (!value.getFirst("closingDate").matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}.\\d{2}.\\d{2}(\\.\\d+)?$")) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Closing Date will contain only Numeric Values"
                                )
                        );
                    }

                    if (value.getFirst("calendarId").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Calendar Id Field is Required"
                                )
                        );
                    }
                    if (!value.getFirst("calendarId").matches("^[0-9]*$")) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Calendar Id will contain only Numeric Values"
                                )
                        );
                    }

                    if (messages.isEmpty() != true) {
                        CustomResponse appresponse = new CustomResponse();
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

                    return next.handle(request);
                });
    }
}


