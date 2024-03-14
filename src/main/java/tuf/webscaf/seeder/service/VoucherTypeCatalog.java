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
public class VoucherTypeCatalog {


    @Autowired
    SeederService seederService;

    @Autowired
    CustomResponse appresponse;


    public Mono<ServerResponse> seedVoucherTypeCatalogue(String url){

        ArrayList<MultiValueMap<String, String>> list = new ArrayList<>();
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", "Cash Receipt Voucher");
        formData.add("description", "Cash Receipt Voucher");
        formData.add("status", "true");
        list.add(formData);

        MultiValueMap<String, String> formData1 = new LinkedMultiValueMap<>();
        formData1.add("name", "Bank Receipt Voucher");
        formData1.add("description", "Bank Receipt Voucher");
        formData1.add("status", "true");
        list.add(formData1);


        MultiValueMap<String, String> formData2 = new LinkedMultiValueMap<>();
        formData2.add("name", "Cash Payment Voucher");
        formData2.add("description", "Cash Payment Voucher");
        formData2.add("status", "true");
        list.add(formData2);

        MultiValueMap<String, String> formData3 = new LinkedMultiValueMap<>();
        formData3.add("name", "Bank Payment Voucher");
        formData3.add("description", "Bank Payment Voucher");
        formData3.add("status", "true");
        list.add(formData3);


        MultiValueMap<String, String> formData4 = new LinkedMultiValueMap<>();
        formData4.add("name", "Journal Voucher");
        formData4.add("description", "Journal Voucher");
        formData4.add("status", "true");
        list.add(formData4);



        MultiValueMap<String, String> formData5 = new LinkedMultiValueMap<>();
        formData5.add("name", "Sale Voucher");
        formData5.add("description", "Sale Voucher");
        formData5.add("status", "true");
        list.add(formData5);

        MultiValueMap<String, String> formData6 = new LinkedMultiValueMap<>();
        formData6.add("name", "Sale Return Voucher");
        formData6.add("description", "Sale Return Voucher");
        formData6.add("status", "true");
        list.add(formData6);

        MultiValueMap<String, String> formData7 = new LinkedMultiValueMap<>();
        formData7.add("name", "Purchase Voucher");
        formData7.add("description", "Purchase Voucher");
        formData7.add("status", "true");
        list.add(formData7);

        MultiValueMap<String, String> formData8 = new LinkedMultiValueMap<>();
        formData8.add("name", "Purchase Return Voucher");
        formData8.add("description", "Purchase Return Voucher");
        formData8.add("status", "true");
        list.add(formData8);



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
