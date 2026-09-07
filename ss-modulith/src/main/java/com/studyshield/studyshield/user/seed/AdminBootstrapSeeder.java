package com.studyshield.studyshield.user.seed;

import com.studyshield.studyshield.user.entity.User;
import com.studyshield.studyshield.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Creates the very first ADMIN-type account when none exists. Enabled via
 * {@code app.admin-bootstrap.enabled} (default: disabled so tests and existing
 * deployments are untouched). Supply fixed credentials with
 * {@code app.admin-bootstrap.email} / {@code app.admin-bootstrap.password};
 * otherwise a random password is generated and logged once at startup.
 */
@Component
public class AdminBootstrapSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapSeeder.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserService userService;

    @Value("${app.admin-bootstrap.enabled:false}")
    private boolean enabled;

    @Value("${app.admin-bootstrap.email:}")
    private String configuredEmail;

    @Value("${app.admin-bootstrap.password:}")
    private String configuredPassword;

    public AdminBootstrapSeeder(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("[AdminBootstrap] Disabled via app.admin-bootstrap.enabled=false — skipping");
            return;
        }
        if (userService.countByType(User.UserType.ADMIN) > 0) {
            log.info("[AdminBootstrap] ADMIN account already exists — skipping");
            return;
        }

        String email = configuredEmail.isBlank() ? "admin@studyshield.local" : configuredEmail.trim();
        String password = configuredPassword.isBlank()
                ? randomPassword() : configuredPassword;

        userService.createAdminUser(email, password, "System Administrator", null, true);
        if (configuredPassword.isBlank()) {
            log.warn("[AdminBootstrap] Created initial admin account. EMAIL={} PASSWORD={} " +
                    "— change it after first login or set app.admin-bootstrap.password " +
                    "(env ADMIN_BOOTSTRAP_PASSWORD) to make it deterministic.", email, password);
        } else {
            log.info("[AdminBootstrap] Created initial admin account from configuration. EMAIL={}", email);
        }
    }

    private String randomPassword() {
        byte[] bytes = new byte[12];
        RANDOM.nextBytes(bytes);
        return "A" + HexFormat.of().formatHex(bytes) + "!";
    }
}