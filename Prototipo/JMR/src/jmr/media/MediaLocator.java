package jmr.media;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class describing the location of media content. 
 * 
 * It is based on the <code>MediaLocator</code> class of the Java Multimedia 
 * Framework.
 * 
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class MediaLocator implements Serializable{
    private URL url;
    private final String locatorString;

    /**
     * Constructs a media locator from an URL
     * 
     * @param url the URL to construct this media locator from.
     */
    public MediaLocator(URL url) {
        this.url = url;
        locatorString = url.toString().trim();
    }

    /**
     * Constructs a media locator from a string.
     * 
     * @param locatorString the URL to construct this media locator from.
     */
    public MediaLocator(String locatorString) {
        this.locatorString = locatorString.trim();
    }

    /**
     * Returns the URL associated with this locator.
     * 
     * @return the URL associated with this locator
     * @throws java.net.MalformedURLException
     */
    public URL getURL() throws MalformedURLException {
        if (url == null) {
            url = new URL(locatorString);
        }
        return url;
    }

    /**
     * Returns the beginning of the locator string up to but not including the first
     * colon.
     *
     * @return the protocol for this locator
     */
    public String getProtocol() {
        String protocol = "";
        int colonIndex = locatorString.indexOf(':');

        if (colonIndex != -1) {
            protocol = locatorString.substring(0, colonIndex);
        }
        return protocol;
    }

    /**
     * Returns the locator string with the protocol removed.
     *
     * @return the locator string with the protocol removed
     */
    public String getRemainder() {
        String remainder = "";
        int colonIndex = locatorString.indexOf(":");

        if (colonIndex != -1) {
            remainder = locatorString.substring(colonIndex + 1);
        }
        return remainder;
    }

    /**
     * Returns a string representation of this database
     * .
     * @return a string representation of this database 
     */
    @Override
    public String toString() {
        return locatorString;
    }
}
