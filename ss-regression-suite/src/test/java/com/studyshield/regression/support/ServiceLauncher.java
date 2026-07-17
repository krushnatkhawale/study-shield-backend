package com.studyshield.regression.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ServiceLauncher {

    private static final Logger log = LoggerFactory.getLogger(ServiceLauncher.class);

    private static final String PROJECT_ROOT = findProjectRoot();
    private static final String JAVA_HOME = System.getProperty("java.home");
    private static final String JAVA_BIN = JAVA_HOME + File.separator + "bin" + File.separator + "java";

    private static final long STARTUP_TIMEOUT_MS = 120_000;
    private static final long HEALTH_CHECK_INTERVAL_MS = 1_000;

    private static volatile boolean started = false;
    private final List<Process> processes = new ArrayList<>();
    private ExecutorService executor = Executors.newCachedThreadPool();

    private static String findProjectRoot() {
        String userDir = System.getProperty("user.dir");
        File dir = new File(userDir);
        while (dir != null) {
            if (new File(dir, "gradlew").exists()) {
                return dir.getAbsolutePath();
            }
            dir = dir.getParentFile();
        }
        return userDir;
    }

    public synchronized void startAllServices() {
        if (started) {
            log.info("[ServiceLauncher] Services already started, skipping");
            return;
        }

        log.info("[ServiceLauncher] Starting all services from {}", PROJECT_ROOT);
        log.info("[ServiceLauncher] JAVA_HOME={}", JAVA_HOME);

        try {
            startService("ss-content-service", 8081);
            waitForService("Content Service", 8081);

            startService("ss-user-service", 8082);
            waitForService("User Service", 8082);

            startService("ss-quiz-attempts", 8083);
            waitForService("Quiz Attempts Service", 8083);

            startService("ss-tv-device-service", 8084);
            waitForService("TV Device Service", 8084);

            startService("ss-api-gateway", 8080);
            waitForService("API Gateway", 8080);

            started = true;
            log.info("[ServiceLauncher] All services started successfully");
        } catch (Exception e) {
            log.error("[ServiceLauncher] Failed to start services", e);
            stopAllServices();
            throw new RuntimeException("Failed to start services", e);
        }
    }

    private void startService(String serviceName, int port) throws IOException {
        String jarPath = PROJECT_ROOT + "/" + serviceName + "/build/libs/" + serviceName + "-0.0.1-SNAPSHOT.jar";
        File jarFile = new File(jarPath);
        if (!jarFile.exists()) {
            throw new IOException("JAR not found: " + jarPath + ". Run './gradlew build' first.");
        }

        log.info("[ServiceLauncher] Starting {} on port {} with jar: {}", serviceName, port, jarPath);

        Map<String, String> env = new HashMap<>(System.getenv());
        env.put("SERVER_PORT", String.valueOf(port));

        String databaseUrl = System.getenv("DATABASE_URL");
        String dbUsername = System.getenv("DB_USERNAME");
        String dbPassword = System.getenv("DB_PASSWORD");
        if (databaseUrl != null) {
            env.put("DATABASE_URL", databaseUrl);
        }
        if (dbUsername != null) {
            env.put("DB_USERNAME", dbUsername);
        }
        if (dbPassword != null) {
            env.put("DB_PASSWORD", dbPassword);
        }

        List<String> command = new ArrayList<>();
        command.add(JAVA_BIN);
        command.add("-jar");
        command.add(jarPath);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(PROJECT_ROOT));
        pb.environment().putAll(env);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        processes.add(process);

        executor.submit(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[{}] {}", serviceName, line);
                }
            } catch (IOException e) {
                log.debug("[{}] Output stream closed", serviceName);
            }
        });

        log.info("[ServiceLauncher] {} started (PID: {})", serviceName, process.pid());
    }

    private void waitForService(String serviceName, int port) {
        log.info("[ServiceLauncher] Waiting for {} on port {}...", serviceName, port);
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < STARTUP_TIMEOUT_MS) {
            try {
                URL url = new URL("http://localhost:" + port + "/actuator/health");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);
                int status = conn.getResponseCode();
                if (status == 200) {
                    log.info("[ServiceLauncher] {} is ready (took {}ms)",
                            serviceName, System.currentTimeMillis() - startTime);
                    return;
                }
            } catch (Exception e) {
                // Service not ready yet
            }
            try {
                Thread.sleep(HEALTH_CHECK_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted waiting for " + serviceName, e);
            }
        }

        throw new RuntimeException(serviceName + " failed to start within " + STARTUP_TIMEOUT_MS + "ms");
    }

    public synchronized void stopAllServices() {
        log.info("[ServiceLauncher] Stopping all services...");

        for (Process process : processes) {
            if (process.isAlive()) {
                log.info("[ServiceLauncher] Stopping process PID: {}", process.pid());
                process.destroy();
                try {
                    if (!process.waitFor(10, TimeUnit.SECONDS)) {
                        process.destroyForcibly();
                    }
                } catch (InterruptedException e) {
                    process.destroyForcibly();
                    Thread.currentThread().interrupt();
                }
            }
        }

        processes.clear();
        executor.shutdownNow();
        executor = Executors.newCachedThreadPool();
        started = false;
        log.info("[ServiceLauncher] All services stopped");
    }

    public boolean isStarted() {
        return started;
    }
}
