package com.stellarcielo.velostat;

import com.google.inject.Inject;
import com.stellarcielo.velostat.http.StatusHttpServer;
import com.stellarcielo.velostat.config.ConfigManager;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import fi.iki.elonen.NanoHTTPD;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "velostat",
        name = "velostat",
        version = BuildConstants.VERSION
)

public class Velostat {

    private final Logger logger;
    private final ProxyServer proxy;
    private final Metrics.Factory metricsFactory;
    private final Path dataDir;

    private StatusCollector collector;
    private StatusHttpServer server;

    @Inject
    public Velostat(Logger logger, ProxyServer proxy, Metrics.Factory metricsFactory, @DataDirectory Path dataDir) {
        this.logger = logger;
        this.proxy = proxy;
        this.metricsFactory = metricsFactory;
        this.dataDir = dataDir;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Starting Velostat...");

        ConfigManager config;
        try {
            config = new ConfigManager(dataDir, logger);
            config.load();
        } catch (Exception e) {
            logger.error("Failed to load config", e);
            return;
        }

        int pluginId = 28763;
        Metrics metrics = metricsFactory.make(this, pluginId);

        collector = new StatusCollector(proxy, logger, config.pingIntervalSeconds());
        collector.start();

        try {
            server = new StatusHttpServer(collector, config.apiPort());
            server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (Exception e) {
            logger.error("Failed to start HTTP server", e);
        }
    }
}
