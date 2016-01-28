package com.excelsiorjet.maven.plugin;

/**
 * @author kit
 */
public class SlimDownConfig {

    /**
     * <p>
     *   Detach the specified components from the main installation package and place them
     *   in a remote package. If unspecified, the components detected to be unused by your
     *   application are automatically detached if Java Runtime Slim-Down is enabled.
     * </p>
     * <p>
     *   Available detachable components:
     *     {@code corba}, {@code management}, {@code xml}, {@code jndi}, {@code jdbc},
     *     {@code awt/java2d}, {@code swing}, {@code jsound}, {@code rmi}, {@code jax-ws}
     * </p>
     *
     * @see #detachedBaseURL
     * @see JetMojo#javaRuntimeSlimDown
     */
    public String[] detachComponents;

    /**
     * Set the base url for the detached package. This parameter is mandatory.
     * 
     * @see JetMojo#javaRuntimeSlimDown
     */
    public String detachedBaseURL;

    /**
     * Set the detached package name.
     * 
     * @see JetMojo#javaRuntimeSlimDown
     */
    public String detachedPackage;

    boolean isEnabled() {
        return ((detachComponents != null) && (detachComponents.length > 0) || (detachedBaseURL != null) ||
                (detachedPackage != null));
    }

}
