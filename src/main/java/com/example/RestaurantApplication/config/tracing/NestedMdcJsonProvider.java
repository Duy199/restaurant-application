package com.example.RestaurantApplication.config.tracing;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.composite.AbstractJsonProvider;

public class NestedMdcJsonProvider extends AbstractJsonProvider<ILoggingEvent> {

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        var mdc = event.getMDCPropertyMap();

        // request
        String correlation_id = mdc.get("correlation_id");
        if (correlation_id != null) {
            generator.writeStringField("correlation_id", correlation_id);
        }

        // http
        String method = mdc.get("method");
        String uri = mdc.get("uri");
        if (method != null || uri != null) {
            generator.writeObjectFieldStart("http");
            if (method != null) generator.writeStringField("method", method);
            if (uri != null) generator.writeStringField("path", uri);
            generator.writeEndObject();
        }

        // client
        String clientIp = mdc.get("clientIp");
        if (clientIp != null) {
            generator.writeObjectFieldStart("client");
            generator.writeStringField("ip", clientIp);
            generator.writeEndObject();
        }

        // user
        String userId = mdc.get("userId");
        String userName = mdc.get("userName");
        String role = mdc.get("role");
        if (userId != null || userName != null || role != null) {
            generator.writeObjectFieldStart("user");
            if (userId != null) generator.writeStringField("id", userId);
            if (userName != null) generator.writeStringField("name", userName);
            if (role != null) generator.writeStringField("role", role);
            generator.writeEndObject();
        }
    }
}