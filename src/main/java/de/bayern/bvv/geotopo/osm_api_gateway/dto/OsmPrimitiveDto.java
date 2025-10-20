package de.bayern.bvv.geotopo.osm_api_gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object representing an OSM primitive.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OsmPrimitiveDto {

    /**
     * Unique OSM identifier.
     */
    private Long id;

    /**
     * Version of the node.
     */
    private Long version;

    /**
     * Changeset identifier in which this osm primitive was modified.
     */
    private Long changesetId;

    /**
     * List of tags as key-value pairs.
     */
    private List<TagDto> tags;
}
