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
            Map<String, Object> players = new LinkedHashMap<>();
            players.put("online", status.playersOnline);
            players.put("max", status.playersMax);
            out.put("players", players);
        }
        if (config.exportServerVersion()) {
            Map<String, Object> version = new LinkedHashMap<>();
            version.put("name", status.versionName); // status.versionName が null でも HashMap なら許容される
            version.put("protocol", status.protocol);
            out.put("version", version);
        }
        if (config.exportServerUpdatedAt())
            out.put("updated_at", status.updateAt);

        return out;
    }
}