package interfaces;

/**
 * Notifiable interface defines the contract for any entity that can receive
 * notifications in the Smart Parking Lot Management System.
 *
 * <p>OOP2 Concept: Interface + Observer Pattern — when a slot frees up,
 * all waiting customers in the queue are notified via this interface.</p>
 *
 * @author SPLMS Team
 * @version 1.0
 */
public interface Notifiable {

    /**
     * Sends a notification message to the implementing entity.
     *
     * @param message the notification message to display
     */
    void notify(String message);
}
