package com.velora.backend.config;

import com.velora.backend.user.Role;
import com.velora.backend.user.User;
import com.velora.backend.user.UserRepository;
import com.velora.backend.vehicle.Vehicle;
import com.velora.backend.vehicle.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, VehicleRepository vehicleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            User demoUser = userRepository.findByEmail("driver@drivemind.ai").orElseGet(() -> {
                User user = User.builder()
                        .name("Guardian Driver")
                        .email("driver@drivemind.ai")
                        .password(passwordEncoder.encode("password123"))
                        .role(Role.ROLE_DRIVER)
                        .enabled(true)
                        .build();
                return userRepository.save(user);
            });

            if (vehicleRepository.findByOwnerId(demoUser.getId()).isEmpty()) {
                Vehicle vehicle = Vehicle.builder()
                        .owner(demoUser)
                        .make("Tata")
                        .model("Nexon EV Dark Edition")
                        .year("2024")
                        .vin("MAT839219K92019")
                        .registrationNumber("DL 01 AB 1234")
                        .obdProtocol("ISO 15765-4 CAN")
                        .build();
                vehicleRepository.save(vehicle);
                log.info("Initialized default demo vehicle for user {}", demoUser.getEmail());
            }
        };
    }
}
