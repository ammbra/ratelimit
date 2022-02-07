package org.acme.example;

import io.quarkus.redis.client.RedisClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.ws.rs.core.Response;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.util.logging.Logger;

@Path("greeting")
public class GreeterResource {

    private final static Logger LOGGER = Logger.getLogger(GreeterResource.class.getName());

    @Inject
    RedisClient redisClient;

    @ConfigProperty(name = "requests.per.minute", defaultValue = "15")
    int customLimit;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello RESTEasy";
    }


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{message}")
    public Response limited(@PathParam(value = "message") String message) {
        final LocalDateTime now = LocalDateTime.now();
        String key = message + ":" + now.getMinute();
        io.vertx.redis.client.Response requests = redisClient.get(key);
        int requestNo = (requests != null) ? requests.toInteger(): 0;

        if (requestNo >= customLimit) {
            return  Response.status(javax.ws.rs.core.Response.Status.TOO_MANY_REQUESTS)
                    .header("X-Rate-Limit-Retry-After-Seconds", 60-now.getSecond())
                    .entity(false)
                    .build();
        }

        redisClient.multi();
        redisClient.incr(key);
        redisClient.expire(key, "60");

        LOGGER.severe(String.format("Request count is %s ", redisClient.exec()));

        return Response.status(javax.ws.rs.core.Response.Status.OK)
                .header("X-Rate-Limit-Remaining", customLimit - requestNo - 1)
                .entity(true)
                .build();
    }
}