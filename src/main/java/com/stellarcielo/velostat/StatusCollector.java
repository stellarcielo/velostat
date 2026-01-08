package com.stellarcielo.velostat;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.stellarcielo.velostat.model.ServerStatus;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.*;

public class StatusCollector {

    private final ProxyServer proxy;
    private final Logger logger;
    private final Map<String, ServerStatus> cache = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;

    public StatusCollector(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;
    }

    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(
                this::updateAll,
                0,
                10,
                TimeUnit.SECONDS
        );
    }

    private void updateAll() {
        for (RegisteredServer server : proxy.getAllServers()) {
            updateServer(server);
        }
    }

    private void updateServer(RegisteredServer server) {
        server.ping().thenAccept(ping -> {
            ServerStatus status = new ServerStatus();
            status.name = server.getServerInfo().getName();
            status.address = server.getServerInfo().getAddress().toString();
            status.online = true;

            status.motd = PlainTextComponentSerializer.plainText().serialize(ping.getDescriptionComponent());

            ping.getPlayers().ifPresent(players -> {
                status.playersOnline = players.getOnline();
                status.playersMax = players.getMax();
            });

            var version = ping.getVersion();
            status.versionName = version.getName();
            status.protocol = version.getProtocol();

            status.updateAt = System.currentTimeMillis();
            cache.put(status.name, status);
        }).exceptionally(ex -> {
            ServerStatus status = new ServerStatus();
            status.name = server.getServerInfo().getName();
            status.address = server.getServerInfo().getAddress().toString();
            status.online = false;
            status.updateAt = System.currentTimeMillis();
            cache.put(status.name, status);
            return null;
        });
    }

    public Map<String, ServerStatus> getSnapshot() {
        return cache;
    }
}
