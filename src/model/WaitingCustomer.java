package model;

import interfaces.Notifiable;

/**
 * WaitingCustomer represents a customer/vehicle that is in the waiting queue
 * because no parking slot was available at the time of entry.
 *
 * <p>OOP2 Concepts:</p>
 * <ul>
 *   <li><b>Observer Pattern</b> — WaitingCustomer acts as an observer; when a slot
 *       becomes available, the {@link manager.ParkingLotManager} notifies all
 *       waiting customers via the {@link Notifiable} interface.</li>
 *   <li><b>Interface Implementation</b> — implements {@link Notifiable}.</li>
 *   <li><b>Encapsulation</b> — stores the associated vehicle privately.</li>
 * </ul>
 *
 * @author SPLMS Team
 * @version 1.0
 */
public class WaitingCustomer implements Notifiable, java.io.Serializable {

    /** Serial version UID for serialization. */
    private static final long serialVersionUID = 400L;

    /** The vehicle associated with this waiting customer. */
    private final Vehicle vehicle;

    /** Customer name or contact identifier. */
    private final String customerName;

    /**
     * Constructs a WaitingCustomer with the given vehicle and customer name.
     *
     * @param vehicle      the {@link Vehicle} that is waiting for a slot
     * @param customerName the name or contact info of the customer
     */
    public WaitingCustomer(Vehicle vehicle, String customerName) {
        this.vehicle      = vehicle;
        this.customerName = customerName;
    }

    /**
     * Receives and displays a notification message.
     *
     * <p>OOP2 Concept: <b>Observer Pattern</b> — this method is called by the
     * subject (ParkingLotManager) to push slot-availability updates to observers.</p>
     *
     * @param message the notification message to display
     */
    @Override
    public void notify(String message) {
        System.out.println("\n  [NOTIFICATION] Customer '" + customerName
                + "' (Plate: " + vehicle.getPlateNumber() + "): " + message);
    }

    /**
     * Returns the vehicle associated with this waiting customer.
     *
     * @return the waiting {@link Vehicle}
     */
    public Vehicle getVehicle() {
        return vehicle;
    }

    /**
     * Returns the customer's name or contact identifier.
     *
     * @return customer name string
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Returns a concise string representation of this waiting customer.
     *
     * @return formatted waiting customer summary
     */
    @Override
    public String toString() {
        return String.format("Customer: %-15s | Vehicle: %s (%s)",
                customerName, vehicle.getPlateNumber(), vehicle.getVehicleType());
    }
}
