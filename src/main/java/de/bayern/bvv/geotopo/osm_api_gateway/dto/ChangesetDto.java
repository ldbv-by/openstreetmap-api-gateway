package de.bayern.bvv.geotopo.osm_api_gateway.dto;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object of an OSM changeset.
 */
@XmlRootElement(name = "osmChange")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"version", "generator", "createPrimitives", "modifyPrimitives", "deletePrimitives"})
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChangesetDto {
    /**
     * Changeset version.
     */
    @XmlAttribute(name = "version")
    private String version;

    /**
     * Changeset generator e.g. JOSM.
     */
    @XmlAttribute(name = "generator")
    private String generator;

    /**
     * OSM primitives created in this changeset.
     */
    @XmlElementWrapper(name = "create")
    @XmlElements({
            @XmlElement(name = "node", type = NodeDto.class),
            @XmlElement(name = "way",  type = WayDto.class),
            @XmlElement(name = "relation", type = RelationDto.class)
    })
    private List<OsmPrimitiveDto> createPrimitives = new ArrayList<>();

    /**
     * OSM primitives modified in this changeset.
     */
    @XmlElementWrapper(name = "modify")
    @XmlElements({
            @XmlElement(name = "node", type = NodeDto.class),
            @XmlElement(name = "way",  type = WayDto.class),
            @XmlElement(name = "relation", type = RelationDto.class)
    })
    private List<OsmPrimitiveDto> modifyPrimitives = new ArrayList<>();

    /**
     * OSM primitives deleted in this changeset.
     */
    @XmlElementWrapper(name = "delete")
    @XmlElements({
            @XmlElement(name = "node", type = NodeDto.class),
            @XmlElement(name = "way",  type = WayDto.class),
            @XmlElement(name = "relation", type = RelationDto.class)
    })
    private List<OsmPrimitiveDto> deletePrimitives = new ArrayList<>();
}
