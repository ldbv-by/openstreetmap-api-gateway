package de.bayern.bvv.geotopo.osm_api_gateway.dto;


import java.util.List;

/**
 * Data transfer object representing the result of a quality service check.
 */
public record QualityServiceResultDto(
        String qualityServiceId,
        Long changesetId,
        ChangesetDto modifiedChangesetDto,
        boolean isValid,
        List<QualityServiceErrorDto> errors
) {}
