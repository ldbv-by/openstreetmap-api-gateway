package de.bayern.bvv.geotopo.osm_api_gateway.config;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class GeometryGeoJsonSerializer extends ValueSerializer<Geometry> {

    private static final GeoJsonWriter WRITER = new GeoJsonWriter();

    @Override
    public void serialize(Geometry value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        String geoJson = WRITER.write(value);
        gen.writeRawValue(geoJson);
    }
}