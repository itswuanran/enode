package com.enodeframework.samples.eventhandlers;

import com.enodeframework.common.logging.ENodeLogger;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class TestService {

    public static Logger logger = ENodeLogger.getLog();

    public void sayHello() {
        logger.info("hello");
    }

}
