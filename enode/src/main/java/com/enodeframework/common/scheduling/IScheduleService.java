package com.enodeframework.common.scheduling;

import com.enodeframework.common.function.Action;

public interface IScheduleService {
    void startTask(String name, Action action, int dueTime, int period);

    void stopTask(String name);
}
