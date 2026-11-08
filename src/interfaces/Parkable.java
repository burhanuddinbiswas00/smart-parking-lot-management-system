package interfaces;

import model.ParkingSlot;

/**
 * Parkable interface defines the contract for any vehicle that can be parked
 * or unparked in the Smart Parking Lot Management System.
 *
 * <p>OOP2 Concept: Interface — provides a behavioral contract (abstraction)
 * that concrete vehicle classes must implement.</p>
 *
 * @author SPLMS Team
 * @version 1.0
 */
public interface Parkable {

    /**
     * Parks the vehicle in the given parking slot.
     *
     * @param slot the {@link ParkingSlot} where the vehicle will be parked
     */
    void park(ParkingSlot slot);

    /**
     * Unparks the vehicle from its currently assigned slot.
     * Should clear the slot reference and set the exit time.
     */
    void unpark();
}
