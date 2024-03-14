package tuf.webscaf.app.http.validationFilters.accountHandler;

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
import java.util.Objects;

public class StoreAccountHandlerFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {

        return request.formData()
                .flatMap(value -> {
                    List<AppResponseMessage> messages = new ArrayList<>();


                    if (value.containsKey("name")) {
                        if (value.getFirst("name").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Name Field Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("name").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Name"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Name Field Required"
                                )
                        );
                    }

                    if (value.containsKey("code")) {
                        if (value.getFirst("code").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Code is Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("code").matches("^[\\sa-zA-Z0-9\\\\+.,:;\\/#$_-]*$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Code. Only Alphanumeric Characters and + _ _ / " + '\\' + " . , : ; # $ Symbols are Allowed"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Code is Required"
                                )
                        );
                    }

                    if (value.containsKey("parentAccountUUID") && (!Objects.equals(value.getFirst("parentAccountUUID"), ""))) {
                        if (!value.getFirst("parentAccountUUID").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Invalid Parent Account"
                                    )
                            );
                        }
                    }

                    if (value.containsKey("description")) {
                        if (!value.getFirst("description").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Invalid Description"
                                    )
                            );
                        }
                    }

                    if (value.containsKey("accountTypeUUID")) {
                        if (value.getFirst("accountTypeUUID").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Account Type Field Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("accountTypeUUID").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Account Type"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Account Type Field Required"
                                )
                        );
                    }

                    if (value.containsKey("openingBalance") && (!Objects.equals(value.getFirst("openingBalance"), ""))) {
                        if (!value.getFirst("openingBalance").matches("^[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+)$")) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Invalid Opening Balance Amount"
                                    )
                            );
                        }
                    }

                    if (value.containsKey("openingBalanceDescription")) {
                        if (!value.getFirst("openingBalanceDescription").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Invalid Opening Balance Description"
                                    )
                            );
                        }
                    }

//                    if (value.containsKey("companyUUID")) {
//                        if (value.getFirst("companyUUID").isEmpty()) {
//                            messages.add(
//                                    new AppResponseMessage(
//                                            AppResponse.Response.ERROR,
//                                            "Company Field Required"
//                                    )
//                            );
//                        } else {
//                            if (!value.getFirst("companyUUID").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//                                messages.add(
//                                        new AppResponseMessage(
//                                                AppResponse.Response.ERROR,
//                                                "Invalid Company"
//                                        )
//                                );
//                            }
//                        }
//                    } else {
//                        messages.add(
//                                new AppResponseMessage(
//                                        AppResponse.Response.ERROR,
//                                        "Company Field Required"
//                                )
//                        );
//                    }


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
