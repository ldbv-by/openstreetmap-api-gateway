package de.bayern.bvv.geotopo.osm_api_gateway.config;


import org.locationtech.jts.geom.Geometry;
import tools.jackson.core.Version;
import tools.jackson.databind.module.SimpleModule;

public class JtsJackson3Module extends SimpleModule {

    public JtsJackson3Module() {
        super("JtsJackson3Module", Version.unknownVersion());
        addSerializer(Geometry.class, new GeometryGeoJsonSerializer());
        addDeserializer(Geometry.class, new GeometryGeoJsonDeserializer());
    }
}
