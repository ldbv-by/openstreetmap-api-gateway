package de.bayern.bvv.geotopo.osm_api_gateway.dto.notification;

import org.locationtech.jts.geom.Geometry;

public record NewNotification(
        NotificationType type,
        String groupDescription,
        String color,
        String receiver,
        String sender,
        String subject,
        String content,
        Geometry geom
) { }
