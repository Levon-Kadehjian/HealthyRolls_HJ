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

        // deterministic comparing straightline distance (favours lower Courier ID)
        return candidates.stream()
                .filter(Courier::isAvailable)
                // floating point comparison (rare problem but just in case)
                .min(Comparator
                        .comparingLong((Courier courier) -> Math.round(courier.distanceTo(
                                restaurant.getLatitude(),
                                restaurant.getLongitude()) * 1e9))
                        .thenComparing(courier -> courier.getId().value().toString()));
    }
}
