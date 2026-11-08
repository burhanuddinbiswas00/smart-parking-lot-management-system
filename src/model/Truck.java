package model;

/**
 * Truck is a concrete subclass of {@link Vehicle} representing a heavy truck or lorry.
 *
 * <p>Hourly rate: Rs. 50 per hour.</p>
 *
 * <p>OOP2 Concepts:</p>
 * <ul>
 *   <li><b>Inheritance</b> — extends {@link Vehicle}</li>
 *   <li><b>Polymorphism</b> — overrides {@code calculateFee()} with Truck-specific logic</li>
 *   <li><b>Encapsulation</b> — hourly rate is a private constant</li>
 * </ul>
 *
 * @author SPLMS Team
 * @version 1.0
 */
public class Truck extends Vehicle {

    /** Serial version UID for serialization. */
    private static final long serialVersionUID = 103L;

    /** Parking rate in rupees per hour for a Truck. */
    private static final double HOURLY_RATE = 50.0;

    /**
     * Constructs a Truck with the given plate number.
     * Automatically sets the vehicle type to "Truck".
     *
     * @param plateNumber the unique license plate number of this truck
     */
    public Truck(String plateNumber) {
        super(plateNumber, "Truck");
    }

    /**
     * Calculates the parking fee for a Truck based on the duration.
     *
     * <p>Billing logic:</p>
     * <ul>
     *   <li>No free grace period for trucks.</li>
     *   <li>Charge starts from the first minute, rounded up to the nearest hour.</li>
     *   <li>Minimum charge: Rs. 50 (full first hour).</li>
     * </ul>
     *
     * <p>OOP2 Concept: <b>Runtime Polymorphism</b> — this method is resolved
     * dynamically at runtime when called on a Vehicle reference.</p>
     *
     * @param durationMinutes total duration parked in minutes
     * @return calculated fee in rupees
     */
    @Override
    public double calculateFee(long durationMinutes) {
        if (durationMinutes <= 0) {
            return HOURLY_RATE; // Minimum one-hour charge
        }
        double hours = Math.ceil(durationMinutes / 60.0);
        return hours * HOURLY_RATE;
    }

    /**
     * Returns the hourly rate for a Truck.
     *
     * @return hourly rate in rupees
     */
    public static double getHourlyRate() {
        return HOURLY_RATE;
    }

    /**
     * Returns a string representation of this truck.
     *
     * @return formatted truck summary
     */
    @Override
    public String toString() {
        return super.toString() + String.format(" | Rate: Rs.%.0f/hr", HOURLY_RATE);
    }
}
