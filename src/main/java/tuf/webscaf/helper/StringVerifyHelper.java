package tuf.webscaf.helper;

import org.springframework.stereotype.Service;

@Service
public class StringVerifyHelper {

    // This method is used to check if a string is null, empty or blank
    public static Boolean isNullEmptyOrBlank(String str){
        return str == null || str.isEmpty() || str.isBlank();
    }

}
