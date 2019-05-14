package com.enode.queue.command;

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
