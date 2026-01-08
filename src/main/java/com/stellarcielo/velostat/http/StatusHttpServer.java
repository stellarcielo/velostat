package com.stellarcielo.velostat.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stellarcielo.velostat.StatusCollector;
import fi.iki.elonen.NanoHTTPD;

import java.util.Map;

public class StatusHttpServer extends NanoHTTPD{

    private final StatusCollector collector;
    private final Gson gson = new GsonBuilder().create();

    public StatusHttpServer(StatusCollector collector, int port) {
        super(port);
        this.collector = collector;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();

        if (uri.equals("/healthz")) {
            return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "OK");
        }

        if (uri.equals("/status")) {
            Map<String, ?> snapshot = collector.getSnapshot();
            String json = gson.toJson(snapshot);
            return newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json",
                    json
            );
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found");
    }
}
