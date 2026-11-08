package manager;

import model.*;

/**
 * VehicleFactory is a Factory class that creates Vehicle objects based on a
 * string type input.
 *
 * <p>OOP2 Concept: <b>Factory Design Pattern</b> — centralizes object creation
 * logic, hiding the constructor details from the caller. The client only needs
 * to know the type string and plate number; the factory decides which concrete
 * class to instantiate.</p>
 *
 * <p>This approach makes it easy to add new vehicle types in the future without
 * changing any caller code — just add a new case to the factory.</p>
 *
 * @author SPLMS Team
 * @version 1.0
 */
public class VehicleFactory {

    /**
     * Private constructor — VehicleFactory should not be instantiated.
     * All methods are static.
     */
    private VehicleFactory() {
        throw new UnsupportedOperationException("VehicleFactory is a utility class.");
    }

    /**
     * Creates and returns a concrete {@link Vehicle} object based on the given type.
     *
     * <p>OOP2 Concept: <b>Factory Pattern + Polymorphism</b> — the returned type
     * is {@link Vehicle} (the abstract supertype), but the actual runtime object
     * is a Car, Bike, or Truck — enabling polymorphic behavior at the call site.</p>
     *
     * @param type        the vehicle type string: "Car", "Bike", or "Truck"
     *                    (case-insensitive)
     * @param plateNumber the license plate number to assign to the vehicle
     * @return the newly created {@link Vehicle} instance
     * @throws IllegalArgumentException if the type string does not match any
     *                                  known vehicle type
     */
    public static Vehicle createVehicle(String type, String plateNumber) {
        if (type == null || plateNumber == null) {
            throw new IllegalArgumentException("Vehicle type and plate number cannot be null.");
        }

        switch (type.trim().toLowerCase()) {
            case "car":
            case "1":
                return new Car(plateNumber.toUpperCase().trim());
            case "bike":
            case "2":
                return new Bike(plateNumber.toUpperCase().trim());
            case "truck":
            case "3":
                return new Truck(plateNumber.toUpperCase().trim());
            default:
                throw new IllegalArgumentException(
                    "Unknown vehicle type: '" + type + "'. Valid types: Car (1), Bike (2), Truck (3).");
        }
    }

    /**
     * Returns a formatted list of all supported vehicle types and their rates.
     *
     * @return multi-line string describing supported types
     */
    public static String getSupportedTypes() {
        return "  1. Car   - Rs." + (int) Car.getHourlyRate()   + "/hr\n"
             + "  2. Bike  - Rs." + (int) Bike.getHourlyRate()  + "/hr\n"
             + "  3. Truck - Rs." + (int) Truck.getHourlyRate() + "/hr";
    }
}
