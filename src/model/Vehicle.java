package model;

import interfaces.Parkable;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Vehicle is an abstract base class representing any vehicle that can use
 * the Smart Parking Lot Management System.
 *
 * <p>OOP2 Concepts demonstrated:</p>
 * <ul>
 *   <li><b>Abstraction</b> — abstract class with abstract method {@code calculateFee()}</li>
 *   <li><b>Encapsulation</b> — private fields with public getters/setters</li>
 *   <li><b>Inheritance</b> — Car, Bike, Truck extend this class</li>
 *   <li><b>Interface implementation</b> — implements {@link Parkable}</li>
 *   <li><b>Serializable</b> — for file I/O persistence</li>
 * </ul>
 *
 * @author SPLMS Team
 * @version 1.0
 */
public abstract class Vehicle implements Parkable, Serializable {

    /** Serial version UID for serialization. */
    private static final long serialVersionUID = 100L;

    /** The unique license plate number of the vehicle. */
    private String plateNumber;

    /** The type of vehicle (Car, Bike, Truck). */
    private String vehicleType;

    /** The date and time the vehicle entered the parking lot. */
    private LocalDateTime entryTime;

    /** The date and time the vehicle exited the parking lot. */
    private LocalDateTime exitTime;

    /** The parking slot currently assigned to this vehicle (null if not parked). */
    private transient ParkingSlot assignedSlot;

    /**
     * Constructs a Vehicle with the given plate number and vehicle type.
     * Entry time is automatically set to the current date and time.
     *
     * @param plateNumber the license plate number (must be unique)
     * @param vehicleType the descriptive type of the vehicle
     */
    public Vehicle(String plateNumber, String vehicleType) {
        this.plateNumber  = plateNumber;
        this.vehicleType  = vehicleType;
        this.entryTime    = LocalDateTime.now();
        this.exitTime     = null;
        this.assignedSlot = null;
    }

    // ───────────────────────────── Abstract method ──────────────────────────

    /**
     * Calculates the parking fee for the given duration.
     *
     * <p>OOP2 Concept: <b>Polymorphism</b> — each subclass provides its own
     * fee calculation based on its unique hourly rate.</p>
     *
     * @param durationMinutes the total duration the vehicle was parked, in minutes
     * @return the calculated parking fee in rupees
     */
    public abstract double calculateFee(long durationMinutes);

    // ───────────────────────────── Parkable impl ────────────────────────────

    /**
     * Parks this vehicle in the specified slot, updating the slot's occupancy.
     *
     * @param slot the {@link ParkingSlot} to assign to this vehicle
     */
    @Override
    public void park(ParkingSlot slot) {
        this.assignedSlot = slot;
        slot.occupy(this);
    }

    /**
     * Unparks this vehicle from its currently assigned slot, freeing the slot
     * and recording the exit time.
     */
    @Override
    public void unpark() {
        if (assignedSlot != null) {
            assignedSlot.vacate();
            assignedSlot = null;
        }
        this.exitTime = LocalDateTime.now();
    }

    // ───────────────────────────── Getters / Setters ────────────────────────

    /**
     * Returns the license plate number of this vehicle.
     *
     * @return plate number string
     */
    public String getPlateNumber() { return plateNumber; }

    /**
     * Returns the vehicle type label (Car, Bike, Truck).
     *
     * @return vehicle type string
     */
    public String getVehicleType() { return vehicleType; }

    /**
     * Returns the entry date-time of this vehicle.
     *
     * @return entry {@link LocalDateTime}
     */
    public LocalDateTime getEntryTime() { return entryTime; }

    /**
     * Returns the exit date-time of this vehicle, or null if still parked.
     *
     * @return exit {@link LocalDateTime}, or null
     */
    public LocalDateTime getExitTime() { return exitTime; }

    /**
     * Sets the exit time explicitly (used during serialization reload).
     *
     * @param exitTime the exit {@link LocalDateTime}
     */
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }

    /**
     * Sets the entry time explicitly (used during serialization reload).
     *
     * @param entryTime the entry {@link LocalDateTime}
     */
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }

    /**
     * Returns the parking slot currently assigned to this vehicle.
     *
     * @return the assigned {@link ParkingSlot}, or null if not parked
     */
    public ParkingSlot getAssignedSlot() { return assignedSlot; }

    /**
     * Sets the assigned slot reference (used internally during park/unpark).
     *
     * @param slot the {@link ParkingSlot} to assign
     */
    public void setAssignedSlot(ParkingSlot slot) { this.assignedSlot = slot; }

    /**
     * Returns a string representation of the vehicle for display purposes.
     *
     * @return formatted vehicle summary
     */
    @Override
    public String toString() {
        return String.format("[%s] Plate: %-12s | Entry: %s",
                vehicleType, plateNumber,
                entryTime != null ? entryTime.toString().replace("T", " ") : "N/A");
    }
}
