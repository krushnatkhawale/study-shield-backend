package com.studyshield.regression.hooks;

import com.studyshield.regression.support.ServiceLauncher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuiteShutdownListener implements TestExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(SuiteShutdownListener.class);
    private static final ServiceLauncher serviceLauncher = new ServiceLauncher();

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        log.info("[SuiteShutdown] Test plan started, registering shutdown hook");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("[SuiteShutdown] JVM shutting down, stopping services...");
            serviceLauncher.stopAllServices();
        }));
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        log.info("[SuiteShutdown] Test plan finished");
    }
}
