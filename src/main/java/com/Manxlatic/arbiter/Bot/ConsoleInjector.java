package com.Manxlatic.arbiter.Bot;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.time.Instant;
import java.util.concurrent.*;

public class ConsoleInjector extends AbstractAppender {

    private final JDADiscordService jdaDiscordService;
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private ScheduledFuture<?> taskFuture;
    private boolean removed = false;
    private static final int QUEUE_PROCESS_PERIOD_SECONDS = 2;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public ConsoleInjector(JDADiscordService jdaDiscordService) {
        super("ConsoleInjector", null, null, false);
        this.jdaDiscordService = jdaDiscordService;
        start(); // Explicitly start the appender

        taskFuture = scheduler.scheduleAtFixedRate(() -> {
            final StringBuilder buffer = new StringBuilder();
            String curLine;
            while ((curLine = messageQueue.poll()) != null) {
                if (buffer.length() + curLine.length() > 2000) { // Adjust max message length as needed
                    sendMessage(buffer.toString());
                    buffer.setLength(0);
                }
                buffer.append("\n").append(curLine);
            }
            if (buffer.length() != 0) {
                sendMessage(buffer.toString());
            }
        }, 0, QUEUE_PROCESS_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    private void sendMessage(String content) {
        if (content == null || content.isEmpty()) {
            System.err.println("Attempted to send an empty message.");
            return;
        }
        jdaDiscordService.sendMessage(content);
    }

    @Override
    public void append(LogEvent event) {
        if (!isStarted()) {
            return; // Ensure the appender is started before appending
        }

        // Check log level and only process messages of level INFO and above
        if (event.getLevel().isMoreSpecificThan(Level.INFO)) {
            String entry = event.getMessage().getFormattedMessage().trim();
            if (entry.isEmpty()) {
                return;
            }

            // Format the log message
            String formattedMessage = String.format("[%s] %s: %s",
                    Instant.now().toString(), event.getLevel().name(), entry);
            messageQueue.add(formattedMessage);
        }
    }

    @Override
    public void start() {
        if (!isStarted()) {
            super.start();
        }
    }

    @Override
    public void stop() {
        if (isStarted()) {
            super.stop();
        }
    }

}





