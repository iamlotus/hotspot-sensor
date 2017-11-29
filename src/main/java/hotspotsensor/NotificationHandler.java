package hotspotsensor;

import java.util.Set;

/**
 * Handle hotspot notification, customize as your need.
 *
 * @author iamlotus@gmail.com
 */
public interface NotificationHandler<E> {

    void handleNotification(Notification<E> notification);

    interface Notification<T> {

        /**
         * Is hot element set changed since last {@link NotificationHandler#handleNotification(Notification)}.
         *
         * @return true changed, false else
         */
        boolean isElementSetChanged();

        /**
         * All hot elements in the {@link Notification}
         *
         * @return set
         */
        Set<? extends T> getElementSet();

        /**
         * Is hot element entry set changed since last
         * {@link NotificationHandler#handleNotification(Notification)}. {@link Entry} contains element and
         * count, changes on element or count will both cause entry change. It is true if
         * {@link #isEntrySetChanged()} return true then {@link #getElementSet()} will return true, but NOT vise veral.
         *
         * @return true changed, false else
         */
        boolean isEntrySetChanged();

        /**
         * All hot element and counts in the {@link Notification}
         *
         * @return set
         */
        Set<Entry<T>> getEntrySet();

        /**
         * Total count
         * {@link HotspotSensor#increase(Object)} is invoked since last {@link NotificationHandler#handleNotification(Notification)}
         *
         * @return total count
         */
        long getTotalCount();
    }
}
