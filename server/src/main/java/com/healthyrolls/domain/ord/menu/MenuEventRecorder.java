package com.healthyrolls.domain.ord.menu;

public interface MenuEventRecorder {

    void record(MenuItemStateChanged event);
}
