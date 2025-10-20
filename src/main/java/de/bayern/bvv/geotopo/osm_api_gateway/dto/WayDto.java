package de.bayern.bvv.geotopo.osm_api_gateway.dto;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object representing an OSM Way primitive.
 */
@EqualsAndHashCode(callSuper = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"id", "version", "changesetId", "nodeRefs", "tags"})
@AllArgsConstructor
@NoArgsConstructor
@Data
public class WayDto extends OsmPrimitiveDto{

    /**
     * Unique OSM identifier.
     */
    @XmlAttribute(name = "id")
    @Override
    public Long getId() { return super.getId(); }

    @Override
    public void setId(Long id) {
        super.setId(id);
    }

    /**
     * Version of the node.
     */
    @XmlAttribute(name = "version")
    @Override
    public Long getVersion() { return super.getVersion(); }

    @Override
    public void setVersion(Long version) {
        super.setVersion(version);
    }

    /**
     * Changeset identifier in which this way was modified.
     */
    @XmlAttribute(name = "changeset")
    @Override
    public Long getChangesetId() { return super.getChangesetId(); }

    @Override
    public void setChangesetId(Long changesetId) {
        super.setChangesetId(changesetId);
    }

    /**
     * List of Way Node Ids.
     */
    @XmlElement(name = "nd")
    private List<Nd> nodeRefs;

    /**
     * List of tags as key-value pairs.
     */
    @XmlElement(name = "tag")
    @Override
    public List<TagDto> getTags() { return super.getTags(); }

    @Override
    public void setTags(List<TagDto> tags) {
        super.setTags(tags);
    }

    /**
     * A specific Way Node.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Nd {

        /**
         * Way Node identifier.
         */
        @XmlAttribute(name = "ref")
        private Long ref;
    }

}
