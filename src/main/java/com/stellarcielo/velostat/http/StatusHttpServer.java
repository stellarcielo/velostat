package com.stellarcielo.velostat.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stellarcielo.velostat.StatusCollector;
import com.stellarcielo.velostat.config.ConfigManager;
import com.stellarcielo.velostat.export.PrometheusExporter;
import com.stellarcielo.velostat.export.ServerStatusExporter;
import com.velocitypowered.api.proxy.ProxyServer;
import fi.iki.elonen.NanoHTTPD;

import java.util.LinkedHashMap;
import java.util.Map;

public class StatusHttpServer extends NanoHTTPD{

    private final ProxyServer proxy;
    private final StatusCollector collector;
    private final Gson gson = new GsonBuilder().create();
    private final ServerStatusExporter exporter;
    private final PrometheusExporter prometheusExporter = new PrometheusExporter();
    ConfigManager config;

    public StatusHttpServer(ProxyServer proxy, StatusCollector collector, int port, ConfigManager config) {
        super(port);
        this.proxy = proxy;
        this.collector = collector;
        this.config = config;
        this.exporter = new ServerStatusExporter(config);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();

        if (uri.equals("/healthz")) {
            return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "OK");
        }

        if (uri.equals("/status")) {
            Map<String, Object> result = new LinkedHashMap<>();

            collector.getSnapshot().forEach((name, status) -> result.put(name, exporter.export(status)));

            return newFixedLengthResponse(Response.Status.OK, "application/json", gson.toJson(result));
        }

        if (config.prometheusEnabled() && uri.equals(config.prometheusPath())) {
            String metrics = prometheusExporter.export(collector.getSnapshot(), proxy.getPlayerCount());

            return newFixedLengthResponse(Response.Status.OK, PrometheusExporter.CONTENT_TYPE, metrics);
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found");
    }
}
