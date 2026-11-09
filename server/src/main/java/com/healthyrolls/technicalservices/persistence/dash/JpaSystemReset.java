package com.healthyrolls.technicalservices.persistence.dash;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.application.dash.ISystemReset;
import com.healthyrolls.technicalservices.configuration.SeedCourierLoader;
import com.healthyrolls.technicalservices.configuration.SeedUserLoader;
import com.healthyrolls.technicalservices.configuration.SeedRestaurantLoader;

import jakarta.persistence.EntityManager;

@Component
public class JpaSystemReset implements ISystemReset {
    private final EntityManager entityManager;
    private final SeedRestaurantLoader seedRestaurantLoader;
    private final SeedUserLoader userSeedLoader;
    private final SeedCourierLoader courierSeedLoader;

    public JpaSystemReset(
            EntityManager entityManager,
            SeedRestaurantLoader seedRestaurantLoader,
            SeedUserLoader userSeedLoader,
            SeedCourierLoader courierSeedLoader) {
        this.entityManager = entityManager;
        this.seedRestaurantLoader = seedRestaurantLoader;
        this.userSeedLoader = userSeedLoader;
        this.courierSeedLoader = courierSeedLoader;
    }

    @Override
    @Transactional
    public void restoreInitialData() {
        entityManager.flush();

        entityManager.createQuery("delete from CourierEventEntity").executeUpdate();
        entityManager.createQuery("delete from OrderEventEntity").executeUpdate();
        entityManager.createQuery("delete from RestaurantEventEntity").executeUpdate();
        entityManager.createQuery("delete from MenuEventEntity").executeUpdate();

        entityManager.createQuery("delete from AssignmentEntity").executeUpdate();
        entityManager.createQuery("delete from OrderLineEntity").executeUpdate();
        entityManager.createQuery("delete from OrderEntity").executeUpdate();

        entityManager.createQuery("delete from MenuItemEntity").executeUpdate();
        entityManager.createQuery("delete from CourierEntity").executeUpdate();
        entityManager.createQuery("delete from UserEntity").executeUpdate();
        entityManager.createQuery("delete from RestaurantEntity").executeUpdate();

        entityManager.clear();

        seedRestaurantLoader.seedRestaurants();
        userSeedLoader.seedUsers();
        courierSeedLoader.seedCouriers();

        entityManager.flush();
    }

}
