package carcassonne.settings;

/**
 * Encapsules system-specific properties like OS name and screen resolution.
 * TODO Summerdave: No longer required, once panels clip to window size.
 * 
 * @deprecated
 * @author Timur Saglam
 */
@Deprecated
public class SystemProperties {
    private final int resolutionHeight;
    private final int resolutionWidth;

    /**
     * Creates system properties.
     */
    public SystemProperties() {
        resolutionWidth = 540;
        resolutionHeight = 540;
    }

    /**
     * Returns the height value of the screen resolution.
     * 
     * @return the resolution height in pixel.
     */
    public int getResolutionHeight() {
        return resolutionHeight;
    }

    /**
     * Returns the width value of the screen resolution.
     * 
     * @return the resolution width in pixel.
     */
    public int getResolutionWidth() {
        return resolutionWidth;
    }
}
