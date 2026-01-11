package com.stellarcielo.velostat.export;

import com.stellarcielo.velostat.config.ConfigManager;
import com.stellarcielo.velostat.model.ServerStatus;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServerStatusExporter {

    private final ConfigManager config;

    public ServerStatusExporter(ConfigManager config) {
        this.config = config;
    }

    public Map<String, Object> export(ServerStatus status) {
        Map<String, Object> out = new LinkedHashMap<>();

        if (config.exportServerName())
            out.put("name", status.name);

        if (config.exportServerAddress())
            out.put("address", status.address);

        if (config.exportServerStatus())
            out.put("online", status.online);

        if (config.exportServerMotd())
            out.put("motd", status.motd);

        if (config.exportServerPlayers()) {
            out.put("players", Map.of(
                    "online", status.playersOnline,
                    "max", status.playersMax
            ));
        }

        if (config.exportServerVersion()) {
            out.put("version", Map.of(
                    "name", status.versionName,
                    "protocol", status.protocol
            ));
        }

        if (config.exportServerUpdatedAt())
            out.put("updated_at", status.updateAt);

        return out;
    }
}