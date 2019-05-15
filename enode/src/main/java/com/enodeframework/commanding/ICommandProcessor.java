package com.enodeframework.commanding;

public interface ICommandProcessor {
    void process(ProcessingCommand processingCommand);

    void start();

    void stop();
}
