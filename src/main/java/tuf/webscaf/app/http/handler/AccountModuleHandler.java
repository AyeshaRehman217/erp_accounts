package tuf.webscaf.app.http.handler;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAccountModuleDto;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

@Tag(name = "infoHandler")
@Component
public class AccountModuleHandler {
    @Autowired
    CustomResponse appresponse;

    @Autowired
    Environment environment;

    @AuthHasPermission(value = "account_api_v1_info_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {

        String baseUrl = getBaseUrl(String.valueOf(serverRequest.uri()));
        String hostAddress = "";

        try {
            hostAddress = new URL(baseUrl).getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        SlaveAccountModuleDto slaveAccountModuleDto = SlaveAccountModuleDto.builder()
                .moduleId(Long.valueOf(environment.getProperty("server.module.id")))
                .moduleUUID(UUID.fromString(environment.getProperty("server.module.uuid")))
                .baseUrl(getBaseUrl(String.valueOf(serverRequest.uri())))
                .infoUrl(String.valueOf(serverRequest.uri()))
                .hostAddress(hostAddress)
                .build();

        return responseSuccessMsg("Record Fetched Successfully", slaveAccountModuleDto)
                .switchIfEmpty(responseInfoMsg("Record does not exist. There is something wrong please try again."))
                .onErrorResume(err -> responseErrorMsg("Record does not exist. Please Contact Developer."));
    }

    public String getBaseUrl(String uri) {
        return uri.substring(0, uri.indexOf("api"));
    }

    public Mono<ServerResponse> responseSuccessMsg(String msg, Object entity) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.SUCCESS,
                        msg
                )
        );
        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.just(entity)
        );
    }

    public Mono<ServerResponse> responseErrorMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.ERROR,
                        msg
                )
        );

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

    public Mono<ServerResponse> responseInfoMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.INFO,
                        msg
                )
        );
        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.empty()
        );
    }
}
