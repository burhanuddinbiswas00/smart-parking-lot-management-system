package model;

/**
 * Bike is a concrete subclass of {@link Vehicle} representing a motorcycle or bicycle.
 *
 * <p>Hourly rate: Rs. 10 per hour.</p>
 *
 * <p>OOP2 Concepts:</p>
 * <ul>
 *   <li><b>Inheritance</b> — extends {@link Vehicle}</li>
 *   <li><b>Polymorphism</b> — overrides {@code calculateFee()} with Bike-specific logic</li>
 *   <li><b>Encapsulation</b> — hourly rate is a private constant</li>
 * </ul>
 *
 * @author SPLMS Team
 * @version 1.0
 */
public class Bike extends Vehicle {

    /** Serial version UID for serialization. */
    private static final long serialVersionUID = 102L;

    /** Parking rate in rupees per hour for a Bike. */
    private static final double HOURLY_RATE = 10.0;

    /**
     * Constructs a Bike with the given plate number.
     * Automatically sets the vehicle type to "Bike".
     *
     * @param plateNumber the unique license plate number of this bike
     */
    public Bike(String plateNumber) {
        super(plateNumber, "Bike");
    }

    /**
     * Calculates the parking fee for a Bike based on the duration.
     *
     * <p>Billing logic:</p>
     * <ul>
     *   <li>First 30 minutes are free (bikes get a longer grace period).</li>
     *   <li>After that, charge is applied per-minute at the hourly rate.</li>
     *   <li>Minimum charge after the free period is Rs. 5.</li>
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
        if (durationMinutes <= 30) {
            return 0.0; // Free grace period for bikes
        }
        long billableMinutes = durationMinutes - 30;
        double hours = Math.ceil(billableMinutes / 60.0);
        double fee = hours * HOURLY_RATE;
        return Math.max(fee, 5.0); // Minimum charge of Rs. 5
    }

    /**
     * Returns the hourly rate for a Bike.
     *
     * @return hourly rate in rupees
     */
    public static double getHourlyRate() {
        return HOURLY_RATE;
    }

    /**
     * Returns a string representation of this bike.
     *
     * @return formatted bike summary
     */
    @Override
    public String toString() {
        return super.toString() + String.format(" | Rate: Rs.%.0f/hr", HOURLY_RATE);
    }
}
