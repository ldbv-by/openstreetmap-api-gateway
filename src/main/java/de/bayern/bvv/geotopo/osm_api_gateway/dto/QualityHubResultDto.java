package de.bayern.bvv.geotopo.osm_api_gateway.dto;

import java.util.List;

/**
 * Data transfer object representing the result of a quality hub check.
 */
public record QualityHubResultDto(
        String changesetXml,
        boolean isValid,
        List<QualityServiceResultDto> qualityServiceResults
) {}
