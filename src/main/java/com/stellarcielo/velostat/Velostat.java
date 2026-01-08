package com.stellarcielo.velostat;

import com.google.inject.Inject;
import com.stellarcielo.velostat.http.StatusHttpServer;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

@Plugin(
        id = "velostat",
        name = "velostat",
        version = BuildConstants.VERSION
)

public class Velostat {

    private final Logger logger;
    private final ProxyServer proxy;

    private StatusCollector collector;
    private StatusHttpServer server;
    Metrics.Factory metricsFactory;

    @Inject
    public Velostat(Logger logger, ProxyServer proxy, Metrics.Factory metricsFactory) {
        this.logger = logger;
        this.proxy = proxy;
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Starting Velostat...");

        int pluginId = 28763;
        Metrics metrics = metricsFactory.make(this, pluginId);

        collector = new StatusCollector(proxy, logger);
        collector.start();

        try {
            server = new StatusHttpServer(collector, 8080);
            server.start();
        } catch (Exception e) {
            logger.error("Failed to start HTTP server", e);
        }
    }
}
