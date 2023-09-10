package com.ycash.server;

import com.ycash.server.dto.UserRegisterRequestDTO;
import com.ycash.server.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static com.ycash.server.enums.Role.ADMIN;
import static com.ycash.server.enums.Role.MANAGER;

@SpringBootApplication
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(
            UserService service
    ) {
        return args -> {
            UserRegisterRequestDTO admin = new UserRegisterRequestDTO(
                    "Admin",
                    "Admin",
                    "admin@mail.com",
                    "password",
                    ADMIN,
                    false
            );
            System.out.println("Admin token: " + service.registerUser(admin).accessToken());

            UserRegisterRequestDTO manager = new UserRegisterRequestDTO(
                    "Admin",
                    "Admin",
                    "manager@mail.com",
                    "password",
                    MANAGER,
                    false
            );
            System.out.println("Manager token: " + service.registerUser(manager).accessToken());
        };
    }
}
