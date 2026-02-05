package de.bayern.bvv.geotopo.osm_api_gateway.dto;

import org.locationtech.jts.geom.Geometry;

public record NewNotification(
        String typeId,
        String groupDescription,
        String color,
        String receiver,
        String sender,
        String subject,
        String content,
        Geometry geom
) { }
