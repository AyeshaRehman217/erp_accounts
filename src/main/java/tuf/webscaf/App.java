package tuf.webscaf;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        info = @Info(
                title = "Account Module",
                version = "1.0.0",
                description = "This is financial accounting module based on double entry system. In this module record monitory transactions " +
                        "and create Financial reports like Profit & Loss Statement and Balance sheet."
//                license = @License(name = "Apache 2.0", url = "https://foo.bar"),
//                contact = @Contact(url = "https://gigantic-server.com", name = "Fred", email = "Fred@gigagantic-server.com")
        ),
        servers = {
                @Server(url = "https://172.15.10.195/", description = "Default Server URL")
        }

)
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}