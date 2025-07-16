package openbukkitutils.eventive;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author earomc <p>
 * Created on February 16, 2022 | 23:43:35
 * A wrapper interface used to quickly register {@link Listener}s.
 * </p>
 * Code Example:
 * <pre>{@code
 *public class YourListener implements EventiveListener {
 *    @Eventhandler
 *    public void onPlayerQuit(PlayerQuitEvent event) {
 *        // Handle the event
 *    }
 *}
 * }
 * </pre>
 * <p><p>
 * Register by creating an instance and calling the register method:<p><p>
 *
 * {@code new YourListener().register(plugin)}
 *
 *
 */

public interface EventiveListener extends Listener {

    /**
     * Registers the all events marked with the {@link org.bukkit.event.EventHandler} annotation.
     *
     * @param plugin The plugin the events are registered to.
     */
    default void register(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Unregisters all events from this listener.
     */
    default void unregister() {
        HandlerList.unregisterAll(this);
    }
}
