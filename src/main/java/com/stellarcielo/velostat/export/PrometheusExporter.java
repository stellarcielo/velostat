package com.stellarcielo.velostat.export;

import com.stellarcielo.velostat.model.ServerStatus;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

/**
 * Renders server statuses in the Prometheus text exposition format (version 0.0.4).
 */
public class PrometheusExporter {

    public static final String CONTENT_TYPE = "text/plain; version=0.0.4; charset=utf-8";

    public String export(Map<String, ServerStatus> snapshot, int proxyPlayersOnline) {
        // サーバー名順に並べて出力を安定させる
        Map<String, ServerStatus> servers = new TreeMap<>(snapshot);
        StringBuilder out = new StringBuilder();

        header(out, "velostat_proxy_players_online", "Number of players connected to the proxy.");
        out.append("velostat_proxy_players_online ").append(proxyPlayersOnline).append('\n');

        header(out, "velostat_server_up", "Whether the backend server responded to the last ping (1 = online, 0 = offline).");
        for (ServerStatus status : servers.values()) {
            sample(out, "velostat_server_up", status, status.online ? "1" : "0");
        }

        header(out, "velostat_server_players_online", "Number of players online reported by the backend server.");
        for (ServerStatus status : servers.values()) {
            if (status.online)
                sample(out, "velostat_server_players_online", status, String.valueOf(status.playersOnline));
        }

        header(out, "velostat_server_players_max", "Maximum number of players reported by the backend server.");
        for (ServerStatus status : servers.values()) {
            if (status.online)
                sample(out, "velostat_server_players_max", status, String.valueOf(status.playersMax));
        }

        header(out, "velostat_server_ping_duration_seconds", "Time taken by the last successful ping to the backend server.");
        for (ServerStatus status : servers.values()) {
            if (status.online)
                sample(out, "velostat_server_ping_duration_seconds", status, BigDecimal.valueOf(status.pingNanos, 9).toPlainString());
        }

        header(out, "velostat_server_info", "Version information reported by the backend server.");
        for (ServerStatus status : servers.values()) {
            if (!status.online) continue;
            out.append("velostat_server_info{server=\"").append(escape(status.name))
                    .append("\",version=\"").append(escape(status.versionName == null ? "" : status.versionName))
                    .append("\",protocol=\"").append(status.protocol)
                    .append("\"} 1\n");
        }

        header(out, "velostat_server_last_update_timestamp_seconds", "Unix time of the last status update for the backend server.");
        for (ServerStatus status : servers.values()) {
            sample(out, "velostat_server_last_update_timestamp_seconds", status, BigDecimal.valueOf(status.updateAt, 3).toPlainString());
        }

        return out.toString();
    }

    private static void header(StringBuilder out, String name, String help) {
        out.append("# HELP ").append(name).append(' ').append(help).append('\n');
        out.append("# TYPE ").append(name).append(" gauge\n");
    }

    private static void sample(StringBuilder out, String name, ServerStatus status, String value) {
        out.append(name).append("{server=\"").append(escape(status.name)).append("\"} ").append(value).append('\n');
    }

    // ラベル値ではバックスラッシュ・ダブルクォート・改行をエスケープする必要がある
    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
