package exceptions;

/**
 * InvalidTicketException is a custom checked exception thrown when a vehicle
 * exit is attempted using a ticket ID that does not exist or has already been
 * closed (i.e., the vehicle has already exited).
 *
 * <p>OOP2 Concept: Custom Checked Exception — enforces explicit handling
 * of invalid ticket scenarios at the call site.</p>
 *
 * @author SPLMS Team
 * @version 1.0
 */
public class InvalidTicketException extends Exception {

    /** Serial version UID for serialization. */
    private static final long serialVersionUID = 2L;

    /** The ticket ID that was deemed invalid. */
    private final String ticketId;

    /**
     * Constructs an InvalidTicketException with a detail message and the offending ticket ID.
     *
     * @param message  human-readable error message
     * @param ticketId the ticket ID that caused this exception
     */
    public InvalidTicketException(String message, String ticketId) {
        super(message);
        this.ticketId = ticketId;
    }

    /**
     * Returns the invalid ticket ID that caused this exception.
     *
     * @return the invalid ticket ID
     */
    public String getTicketId() {
        return ticketId;
    }
}
