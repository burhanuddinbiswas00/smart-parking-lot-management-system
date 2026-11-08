package model;

/**
 * Car is a concrete subclass of {@link Vehicle} representing a standard car.
 *
 * <p>Hourly rate: Rs. 30 per hour (Rs. 1 per minute for the first hour,
 * then full hourly rate applies for any partial hour thereafter).</p>
 *
 * <p>OOP2 Concepts:</p>
 * <ul>
 *   <li><b>Inheritance</b> — extends {@link Vehicle}</li>
 *   <li><b>Polymorphism</b> — overrides {@code calculateFee()} with Car-specific logic</li>
 *   <li><b>Encapsulation</b> — hourly rate is a private constant</li>
 * </ul>
 *
 * @author SPLMS Team
 * @version 1.0
 */
public class Car extends Vehicle {

    /** Serial version UID for serialization. */
    private static final long serialVersionUID = 101L;

    /** Parking rate in rupees per hour for a Car. */
    private static final double HOURLY_RATE = 30.0;

    /**
     * Constructs a Car with the given plate number.
     * Automatically sets the vehicle type to "Car".
     *
     * @param plateNumber the unique license plate number of this car
     */
    public Car(String plateNumber) {
        super(plateNumber, "Car");
    }

    /**
     * Calculates the parking fee for a Car based on the duration.
     *
     * <p>Billing logic:</p>
     * <ul>
     *   <li>First 15 minutes are free.</li>
     *   <li>After that, charge is applied per-minute at the hourly rate.</li>
     *   <li>Minimum charge after the free period is Rs. 10.</li>
     * </ul>
     *
     * <p>OOP2 Concept: <b>Runtime Polymorphism</b> — this method is called
     * on a Vehicle reference at runtime, dispatching to this Car-specific logic.</p>
     *
     * @param durationMinutes total duration parked in minutes
     * @return calculated fee in rupees
     */
    @Override
    public double calculateFee(long durationMinutes) {
        if (durationMinutes <= 15) {
            return 0.0; // Free grace period
        }
        // Calculate billable hours (round up any partial hour)
        long billableMinutes = durationMinutes - 15;
        double hours = Math.ceil(billableMinutes / 60.0);
        double fee = hours * HOURLY_RATE;
        return Math.max(fee, 10.0); // Minimum charge of Rs. 10
    }

    /**
     * Returns the hourly rate for a Car.
     *
     * @return hourly rate in rupees
     */
    public static double getHourlyRate() {
        return HOURLY_RATE;
    }

    /**
     * Returns a string representation of this car.
     *
     * @return formatted car summary
     */
    @Override
    public String toString() {
        return super.toString() + String.format(" | Rate: Rs.%.0f/hr", HOURLY_RATE);
    }
}
