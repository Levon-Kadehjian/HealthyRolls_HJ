package com.healthyrolls.technicalservices.configuration;

import com.healthyrolls.domain.common.UserRole;
import com.healthyrolls.domain.dlv.courier.Courier;
import com.healthyrolls.domain.dlv.courier.CourierStatus;
import com.healthyrolls.domain.dlv.courier.ICourierRepository;
import com.healthyrolls.domain.reg.IUserRepository;
import com.healthyrolls.domain.reg.User;
import com.healthyrolls.domain.reg.seed.SeedUser;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SeedCourierLoader {
    private static final Logger log = LoggerFactory.getLogger(SeedCourierLoader.class);

    private final ApplicationArguments args;
    private final SeedUserProperties props;
    private final IUserRepository userRepository;
    private final ICourierRepository courierRepository;

    public SeedCourierLoader(
            ApplicationArguments args,
            SeedUserProperties props,
            IUserRepository userRepository,
            ICourierRepository courierRepository) {
        this.args = args;
        this.props = props;
        this.userRepository = userRepository;
        this.courierRepository = courierRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(0)
    @Transactional
    public void run() {
        if (!args.containsOption("seed")) {
            log.info("No '--seed' argument given, skipping courier seeding");
            return;
        }
        long couriers = props.getUsers().stream()
                .filter(seed -> seed.role() == UserRole.COURIER)
                .count();
        long created = seedCouriers();
        log.info("Seeded {} of {} couriers from {}", created, couriers, "seed/users.json");
    }

    public long seedCouriers() {
        long created = 0;
        for (SeedUser seed : props.getUsers()) {
            if (seed.role() == UserRole.COURIER && createCourier(seed)) {
                created++;
            }
        }
        return created;
    }

    private boolean createCourier(SeedUser seed) {
        if (seed.latitude() == null || seed.longitude() == null) {
            log.warn("Courier {} has no position in seed, skipping", seed.email());
            return false;
        }
        Optional<User> user = userRepository.findByEmail(seed.email());
        if (user.isEmpty()) {
            log.warn("No user account for courier {}, skipping", seed.email());
            return false;
        }
        if (courierRepository.findById(user.get().id()).isPresent()) {
            return false;
        }
        courierRepository.save(new Courier(
                user.get().id(),
                user.get().name(),
                seed.latitude(),
                seed.longitude(),
                CourierStatus.OFFLINE,
                null));
        return true;
    }
}