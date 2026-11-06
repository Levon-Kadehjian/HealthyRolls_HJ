package com.healthyrolls.domain.dlv.courier;

public interface ICourierEventRepository {

    void save(CourierStateChanged event);
}
