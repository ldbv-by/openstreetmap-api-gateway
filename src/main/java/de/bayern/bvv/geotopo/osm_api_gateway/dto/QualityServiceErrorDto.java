package de.bayern.bvv.geotopo.osm_api_gateway.dto;

import org.locationtech.jts.geom.Geometry;

/**
 * Data transfer object representing a specific error of a quality service.
 */
public record QualityServiceErrorDto(
        String errorText,
        Geometry errorGeometry
) {}
