package model;

/**
 * ParkingSlot represents a single physical parking space in the lot.
 *
 * <p>Each slot has an ID, a vehicle type it supports, and a flag indicating
 * whether it is currently occupied.</p>
 *
 * <p>OOP2 Concept: <b>Encapsulation</b> — all fields are private with
 * controlled access through getter/setter methods.</p>
 *
 * @author SPLMS Team
 * @version 1.0
 */
public class ParkingSlot implements java.io.Serializable {

    /** Serial version UID for serialization. */
    private static final long serialVersionUID = 200L;

    /** Unique identifier for this slot (e.g., "C-01", "B-01", "T-01"). */
    private final String slotId;

    /**
     * The type of vehicle this slot is designated for.
     * Valid values: "Car", "Bike", "Truck".
     */
    private final String slotType;

    /** Whether this slot is currently occupied. */
    private boolean occupied;

    /** The vehicle currently occupying this slot (null if vacant). */
    private transient Vehicle currentVehicle;

    /**
     * Constructs a ParkingSlot with the given ID and designated vehicle type.
     *
     * @param slotId   the unique slot identifier
     * @param slotType the vehicle type this slot accommodates ("Car", "Bike", "Truck")
     */
    public ParkingSlot(String slotId, String slotType) {
        this.slotId         = slotId;
        this.slotType       = slotType;
        this.occupied       = false;
        this.currentVehicle = null;
    }

    /**
     * Marks this slot as occupied by the given vehicle.
     *
     * @param vehicle the {@link Vehicle} occupying this slot
     */
    public void occupy(Vehicle vehicle) {
        this.currentVehicle = vehicle;
        this.occupied = true;
    }

    /**
     * Marks this slot as vacant, clearing the vehicle reference.
     */
    public void vacate() {
        this.currentVehicle = null;
        this.occupied = false;
    }

    /**
     * Returns the unique identifier of this slot.
     *
     * @return slot ID string
     */
    public String getSlotId() { return slotId; }

    /**
     * Returns the vehicle type this slot supports.
     *
     * @return slot type string ("Car", "Bike", "Truck")
     */
    public String getSlotType() { return slotType; }

    /**
     * Returns whether this slot is currently occupied.
     *
     * @return true if occupied, false if vacant
     */
    public boolean isOccupied() { return occupied; }

    /**
     * Returns the vehicle currently in this slot.
     *
     * @return the current {@link Vehicle}, or null if vacant
     */
    public Vehicle getCurrentVehicle() { return currentVehicle; }

    /**
     * Returns a formatted string representation of this slot's status.
     *
     * @return status string (OCCUPIED/VACANT with details)
     */
    @Override
    public String toString() {
        if (occupied && currentVehicle != null) {
            return String.format("Slot %-6s [%s] --> OCCUPIED by %s",
                    slotId, slotType, currentVehicle.getPlateNumber());
        }
        return String.format("Slot %-6s [%s] --> VACANT", slotId, slotType);
    }
}
