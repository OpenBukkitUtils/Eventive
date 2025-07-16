package openbukkitutils.eventive;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

/**
 * Generic wrapper class for Bukkit's {@link EventExecutor}.
 * <p>
 * Helps to register EventExecutors working with generics.
 * This class being generic removes unnecessary instanceof queries and typecasts when writing own EventExecutor implementations.
 * Especially useful when expressing EventExecutors as lambda expressions.
 * </p>
 * As an example with the {@link EventsUtil} class:
 * <p>
 * <p>Instead of</p>
 * <pre>{@code
 * eventsUtil.registerEventExecutor(BlockPlaceEvent.class, (listener, event) -> {
 *  if (event instanceof BlockPlaceEvent) {
 *   BlockPlaceEvent event1 = (BlockPlaceEvent) event;
 *   Bukkit.broadcastMessage("Placed block at " + event1.getBlock().getLocation());
 *  }
 * });
 * }</pre>
 * <p>You can do:</p>
 * <pre>{@code
 * eventsUtil.registerEventExecutor(BlockPlaceEvent.class, (listener, event) -> {
 *  Bukkit.broadcastMessage("Placed block at " + event.getBlock().getLocation());
 * });
 * }</pre>
 * @param <T> The Event this Executor executes :P
 */

@FunctionalInterface
public interface GenericEventExecutor<T extends Event> {

    void execute(T event) throws EventException;

    default EventExecutor unwrap(Class<T> eventClass) {
        return (listener, event) -> {
            if (eventClass.isInstance(event)) { // basically: if (event instanceof T)
                //noinspection unchecked
                execute((T) event);
            }
        };
    }
}
