package de.bayern.bvv.geotopo.osm_api_gateway.config;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;


public class GeometryGeoJsonDeserializer extends ValueDeserializer<Geometry> {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final GeoJsonReader READER = new GeoJsonReader(GEOMETRY_FACTORY);

    @Override
    public Geometry deserialize(JsonParser p, DeserializationContext ctxt) {
        try {
            String json = p.readValueAsTree().toString();
            return READER.read(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse GeoJSON: " + e.getMessage(), e);
        }
    }
}