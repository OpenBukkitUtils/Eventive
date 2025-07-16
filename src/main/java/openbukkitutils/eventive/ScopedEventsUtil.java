package openbukkitutils.eventive;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

/**
 * Like EventsUtil, but has a condition that has to be true in order for the event handlers registered with this util to be triggered.
 * <br><br>
 * Can be used to have specific scope in which the events are handled by this executor.
 * It also helps to not have to cancel each event individually with if-clauses.
 */
public class ScopedEventsUtil extends EventsUtil {

    @NotNull
    private final BooleanSupplier condition;

    public ScopedEventsUtil(@NotNull Plugin plugin, @Nullable Listener listener, @NotNull BooleanSupplier condition) {
        super(plugin, listener);
        this.condition = condition;
    }

    public ScopedEventsUtil(Plugin plugin, BooleanSupplier condition) {
        this(plugin, null, condition);
    }

    // only overriding this base method because every other method in EventsUtil is going back to referencing this method.

    @Override
    protected void registerEventExecutor(Class<? extends Event> eventClass, EventPriority priority, EventExecutor executor) {
        Bukkit.getPluginManager().registerEvent(eventClass, listener, priority, conditionWrap(executor), plugin);
    }

    /**
     * Wraps this the given executor in the condition that is defined in this class.
     * The wrapped EventExecutor can only be executed if the condition returns true.
     *
     * @param executor The EventExecutor to wrap with the condition.
     * @return The wrapped EventExecutor
     */
    private EventExecutor conditionWrap(EventExecutor executor) {
        return (listener, event) -> {
            if (condition.getAsBoolean()) executor.execute(listener, event);
        };
    }

    @NotNull
    public BooleanSupplier getCondition() {
        return condition;
    }
}
