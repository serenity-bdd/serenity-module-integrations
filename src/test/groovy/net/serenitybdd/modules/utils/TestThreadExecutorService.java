package net.serenitybdd.modules.utils;

import net.thucydides.core.ThucydidesSystemProperty;
import net.thucydides.core.reports.ExecutorServiceProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: YamStranger
 * Date: 2/10/16
 * Time: 1:53 PM
 */
public class TestThreadExecutorService implements ExecutorServiceProvider {
    final int corePoolSize;
    final int maximumPoolSize;
    private ExecutorService executorService;

    public TestThreadExecutorService() {
        this.corePoolSize = Runtime.getRuntime().availableProcessors();
        this.maximumPoolSize = Runtime.getRuntime().availableProcessors();
    }

    public ExecutorService getExecutorService() {
        if (this.executorService == null) {
            this.executorService = Executors.newFixedThreadPool(this.maximumPoolSize);
        }

        return this.executorService;
    }
}
