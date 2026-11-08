package exceptions;

/**
 * VehicleAlreadyParkedException is a custom checked exception thrown when a
 * vehicle with the same plate number attempts to enter the parking lot while
 * it is already parked inside.
 *
 * <p>OOP2 Concept: Custom Checked Exception — prevents duplicate entries and
 * forces the caller to handle duplicate plate scenarios explicitly.</p>
 *
 * @author SPLMS Team
 * @version 1.0
 */
public class VehicleAlreadyParkedException extends Exception {

    /** Serial version UID for serialization. */
    private static final long serialVersionUID = 3L;

    /** The plate number of the duplicate vehicle. */
    private final String plateNumber;

    /**
     * Constructs a VehicleAlreadyParkedException with a detail message and plate number.
     *
     * @param message     human-readable error message
     * @param plateNumber the plate number of the vehicle already inside the lot
     */
    public VehicleAlreadyParkedException(String message, String plateNumber) {
        super(message);
        this.plateNumber = plateNumber;
    }

    /**
     * Returns the plate number of the vehicle that triggered this exception.
     *
     * @return the plate number string
     */
    public String getPlateNumber() {
        return plateNumber;
    }
}
