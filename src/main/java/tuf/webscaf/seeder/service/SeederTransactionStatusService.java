package tuf.webscaf.seeder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.util.ArrayList;
import java.util.List;

@Service
public class SeederTransactionStatusService {

    @Autowired
    SeederService seederService;

    @Autowired
    CustomResponse appresponse;

    public Mono<ServerResponse> seedTransactionStatusData(String url){

        ArrayList<MultiValueMap<String, String>> list = new ArrayList<>();
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", "Pending");
        formData.add("description", "Pending Transaction");
        formData.add("slug","pending");
        formData.add("status", "true");
        list.add(formData);

        MultiValueMap<String, String> formData1 = new LinkedMultiValueMap<>();
        formData1.add("name", "Recorded");
        formData1.add("description", "Recorded Transaction");
        formData1.add("slug","recorded");
        formData1.add("status", "true");
        list.add(formData1);

        MultiValueMap<String, String> formData2 = new LinkedMultiValueMap<>();
        formData2.add("name", "Cleared");
        formData2.add("description", "Cleared Transaction");
        formData2.add("slug","cleared");
        formData2.add("status", "true");
        list.add(formData2);

        MultiValueMap<String, String> formData3 = new LinkedMultiValueMap<>();
        formData3.add("name", "Reconciled");
        formData3.add("description", "Reconciled Transaction");
        formData3.add("slug","reconciled");
        formData3.add("status", "true");
        list.add(formData3);

        MultiValueMap<String, String> formData4 = new LinkedMultiValueMap<>();
        formData4.add("name", "Voided");
        formData4.add("description", "Voided Transaction");
        formData4.add("slug","voided");
        formData4.add("status", "true");
        list.add(formData4);


        Flux<Boolean> flux = Flux.just(false);
        for(int i=0; i< list.size(); i++){
            Mono<Boolean> res = seederService.seedData(url, list.get(i));
            flux = flux.concatWith(res);
        }
        return flux.last()
                .flatMap(aBoolean -> {
                    List<AppResponseMessage> messages = new ArrayList<>();
                    if(aBoolean){
                        messages = List.of(
                                new AppResponseMessage(
                                        AppResponse.Response.SUCCESS,
                                        "Successful")
                        );
                    }else {
                        messages = List.of(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Unsuccessful")
                        );
                    }

                    return appresponse.set(
                            HttpStatus.OK.value(),
                            HttpStatus.OK.name(),
                            null,
                            "eng",
                            "token",
                            0L,
                            0L,
                            messages,
                            Mono.just(aBoolean)
                    );
                });
    }
}