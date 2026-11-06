package com.healthyrolls.domain.dlv.delivery;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.healthyrolls.domain.dlv.courier.Courier;
import com.healthyrolls.domain.ord.restaurant.Restaurant;

public class NearestCourierSelector {

    public Optional<Courier> select(
            Restaurant restaurant,
            List<Courier> candidates) {

        Objects.requireNonNull(restaurant, "Restaurant is required");
        Objects.requireNonNull(candidates, "Courier candidates are required");

        return candidates.stream()
                .filter(Courier::isAvailable)
                .min(Comparator
                        .comparingDouble((Courier courier) -> courier.distanceTo(
                                restaurant.getLatitude(),
                                restaurant.getLongitude()))
                        .thenComparing(courier -> courier.getId().value().toString()));
    }
}