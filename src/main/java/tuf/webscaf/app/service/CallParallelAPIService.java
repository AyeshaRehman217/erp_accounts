package tuf.webscaf.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import javax.net.ssl.SSLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CallParallelAPIService {

    @Value("${server.ssl-status}")
    private String sslStatus;

    @Value("${webclient.backend.token}")
    private String token;

    public Mono<List<JsonNode>> getInParallel(ArrayList<String> urls) {
        return Flux.fromIterable(urls)
                .parallel()
                .flatMap(this::getData)
                .ordered((o1, o2) -> {
                    return 0;
                })
                .collectList();
//                .toStream()
    }

    public Mono<JsonNode> getData(String url){
        WebClient webClient = initWebClient();
        return webClient.get()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_FORM_URLENCODED))
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+token)
                .retrieve()
                .bodyToMono(JsonNode.class).flatMap(value -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = null;
                    JsonNode sendJsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(value.toString());
                        Integer status = Integer.valueOf(jsonNode.get("status").toString());
                        if (status.equals(200)) {
                            JsonNode objectNode = jsonNode.get("appResponse");
                            JsonNode arrNode = objectNode.get("message");
                            Integer msgCode = Integer.valueOf(arrNode.get(0).get("messageCode").toString());
                            if (msgCode == 99200) {
                                JsonNode dataNode = jsonNode.get("data");
                                if (dataNode.isArray()) {
                                    for (JsonNode objNode : dataNode) {
                                        sendJsonNode = objNode;
                                    }
                                }
                                return Mono.just(sendJsonNode);
                            }
                        }
                    } catch (JsonProcessingException e) {
                        return Mono.empty();
                    }
                    return Mono.empty();
                });
    }

    public WebClient initWebClient() {
        try {
            SslContext context = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            HttpClient httpClient = HttpClient.create()
                    .secure(t -> t.sslContext(context));

            if (sslStatus.equals("enable")) {
                return WebClient.builder()
                        .clientConnector(
                                new ReactorClientHttpConnector(httpClient)
                        )
                        .build();
            } else {
                return WebClient.builder()
                        .build();
            }
        } catch (SSLException e) {
            return WebClient.builder()
                    .build();
        }
    }

}
