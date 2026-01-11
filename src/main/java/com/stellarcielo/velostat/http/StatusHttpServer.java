package com.stellarcielo.velostat.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stellarcielo.velostat.StatusCollector;
import com.stellarcielo.velostat.config.ConfigManager;
import com.stellarcielo.velostat.export.ServerStatusExporter;
import fi.iki.elonen.NanoHTTPD;

import java.util.LinkedHashMap;
import java.util.Map;

public class StatusHttpServer extends NanoHTTPD{

    private final StatusCollector collector;
    private final Gson gson = new GsonBuilder().create();
    ConfigManager config;

    public StatusHttpServer(StatusCollector collector, int port, ConfigManager config) {
        super(port);
        this.collector = collector;
        this.config = config;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();

        if (uri.equals("/healthz")) {
            return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "OK");
        }

        if (uri.equals("/status")) {
            Map<String, Object> result = new LinkedHashMap<>();

            ServerStatusExporter exporter = new ServerStatusExporter(config);

            collector.getSnapshot().forEach((name, status) -> result.put(name, exporter.export(status)));

            return newFixedLengthResponse(Response.Status.OK, "application/json", gson.toJson(result));
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found");
    }
}
