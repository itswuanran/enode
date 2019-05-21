package com.enodeframework.kafka;

public enum ConsumeStatus {
    /**
     * Success consumption
     */
    CONSUMESUCCESS,
    /**
     * Failure consumption,later try to consume
     */
    RECONSUMELATER;
}
