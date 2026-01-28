package de.bayern.bvv.geotopo.osm_api_gateway.dto.notification;

import lombok.Getter;

@Getter
public enum NotificationType {
    REGULAR_UPDATE("Regelaktualisierung"),
    QUALITY_CHECK("Qualitätsprüfung");

    NotificationType(String description) {
        this.description = description;
    }

    private final String description;


    /**
     * Get NotificationType from Description.
     */
    public static NotificationType fromDescription(String description) {
        for (NotificationType type : values()) {
            if (type.getDescription().equalsIgnoreCase(description)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown NotificationType description: " + description);
    }
}

