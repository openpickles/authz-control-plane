package org.openpickles.policy.engine;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Component
public class OpaProcessManager implements SmartLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(OpaProcessManager.class);
    private Process opaProcess;
    private boolean running = false;
    private final int port = 8181;

    @Override
    public void start() {
        if (running) {
            return;
        }

        try {
            logger.info("Starting OPA process on port {}", port);

            // Determine OPA binary path. Detailed logic might be needed for dev vs prod.
            // Using a simple check for local dev convenience or docker location.
            String opaPath = "/app/opa";
            if (!Files.exists(Path.of(opaPath))) {
                // Fallback for local dev if 'opa' is not in /app/opa
                // Assume 'opa' is in PATH for local dev
                opaPath = "opa";
            }

            ProcessBuilder pb = new ProcessBuilder(
                    opaPath,
                    "run",
                    "--server",
                    "--addr=localhost:" + port,
                    "--log-level=info");

            // Redirect output to inherit so we see OPA logs in app logs
            pb.inheritIO();

            opaProcess = pb.start();
            running = true;
            logger.info("OPA process started with PID: {}", opaProcess.pid());

            // Give it a moment to warm up
            TimeUnit.SECONDS.sleep(1);

        } catch (IOException | InterruptedException e) {
            logger.error("Failed to start OPA process", e);
            running = false;
            // In a real app, we might want to fail startup, but for now log error
        }
    }

    @Override
    public void stop() {
        if (opaProcess != null && opaProcess.isAlive()) {
            logger.info("Stopping OPA process...");
            opaProcess.destroy();
            try {
                if (!opaProcess.waitFor(5, TimeUnit.SECONDS)) {
                    opaProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                opaProcess.destroyForcibly();
            }
            logger.info("OPA process stopped.");
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running && opaProcess != null && opaProcess.isAlive();
    }

    @Override
    public int getPhase() {
        // Typically we want it available as soon as possible, so phase 0 is fine.
        // SmartLifecycle default is 0.
        return 0;
    }
}
