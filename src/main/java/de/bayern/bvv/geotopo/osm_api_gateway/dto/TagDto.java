package de.bayern.bvv.geotopo.osm_api_gateway.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing an OSM tag.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"k", "v"})
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TagDto {

    /**
     * Tag key.
     */
    @XmlAttribute(name = "k")
    private String k;

    /**
     * Tag value.
     */
    @XmlAttribute(name = "v")
    private String v;
}
