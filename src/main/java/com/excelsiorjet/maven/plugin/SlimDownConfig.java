package com.excelsiorjet.maven.plugin;

/**
 * @author kit
 */
public class SlimDownConfig {

    /**
     * Detach the specified components from the main installation package and move them
     * to a remote package. If not specified, components detected to be unused by your
     * application are automatically detached if Java Runtime Slim-Down is enabled.
     * Available detachable components:
     *  corba, management, xml, jndi, jdbc, awt/java2d, swing, jsound, rmi, jax-ws
     *
     *  @see #detachedBaseURL
     */
    String[] detachComponents;

    /**
     * Set the base url for the detached package. It is mandatory parameter.
     */
    String detachedBaseURL;

    /**
     * Set the detached package name.
     */
    String detachedPackage;

    boolean isEnabled() {
        return ((detachComponents != null) && (detachComponents.length > 0) || (detachedBaseURL != null) ||
                (detachedPackage != null));
    }

}
