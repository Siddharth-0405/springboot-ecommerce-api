package com.productservice.config;

import com.productservice.model.Category;
import com.productservice.model.Product;
import com.productservice.model.Role;
import com.productservice.model.User;
import com.productservice.repository.CategoryRepository;
import com.productservice.repository.ProductRepository;
import com.productservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    @Bean
    @Profile("!test")
    CommandLineRunner seedDatabase(
            UserRepository userRepo,
            CategoryRepository categoryRepo,
            ProductRepository productRepo,
            PasswordEncoder passwordEncoder) {
        return args -> {

            // ── Seed Admin User ───────────────────────────────────────────────
            if (!userRepo.existsByEmail("admin@store.com")) {
                userRepo.save(User.builder()
                        .firstName("Admin").lastName("User")
                        .email("admin@store.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ROLE_ADMIN).active(true).build());
                log.info("Admin user created → email: admin@store.com | password: admin123");
            }

            if (!userRepo.existsByEmail("user@store.com")) {
                userRepo.save(User.builder()
                        .firstName("John").lastName("Doe")
                        .email("user@store.com")
                        .password(passwordEncoder.encode("user123"))
                        .role(Role.ROLE_USER).active(true).build());
                log.info("Sample user created → email: user@store.com | password: user123");
            }

            // ── Seed Categories ───────────────────────────────────────────────
            if (categoryRepo.count() == 0) {
                Category electronics = categoryRepo.save(Category.builder()
                        .name("Electronics").description("Gadgets and electronic devices").build());
                Category peripherals = categoryRepo.save(Category.builder()
                        .name("Peripherals").description("Computer peripherals and accessories").build());
                Category audio = categoryRepo.save(Category.builder()
                        .name("Audio").description("Speakers, headphones and audio gear").build());
                log.info("Categories seeded");

                // ── Seed Products ─────────────────────────────────────────────
                if (productRepo.count() == 0) {
                    productRepo.saveAll(List.of(
                            Product.builder().name("iPhone 15 Pro").description("Apple flagship smartphone")
                                    .price(new BigDecimal("999.99")).stockQuantity(50).sku("APPL-001")
                                    .category(electronics).active(true).build(),
                            Product.builder().name("Samsung Galaxy S24").description("Android flagship")
                                    .price(new BigDecimal("899.99")).stockQuantity(45).sku("SAMS-001")
                                    .category(electronics).active(true).build(),
                            Product.builder().name("Mechanical Keyboard").description("TKL with Cherry MX switches")
                                    .price(new BigDecimal("129.99")).stockQuantity(80).sku("KB-001")
                                    .category(peripherals).active(true).build(),
                            Product.builder().name("Ergonomic Mouse").description("Vertical ergonomic mouse")
                                    .price(new BigDecimal("59.99")).stockQuantity(7).sku("MS-001")
                                    .category(peripherals).active(true).build(),
                            Product.builder().name("Noise-Cancelling Headphones").description("30h battery ANC headphones")
                                    .price(new BigDecimal("299.99")).stockQuantity(0).sku("HDPH-001")
                                    .category(audio).active(true).build()
                    ));
                    log.info("Sample products seeded");
                }
            }
        };
    }
}
