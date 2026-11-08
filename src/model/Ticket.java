package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Ticket is a generic class that stores a complete record of a parking session
 * for any vehicle that extends {@link Vehicle}.
 *
 * <p>OOP2 Concept: <b>Generics</b> — the type parameter {@code T extends Vehicle}
 * ensures type safety while allowing Ticket to work with Car, Bike, or Truck.
 * This eliminates the need for casting and enables compile-time type checking.</p>
 *
 * <p>Also implements {@link Serializable} so tickets can be persisted to disk.</p>
 *
 * @param <T> the specific type of vehicle for this ticket (must extend {@link Vehicle})
 *
 * @author SPLMS Team
 * @version 1.0
 */
public class Ticket<T extends Vehicle> implements Serializable {

    /** Serial version UID for serialization. */
    private static final long serialVersionUID = 300L;

    /** Date-time formatter for readable display. */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Static counter used to generate unique ticket IDs. */
    private static int idCounter = 1;

    /** Unique ticket identifier (e.g., "TKT-001"). */
    private final String ticketId;

    /** The vehicle associated with this ticket. */
    private final T vehicle;

    /** The date and time this ticket was issued (entry time). */
    private final LocalDateTime entryTime;

    /** The date and time this ticket was closed (exit time). Null if still active. */
    private LocalDateTime exitTime;

    /** The calculated parking fee for this session. -1 if still active. */
    private double fee;

    /** Whether this ticket has been closed (vehicle has exited). */
    private boolean closed;

    /** The slot ID where the vehicle was parked. */
    private final String slotId;

    /**
     * Constructs a new Ticket for the given vehicle, capturing entry time and slot.
     *
     * @param vehicle  the vehicle for which this ticket is issued
     * @param slotId   the ID of the slot assigned to this vehicle
     */
    public Ticket(T vehicle, String slotId) {
        this.ticketId  = String.format("TKT-%03d", idCounter++);
        this.vehicle   = vehicle;
        this.entryTime = vehicle.getEntryTime();
        this.exitTime  = null;
        this.fee       = -1.0;
        this.closed    = false;
        this.slotId    = slotId;
    }

    /**
     * Constructs a Ticket with an explicit ID counter value (used when loading from file).
     *
     * @param vehicle   the vehicle for which this ticket is issued
     * @param slotId    the ID of the slot assigned to this vehicle
     * @param ticketId  the pre-assigned ticket ID
     */
    public Ticket(T vehicle, String slotId, String ticketId) {
        this.ticketId  = ticketId;
        this.vehicle   = vehicle;
        this.entryTime = vehicle.getEntryTime();
        this.exitTime  = null;
        this.fee       = -1.0;
        this.closed    = false;
        this.slotId    = slotId;
    }

    /**
     * Closes this ticket by recording the exit time and calculating the fee.
     * Once closed, the ticket cannot be re-closed.
     *
     * @param exitTime the time of vehicle exit
     */
    public void close(LocalDateTime exitTime) {
        this.exitTime = exitTime;
        this.closed   = true;

        // Calculate duration in minutes between entry and exit
        long durationMinutes = java.time.Duration
                .between(entryTime, exitTime)
                .toMinutes();

        // Polymorphic call — dispatches to Car/Bike/Truck's calculateFee()
        this.fee = vehicle.calculateFee(durationMinutes);
    }

    // ───────────────────────────── Getters ──────────────────────────────────

    /** @return the unique ticket ID */
    public String getTicketId()    { return ticketId; }

    /** @return the vehicle associated with this ticket */
    public T getVehicle()          { return vehicle; }

    /** @return the entry date-time */
    public LocalDateTime getEntryTime()  { return entryTime; }

    /** @return the exit date-time, or null if still active */
    public LocalDateTime getExitTime()   { return exitTime; }

    /** @return the calculated fee, or -1 if still active */
    public double getFee()         { return fee; }

    /** @return true if this ticket has been closed */
    public boolean isClosed()      { return closed; }

    /** @return the parking slot ID for this ticket */
    public String getSlotId()      { return slotId; }

    /**
     * Sets the internal static ID counter (used when loading tickets from file
     * to avoid duplicate ticket IDs on the next session).
     *
     * @param nextId the next counter value to use
     */
    public static void setIdCounter(int nextId) {
        idCounter = nextId;
    }

    /**
     * Returns the current value of the static ID counter.
     *
     * @return current ID counter
     */
    public static int getIdCounter() {
        return idCounter;
    }

    /**
     * Prints a formatted parking receipt for this ticket.
     * This is only meaningful after the ticket has been closed.
     *
     * @return the formatted receipt as a multi-line string
     */
    public String generateReceipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔══════════════════════════════════════════════╗\n");
        sb.append("║        SMART PARKING LOT - RECEIPT           ║\n");
        sb.append("╠══════════════════════════════════════════════╣\n");
        sb.append(String.format("║  Ticket ID   : %-29s║%n", ticketId));
        sb.append(String.format("║  Plate No.   : %-29s║%n", vehicle.getPlateNumber()));
        sb.append(String.format("║  Vehicle Type: %-29s║%n", vehicle.getVehicleType()));
        sb.append(String.format("║  Slot ID     : %-29s║%n", slotId));
        sb.append(String.format("║  Entry Time  : %-29s║%n",
                entryTime != null ? entryTime.format(FORMATTER) : "N/A"));
        sb.append(String.format("║  Exit Time   : %-29s║%n",
                exitTime != null ? exitTime.format(FORMATTER) : "N/A"));

        if (closed && exitTime != null) {
            long mins = java.time.Duration.between(entryTime, exitTime).toMinutes();
            String durStr = String.format("%d hr %d min", mins / 60, mins % 60);
            sb.append(String.format("║  Duration    : %-29s║%n", durStr));
            sb.append(String.format("║  Total Fee   : Rs. %-25.2f║%n", fee));
        } else {
            sb.append("║  Status      : ACTIVE (not yet closed)       ║\n");
        }
        sb.append("╚══════════════════════════════════════════════╝");
        return sb.toString();
    }

    /**
     * Returns a concise string summary of this ticket.
     *
     * @return summary string
     */
    @Override
    public String toString() {
        return String.format("%s | %s | %s | Slot: %s | %s",
                ticketId,
                vehicle.getPlateNumber(),
                vehicle.getVehicleType(),
                slotId,
                closed ? String.format("CLOSED | Fee: Rs.%.2f", fee) : "ACTIVE");
    }
}
