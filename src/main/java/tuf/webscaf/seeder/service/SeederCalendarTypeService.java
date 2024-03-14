package tuf.webscaf.seeder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import tuf.webscaf.config.service.response.CustomResponse;


@Service
public class SeederCalendarTypeService {

    @Autowired
    SeederService seederService;

    @Autowired
    CustomResponse appresponse;

    public Mono<String> seedCalendarTypeData(String url,MultiValueMap<String, String> formData){

        return seederService.seedData(url,formData)
                .flatMap(aBoolean -> Mono.just(""));
    }
}