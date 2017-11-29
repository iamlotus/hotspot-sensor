package hotspotsensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Expose notification as JMXBean, there can be at most one instance of {@code MBeanNotificationHandler} in every JVM
 * process.
 *
 * @author lotus.jzx
 */
public class MBeanNotificationHandler<E> implements NotificationHandler<E> {

    private static final Logger LOG = LoggerFactory.getLogger(MBeanNotificationHandler.class);

    private Notification<E> notification;


    public interface HotspotSensorNotificationMBean<T> {

        boolean isElementSetChanged();

        Set<? extends T> getElementSet();

        boolean isEntrySetChanged();

        Map<T, Integer> getEntrySet();

        long getTotalCount();
    }


    public class HotspotSensorNotification implements HotspotSensorNotificationMBean<E> {

        @Override
        public boolean isElementSetChanged() {
            return notification == null ? false : notification.isElementSetChanged();
        }

        @Override
        public Set<? extends E> getElementSet() {
            return notification == null ? Collections.emptySet() : notification.getElementSet();
        }

        @Override
        public boolean isEntrySetChanged() {
            return notification == null ? false : notification.isEntrySetChanged();
        }

        @Override
        public Map<E, Integer> getEntrySet() {
            return notification == null
                ? Collections.emptyMap()
                : notification.getEntrySet().stream().collect(Collectors.toMap(e -> e.getElement(), e -> e.getCount()));
        }

        @Override
        public long getTotalCount() {
            return notification == null ? 0 : notification.getTotalCount();
        }
    }



    public MBeanNotificationHandler() {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            String names = HotspotSensorNotificationMBean.class.getName();
            String packageName = names.substring(0, names.lastIndexOf('.'));
            String className = names.substring(names.lastIndexOf('.') + 1);
            ObjectName name = new ObjectName(packageName + ":type=" + className);
            HotspotSensorNotificationMBean mbean = new HotspotSensorNotification();
            mbs.registerMBean(mbean, name);
            LOG.debug("JMXBean register successfully ");
        } catch (InstanceAlreadyExistsException e) {
            LOG.error("Find duplicated instance of {}, there can be only one instance in one JVM");
            throw new RuntimeException(e);
        } catch (Exception e) {
            LOG.warn("JMXBean register unsuccessfully ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleNotification(Notification<E> notification) {
        this.notification = notification;
    }
}
