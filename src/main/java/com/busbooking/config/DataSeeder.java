package com.busbooking.config;

import com.busbooking.dto.BusRequest;
import com.busbooking.entity.Role;
import com.busbooking.entity.User;
import com.busbooking.repository.BusRepository;
import com.busbooking.repository.UserRepository;
import com.busbooking.service.BusService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** Seeds an admin user and a few sample buses on first run (empty database only). */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BusRepository busRepository;
    private final BusService busService;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRepository.save(User.builder()
                    .name("Admin").email("admin@bus.com")
                    .password(encoder.encode("admin123"))
                    .phone("9999999999").role(Role.ADMIN).build());
        }
        if (busRepository.count() == 0) {
            LocalDate d1 = LocalDate.now().plusDays(1);
            LocalDate d2 = LocalDate.now().plusDays(2);
            busService.create(new BusRequest("Orange Travels", "AC Sleeper", "Hyderabad", "Bangalore",
                    d1, LocalTime.of(21, 0), LocalTime.of(5, 30), new BigDecimal("1200"), 40));
            busService.create(new BusRequest("VRL Express", "Non-AC Seater", "Hyderabad", "Bangalore",
                    d1, LocalTime.of(22, 30), LocalTime.of(7, 0), new BigDecimal("800"), 32));
            busService.create(new BusRequest("KSRTC Airavat", "AC Semi-Sleeper", "Hyderabad", "Chennai",
                    d2, LocalTime.of(20, 0), LocalTime.of(6, 0), new BigDecimal("1000"), 36));
            busService.create(new BusRequest("SRS Travels", "AC Sleeper", "Bangalore", "Hyderabad",
                    d2, LocalTime.of(21, 30), LocalTime.of(6, 0), new BigDecimal("1100"), 40));
        }
    }
}
