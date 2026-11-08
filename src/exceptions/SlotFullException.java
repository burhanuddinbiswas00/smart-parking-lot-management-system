package exceptions;

/**
 * SlotFullException is a custom checked exception thrown when no parking slot
 * is available for the given vehicle type.
 *
 * <p>OOP2 Concept: Custom Checked Exception — forces the caller to handle or
 * declare this exception, ensuring proper error propagation. When caught, the
 * vehicle should be added to the waiting queue.</p>
 *
 * @author SPLMS Team
 * @version 1.0
 */
public class SlotFullException extends Exception {

    /** Serial version UID for serialization. */
    private static final long serialVersionUID = 1L;

    /** The type of vehicle that could not be parked. */
    private final String vehicleType;

    /**
     * Constructs a SlotFullException with a detail message and the vehicle type.
     *
     * @param message    human-readable error message
     * @param vehicleType the type of vehicle (Car, Bike, Truck) that triggered this exception
     */
    public SlotFullException(String message, String vehicleType) {
        super(message);
        this.vehicleType = vehicleType;
    }

    /**
     * Returns the vehicle type that caused the slot-full condition.
     *
     * @return the vehicle type string
     */
    public String getVehicleType() {
        return vehicleType;
    }
}
