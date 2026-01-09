package com.stellarcielo.velostat.config;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ConfigManager {

    public static final int CONFIG_VERSION = 1;

    private final Path dataDir;
    private final Logger logger;

    private Map<String, Object> config;

    public ConfigManager(Path dataDir, Logger logger) {
        this.dataDir = dataDir;
        this.logger = logger;
    }

    public void load() throws IOException {
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
        }

        Path configPath = dataDir.resolve("config.yml");

        if (!Files.exists(configPath)) {
            copyDefaultConfig(configPath);
            logger.info("Created default config file");
        }

        try (InputStream in = Files.newInputStream(configPath)) {
            config = new Yaml().load(in);
        }

        migrateIfNeeded(configPath);
    }


    private void copyDefaultConfig(Path configPath) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {

            if (in == null) {
                throw new FileNotFoundException("Default config.yml not found in resources");
            }

            Files.copy(in, configPath);
        }
    }

    private void migrateIfNeeded(Path configPath) throws IOException{
        int version = (int) config.getOrDefault("version", 0);

        if (version == CONFIG_VERSION) return;

        logger.info("Migrating config from version {} to {}", version, CONFIG_VERSION);

        if (version < 1) {
            migrateToV1();
        }

        try (Writer writer = Files.newBufferedWriter(configPath)) {
            new Yaml().dump(config, writer);
        }

        logger.info("Config migration complete.");
    }

    private void migrateToV1() {
        config.putIfAbsent("api", Map.of(
                "port", 8080
        ));
        config.putIfAbsent("ping", Map.of(
                "interval", 10
        ));
    }

    /* ===== getters ===== */

    public int apiPort() {
        return (int) ((Map<String, Object>) config.get("api")).get("port");
    }

    public int pingIntervalSeconds() {
        return (int) ((Map<String, Object>) config.get("ping")).get("interval_seconds");
    }
}
