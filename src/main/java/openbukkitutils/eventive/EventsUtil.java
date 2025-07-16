package openbukkitutils.eventive;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * This class allows you to easily register individual event handlers / event executors
 * with single method calls.
 */
/*TODO:
   Think about adding a way to add executors without listener component to make it possible
   to register classic event handler methods use method reference.
   Like an EventConsumer :)
 */
public class EventsUtil {

    @NotNull
    protected final Plugin plugin;

    @NotNull
    protected final Listener listener;

    /**
     * Creates a new {@link EventsUtil} that is able to register event handlers for the given plugin.
     * @param plugin The plugin to register the events to.
     * @param listener An optional listener instance to register the events to. If none is provided, an empty anonymous listener is created.
     */
    public EventsUtil(@NotNull Plugin plugin, @Nullable Listener listener) {
        this.plugin = plugin;
        if (listener == null) {
            this.listener = new Listener() {};
            /* Empty listener to register stuff to.
            I mean, Listener is just a marker interface anyway.*/
        } else this.listener = listener;
    }

    public EventsUtil(Plugin plugin) {
        this(plugin, new Listener() {});
    }

    /**
     * Helper function to register a single event executor
     * @param eventClass The class/type of the event that is handled by the event executor
     * @param listener The listener that the executor is registered to
     * @param priority The event priority See: {@link EventPriority}
     * @param executor The actual event executor that is executed when the event occurs
     * @param plugin The plugin that the executor is registered to
     * @param ignoreCancelled Whether to pass cancelled events or not
     * @param <T> The generic event type of the handled event
     */
    public static <T extends Event> void registerGenericEventExecutor(Class<T> eventClass, Listener listener, EventPriority priority, GenericEventExecutor<T> executor, Plugin plugin, boolean ignoreCancelled) {
        Bukkit.getPluginManager().registerEvent(eventClass,
                listener,
                priority,
                executor.unwrap(eventClass),
                plugin,
                ignoreCancelled);
    }

    /**
     * Helper function to register a single event executor
     * @param eventClass The class/type of the event that is handled by the event executor
     * @param listener The listener that the executor is registered to
     * @param priority The event priority: {@link EventPriority}
     * @param executor The actual event executor that is executed when the event occurs
     * @param plugin The plugin that the executor is registered to
     * @param <T> The generic event type of the handled event
     */
    public static <T extends Event> void registerGenericEventExecutor(Class<T> eventClass, Listener listener, EventPriority priority, GenericEventExecutor<T> executor, Plugin plugin) {
        Bukkit.getPluginManager().registerEvent(eventClass,
                listener,
                priority,
                executor.unwrap(eventClass),
                plugin);
    }


    // ----------- canceling events -------------

    // ----------- Bulk canceling -------------

    /**
     * Registers EventExecutors with the given {@link EventPriority} that automatically cancel the given events
     * Use carefully: Cancels the events <strong>globally</strong>
     *
     * @param priority     The event priority: {@link EventPriority}
     * @param cancellableClasses The event classes to register as varargs. Example: {@literal BlockPlaceEvent.class, BlockBreakEvent.class}
     */
    @SafeVarargs
    public final void cancelEvents(EventPriority priority, Class<? extends Cancellable>... cancellableClasses) {
        for (Class<? extends Cancellable> cancellableClass : cancellableClasses) {
            cancelEvent(cancellableClass, priority);
        }
    }

    /**
     * Registers EventExecutors that automatically cancel the given events
     * Use carefully: Cancels the events <strong>globally</strong>
     *
     * @param cancellableClass The event classes to register as varargs. Example: {@literal BlockPlaceEvent.class, BlockBreakEvent.class}
     */
    @SafeVarargs
    public final  void cancelEvents(Class<? extends Cancellable> ... cancellableClass) {
        for (Class<? extends Cancellable> eventClass : cancellableClass) {
            cancelEvent(eventClass);
        }
    }

    // ----------- Single cancelling -------------

    /**
     * Registers an EventExecutor that automatically cancels the given event.
     * Use carefully: Cancels an event <strong>globally</strong>!
     *
     * @param cancellableClass A cancellable event class to register. Example: {@literal PlayerDropItemEvent.class}
     */
    public void cancelEvent(Class<? extends Cancellable> cancellableClass) {
        cancelEvent(cancellableClass, EventPriority.NORMAL);
    }

    // ----------- with priority  -------------
    // cancellable base method

    /**
     * Registers an EventExecutor with the given {@link EventPriority} that automatically cancels the given event.
     *
     * @param cancellableClass A cancellable event class to register. Example: {@literal PlayerDropItemEvent.class}
     * @param priority   The event priority: {@link EventPriority}.
     */
    public void cancelEvent(Class<? extends Cancellable> cancellableClass, EventPriority priority) {
        Class<? extends Event> eventClass = ensureCancellableIsEventClass(cancellableClass);
        registerEventExecutor(eventClass, priority, getEventCanceller());
    }


    // -------- Event cancelling with BooleanSupplier conditions ---------

    /**
     * Registers an EventExecutor that automatically cancels the event under the given condition.
     *
     * @param eventClass A cancellable event class to register. Example: {@literal PlayerDropItemEvent.class}
     * @param <T>        A cancellable event type.
     * @param condition  The condition under which the event is cancelled.
     */
    public <T extends Event & Cancellable> void cancelEvent(Class<T> eventClass, BooleanSupplier condition) {
        cancelEvent(eventClass, condition, EventPriority.NORMAL);
    }

    /**
     * Registers an EventExecutor that automatically cancels the event under the given condition.
     *
     * @param eventClass A cancellable event class to register. Example: {@literal PlayerDropItemEvent.class}
     * @param <T>        A cancellable event type.
     * @param condition  The condition under which the event is cancelled.
     * @param priority   The event priority: {@link EventPriority}.
     */
    public <T extends Event & Cancellable> void cancelEvent(Class<T> eventClass, BooleanSupplier condition, EventPriority priority) {
        registerEventExecutor(eventClass, priority, getEventCanceller(condition));
    }

    // -------- Event cancelling with Predicate conditions --------

    /**
     * Registers an EventExecutor that automatically cancels the event under the given condition.
     *
     * @param eventClass A cancellable event class to register. Example: {@literal PlayerDropItemEvent.class}
     * @param <T>        A cancellable event type.
     * @param condition  The condition under which the event is cancelled.
     */
    public <T extends Event & Cancellable> void cancelEvent(Class<T> eventClass, Predicate<T> condition) {
        cancelEvent(eventClass, condition, EventPriority.NORMAL);
    }

    /**
     * Registers an EventExecutor that automatically cancels the event under the given condition.
     *
     * @param eventClass The event class to register. Example: {@literal PlayerDropItemEvent.class}
     * @param <T>        A cancellable event type.
     * @param condition  The condition under which the event is cancelled.
     * @param priority   The event priority: {@link EventPriority}.
     */
    public <T extends Event & Cancellable> void cancelEvent(Class<T> eventClass, Predicate<T> condition, EventPriority priority) {
        registerEventExecutor(eventClass, priority, getEventCanceller(condition));
    }

    //------- checker methods ------

    /**
     * Internal util method for ensuring, that the given class implementing the {@link Cancellable} interface, extends the {@link Event} class.
     * @param cancellableClass The class to check.
     * @return The class that extends the {@link Event} class
     */
    protected static Class<? extends Event> ensureCancellableIsEventClass(Class<?extends Cancellable> cancellableClass) {
        if (!Event.class.isAssignableFrom(cancellableClass))
            // is Event a super class of cancellable class / does class implementing Cancellable interface extend the Event class? #
            // if not throw exception
            throw new IllegalArgumentException(cancellableClass.getName() + " has to extend org.bukkit.event.Event");
        //noinspection unchecked
        return (Class<? extends Event>) cancellableClass;
    }

    //--------- Event cancellers --------

    public EventExecutor getEventCanceller() {
        return (listener1, event) -> {
            if (event instanceof Cancellable cancellable) {
                cancellable.setCancelled(true);
            }
        };
    }

    public EventExecutor getEventCanceller(BooleanSupplier condition) {
        return (listener, event) -> {
            if (event instanceof Cancellable cancellable) {
                if (condition.getAsBoolean()) {
                    cancellable.setCancelled(true);
                }
            }
        };
    }

    public <T extends Event & Cancellable> EventExecutor getEventCanceller(Predicate<T> condition) {
        return (listener, event) -> {
            if (event instanceof Cancellable) {
                @SuppressWarnings("unchecked")
                // cast is okay, because it is an event and Cancellable at this point.
                T cancellableEvent = (T) event;
                if (condition.test(cancellableEvent)) {
                    cancellableEvent.setCancelled(true);
                }
            }
        };
    }

    // ------ Registering event executors ------

    // --- methods using default Bukkit EventExecutor ---

    // - with priority arg - Base method.
    // every method in this class that registers an event executor is going back to this method.

    /**
     * The base method of all event registering used by this class. This is the end point of every method that is registering anything within this class.
     *
     * @param eventClass The event class to register. Example: {@literal  PlayerDropItemEvent.class}
     * @param executor   An EventExecutor to execute the given event.
     * @param priority   The event priority: {@link EventPriority}.
     */
    protected void registerEventExecutor(Class<? extends Event> eventClass, EventPriority priority, EventExecutor executor) {
        Bukkit.getPluginManager().registerEvent(eventClass, listener, priority, executor, plugin);
    }

    // --- Methods using GenericEventExecutor ---

    // - with priority arg -

    /**
     * Use carefully: Registers an EventExecutor <strong>globally</strong>!
     *
     * @param eventClass The event class to register. Example: {@literal  PlayerDropItemEvent.class}
     * @param executor   An EventExecutor to execute the given event.
     * @param priority   The event priority: {@link EventPriority}.
     */
    public <T extends Event> void registerEventExecutor(Class<T> eventClass, EventPriority priority, GenericEventExecutor<T> executor) {
        registerEventExecutor(eventClass, priority, executor.unwrap(eventClass));
    }

    // - without priority arg -

    /**
     * Use carefully: Registers an EventExecutor <strong>globally</strong> with {@link EventPriority#NORMAL}!
     *
     * @param eventClass The event class to register. Example: {@literal  PlayerDropItemEvent.class}
     * @param executor   An EventExecutor to execute the given event.
     */
    public <T extends Event> void registerEventExecutor(Class<T> eventClass, GenericEventExecutor<T> executor) {
        registerEventExecutor(eventClass, EventPriority.NORMAL, executor.unwrap(eventClass));
    }

    // --- Methods using GenericEventExecutor and a Predicate condition ---

    // - with priority arg -

    /**
     * Use carefully: Registers an EventExecutor <strong>globally</strong>!
     *
     * @param eventClass The event class to register. Example: {@literal  PlayerDropItemEvent.class}
     * @param priority   The event priority: {@link EventPriority}.
     * @param executor   An EventExecutor to execute the given event.
     * @param condition  The condition under which the EventExecutor is run.
     * @param <T>        The event type that registered.
     */
    public <T extends Event> void registerEventExecutor(Class<T> eventClass, EventPriority priority, GenericEventExecutor<T> executor, Predicate<T> condition) {
        registerEventExecutor(eventClass, priority, ((GenericEventExecutor<T>) (event) -> {
            if (condition.test(event)) {
                executor.execute(event);
            }
        }).unwrap(eventClass));
    }

    // - without priority arg -

    /**
     * Use carefully: Registers an EventExecutor <strong>globally</strong>!
     *
     * @param eventClass The event class to register. Example: {@literal  PlayerDropItemEvent.class}
     * @param executor   An EventExecutor to execute the given event.
     * @param condition  The condition under which the EventExecutor is run.
     * @param <T>        The event type that registered.
     */
    public <T extends Event> void registerEventExecutor(Class<T> eventClass, GenericEventExecutor<T> executor, Predicate<T> condition) {
        registerEventExecutor(eventClass, EventPriority.NORMAL, executor, condition);
    }

    // --- Methods using GenericEventExecutor and a BooleanConsumer condition ---

    // - with priority arg -

    /**
     * Use carefully: Registers an EventExecutor <strong>globally</strong>!
     *
     * @param eventClass The event class to register. Example: {@literal  PlayerDropItemEvent.class}
     * @param priority   The event priority: {@link EventPriority}.
     * @param executor   An EventExecutor to execute the given event.
     * @param condition  The condition under which the EventExecutor is run.
     * @param <T>        The event type that registered.
     */
    public <T extends Event> void registerEventExecutor(Class<T> eventClass, EventPriority priority, GenericEventExecutor<T> executor, BooleanSupplier condition) {
        registerEventExecutor(eventClass, priority, ((GenericEventExecutor<T>) event -> {
            if (condition.getAsBoolean()) {
                executor.execute(event);
            }
        }).unwrap(eventClass));
    }

    // - without priority arg -

    /**
     * Use carefully: Registers an EventExecutor <strong>globally</strong>!
     *
     * @param eventClass The event class to register. Example: {@literal  PlayerDropItemEvent.class}
     * @param executor   An EventExecutor to execute the given event.
     * @param condition  The condition under which the EventExecutor is run.
     * @param <T>        The event type that registered.
     */
    public <T extends Event> void registerEventExecutor(Class<T> eventClass, GenericEventExecutor<T> executor, BooleanSupplier condition) {
        registerEventExecutor(eventClass, EventPriority.NORMAL, executor, condition);
    }

    // ------ END OF Registering event executors ------

    /**
     * Unregisters all events that are registered to the internal listener (The one specified in the constructor)
     */
    public void unregisterAll() {
        HandlerList.unregisterAll(listener);
    }

    // --- Getters ---

    /**
     * @return The plugin used to register all the event executors to.
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * @return The listener used to register all the event executors to.
     */
    public Listener getListener() {
        return listener;
    }
}
