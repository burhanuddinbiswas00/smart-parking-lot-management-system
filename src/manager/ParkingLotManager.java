package manager;

import exceptions.InvalidTicketException;
import exceptions.SlotFullException;
import exceptions.VehicleAlreadyParkedException;
import model.*;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ParkingLotManager is the central controller (Singleton) for the Smart Parking
 * Lot Management System. It is the only class that holds and coordinates all
 * shared system state.
 *
 * <p>OOP2 Concepts demonstrated:</p>
 * <ul>
 *   <li><b>Singleton Pattern</b> — only one instance exists; accessed via
 *       {@link #getInstance()}</li>
 *   <li><b>Collections</b> — uses HashMap, Queue, ArrayList, and Iterator</li>
 *   <li><b>Exception Handling</b> — throws and catches custom checked exceptions</li>
 *   <li><b>File I/O</b> — serializes/deserializes session data using ObjectStreams</li>
 *   <li><b>Observer Pattern</b> — notifies waiting customers when a slot opens</li>
 *   <li><b>Generics</b> — works with {@code Ticket<?>} (wildcard generic type)</li>
 * </ul>
 *
 * @author SPLMS Team
 * @version 1.0
 */
public class ParkingLotManager {

    // ───────────────────────────── Singleton ────────────────────────────────

    /** The single, lazily-created instance of this manager. */
    private static ParkingLotManager instance;

    /**
     * Returns the single instance of ParkingLotManager.
     * Thread-safe via synchronized (important for multi-threaded environments).
     *
     * <p>OOP2 Concept: <b>Singleton Pattern</b> — ensures global, shared
     * access to one and only one ParkingLotManager object.</p>
     *
     * @return the singleton ParkingLotManager instance
     */
    public static synchronized ParkingLotManager getInstance() {
        if (instance == null) {
            instance = new ParkingLotManager();
        }
        return instance;
    }

    // ───────────────────────────── Fields ───────────────────────────────────

    /**
     * Maps slot IDs to ParkingSlot objects for O(1) lookup.
     * OOP2 Concept: {@code HashMap<String, ParkingSlot>} from Java Collections Framework.
     */
    private final Map<String, ParkingSlot> slots;

    /**
     * Active tickets keyed by ticket ID for fast exit lookup.
     * OOP2 Concept: {@code HashMap<String, Ticket<?>>} with wildcard generics.
     */
    private final Map<String, Ticket<?>> activeTickets;

    /**
     * Queue of vehicles waiting for a slot (FIFO order).
     * OOP2 Concept: {@code Queue<Vehicle>} — FIFO data structure from Collections.
     */
    private final Queue<Vehicle> waitingQueue;

    /**
     * Queue of WaitingCustomer observers for notifications (parallel to waitingQueue).
     * OOP2 Concept: Observer Pattern — stores Notifiable observers.
     */
    private final Queue<WaitingCustomer> waitingCustomers;

    /**
     * Full log of all tickets (both active and closed) for the day.
     * OOP2 Concept: {@code ArrayList<Ticket<?>>} — dynamic list from Collections.
     */
    private final List<Ticket<?>> dailyTicketLog;

    /** Accumulated revenue from all closed tickets today. */
    private double dailyRevenue;

    /** File path where session data is persisted. */
    private static final String DATA_FILE = "data" + File.separator + "parking_log.dat";

    /** File path where ticket ID counter is persisted. */
    private static final String COUNTER_FILE = "data" + File.separator + "ticket_counter.dat";

    // ───────────────────────────── Constructor ───────────────────────────────

    /**
     * Private constructor — prevents external instantiation.
     * Initializes all collections and sets up default parking slots.
     * OOP2 Concept: <b>Singleton</b> — constructor is private.
     */
    private ParkingLotManager() {
        slots           = new LinkedHashMap<>();
        activeTickets   = new LinkedHashMap<>();
        waitingQueue    = new LinkedList<>();
        waitingCustomers= new LinkedList<>();
        dailyTicketLog  = new ArrayList<>();
        dailyRevenue    = 0.0;

        initializeSlots();
    }

    /**
     * Initializes the default set of parking slots:
     * 5 Car slots (C-01 to C-05), 5 Bike slots (B-01 to B-05),
     * and 3 Truck slots (T-01 to T-03).
     */
    private void initializeSlots() {
        // Car slots
        for (int i = 1; i <= 5; i++) {
            String id = String.format("C-%02d", i);
            slots.put(id, new ParkingSlot(id, "Car"));
        }
        // Bike slots
        for (int i = 1; i <= 5; i++) {
            String id = String.format("B-%02d", i);
            slots.put(id, new ParkingSlot(id, "Bike"));
        }
        // Truck slots
        for (int i = 1; i <= 3; i++) {
            String id = String.format("T-%02d", i);
            slots.put(id, new ParkingSlot(id, "Truck"));
        }
    }

    // ───────────────────────────── Vehicle Entry ────────────────────────────

    /**
     * Processes a vehicle's entry into the parking lot.
     *
     * <p>Steps:</p>
     * <ol>
     *   <li>Check for duplicate plate number (throws {@link VehicleAlreadyParkedException})</li>
     *   <li>Find the nearest available slot for the vehicle type</li>
     *   <li>If no slot is available, throw {@link SlotFullException} — caller adds
     *       vehicle to waiting queue via {@link #addToWaitingQueue(Vehicle, String)}</li>
     *   <li>Park the vehicle, create a Ticket, and log it</li>
     * </ol>
     *
     * @param vehicle the {@link Vehicle} requesting entry
     * @return the created {@link Ticket} for this session
     * @throws SlotFullException                if no slot is available for this vehicle type
     * @throws VehicleAlreadyParkedException    if the plate number is already in the lot
     */
    @SuppressWarnings("unchecked")
    public <T extends Vehicle> Ticket<T> processEntry(T vehicle)
            throws SlotFullException, VehicleAlreadyParkedException {

        // ── Check duplicate plate ──────────────────────────────────────────
        if (isAlreadyParked(vehicle.getPlateNumber())) {
            throw new VehicleAlreadyParkedException(
                "Vehicle with plate '" + vehicle.getPlateNumber()
                        + "' is already parked in the lot.",
                vehicle.getPlateNumber());
        }

        // ── Also check waiting queue ───────────────────────────────────────
        for (Vehicle v : waitingQueue) {
            if (v.getPlateNumber().equalsIgnoreCase(vehicle.getPlateNumber())) {
                throw new VehicleAlreadyParkedException(
                    "Vehicle with plate '" + vehicle.getPlateNumber()
                            + "' is already in the waiting queue.",
                    vehicle.getPlateNumber());
            }
        }

        // ── Find available slot ────────────────────────────────────────────
        ParkingSlot availableSlot = findAvailableSlot(vehicle.getVehicleType());

        if (availableSlot == null) {
            throw new SlotFullException(
                "No available slot for vehicle type '" + vehicle.getVehicleType()
                        + "'. Vehicle added to waiting queue.",
                vehicle.getVehicleType());
        }

        // ── Park vehicle and issue ticket ─────────────────────────────────
        vehicle.park(availableSlot);
        Ticket<T> ticket = new Ticket<>(vehicle, availableSlot.getSlotId());
        activeTickets.put(ticket.getTicketId(), ticket);
        dailyTicketLog.add(ticket);

        System.out.println("\n  ✔ Vehicle parked successfully!");
        System.out.println("  Slot assigned : " + availableSlot.getSlotId());
        System.out.println("  Ticket ID     : " + ticket.getTicketId());

        return ticket;
    }

    /**
     * Adds a vehicle and its associated customer to the waiting queue.
     *
     * <p>OOP2 Concept: <b>Queue</b> — FIFO ordering ensures fairness.</p>
     *
     * @param vehicle      the {@link Vehicle} to add to the waiting queue
     * @param customerName the name/contact of the customer waiting
     */
    public void addToWaitingQueue(Vehicle vehicle, String customerName) {
        waitingQueue.add(vehicle);
        WaitingCustomer wc = new WaitingCustomer(vehicle, customerName);
        waitingCustomers.add(wc);
        System.out.println("  ✔ Vehicle added to waiting queue. Position: "
                + waitingQueue.size());
    }

    // ───────────────────────────── Vehicle Exit ──────────────────────────────

    /**
     * Processes a vehicle's exit from the parking lot by ticket ID.
     *
     * <p>Steps:</p>
     * <ol>
     *   <li>Look up the active ticket by ID (throws {@link InvalidTicketException} if missing)</li>
     *   <li>Close the ticket (records exit time and calculates fee)</li>
     *   <li>Unpark the vehicle and free the slot</li>
     *   <li>Add fee to daily revenue</li>
     *   <li>Check waiting queue and notify next matching vehicle using Iterator</li>
     * </ol>
     *
     * @param ticketId the ticket ID provided at exit
     * @return the closed {@link Ticket} with receipt information
     * @throws InvalidTicketException if the ticket ID does not exist or was already closed
     */
    public Ticket<?> processExit(String input) throws InvalidTicketException {
        // ── Look up ticket by ID first, then by plate number ─────────────
        String trimmed = input.trim();
        Ticket<?> ticket = activeTickets.get(trimmed);

        // If not found by ticket ID, try searching by plate number
        if (ticket == null) {
            for (Ticket<?> t : activeTickets.values()) {
                if (!t.isClosed() && t.getVehicle().getPlateNumber()
                        .equalsIgnoreCase(trimmed)) {
                    ticket = t;
                    break;
                }
            }
        }

        if (ticket == null) {
            throw new InvalidTicketException(
                "No active ticket found for '" + input
                        + "'. You can enter a Ticket ID (e.g. TKT-001) or a plate number.",
                input);
        }

        if (ticket.isClosed()) {
            throw new InvalidTicketException(
                "Ticket ID '" + ticket.getTicketId() + "' has already been closed. "
                        + "This vehicle has already exited.",
                ticket.getTicketId());
        }

        // ── Close ticket and unpark ───────────────────────────────────────
        LocalDateTime exitTime = LocalDateTime.now();
        ticket.close(exitTime);

        Vehicle vehicle = ticket.getVehicle();
        String freedSlotType = vehicle.getVehicleType();
        vehicle.unpark();

        // ── Update revenue and remove from active tickets ─────────────────
        dailyRevenue += ticket.getFee();
        activeTickets.remove(ticket.getTicketId());

        System.out.println(ticket.generateReceipt());

        // ── Notify waiting queue (Observer pattern with Iterator) ─────────
        processWaitingQueue(freedSlotType);

        return ticket;
    }

    /**
     * Scans the waiting queue for the next vehicle that matches the freed slot type,
     * assigns it a slot, and notifies all waiting customers.
     *
     * <p>OOP2 Concepts:</p>
     * <ul>
     *   <li><b>Iterator</b> — safely removes from the queue during iteration</li>
     *   <li><b>Observer Pattern</b> — notifies waiting customers via Notifiable</li>
     * </ul>
     *
     * @param freedSlotType the vehicle type of the slot that just became available
     */
    @SuppressWarnings("unchecked")
    private void processWaitingQueue(String freedSlotType) {
        // Use Iterator to safely iterate and remove from waitingQueue
        Iterator<Vehicle> vehicleIterator = waitingQueue.iterator();
        Iterator<WaitingCustomer> customerIterator = waitingCustomers.iterator();

        boolean slotAssigned = false;
        // Collect customers to notify after iteration is complete
        List<WaitingCustomer> toNotify = new ArrayList<>();

        while (vehicleIterator.hasNext() && customerIterator.hasNext()) {
            Vehicle waiting = vehicleIterator.next();
            WaitingCustomer wc = customerIterator.next();

            if (!slotAssigned && waiting.getVehicleType().equals(freedSlotType)) {
                ParkingSlot slot = findAvailableSlot(freedSlotType);
                if (slot != null) {
                    // Park the waiting vehicle
                    waiting.setEntryTime(LocalDateTime.now());
                    waiting.park(slot);
                    @SuppressWarnings("rawtypes")
                    Ticket ticket = new Ticket<>(waiting, slot.getSlotId());
                    activeTickets.put(ticket.getTicketId(), ticket);
                    dailyTicketLog.add(ticket);

                    // Notify the specific customer
                    wc.notify("A slot (" + slot.getSlotId() + ") is now available! "
                            + "Your vehicle has been auto-parked. Ticket: "
                            + ticket.getTicketId());

                    // Remove from both queues using Iterator (safe removal)
                    vehicleIterator.remove();
                    customerIterator.remove();
                    slotAssigned = true;
                }
            } else if (slotAssigned && waiting.getVehicleType().equals(freedSlotType)) {
                // Only notify same-type waiters that the slot went to someone ahead of them
                toNotify.add(wc);
            }
            // Skip notification for customers waiting for a different vehicle type
        }

        // Notify after iteration to avoid confusing mid-iteration output
        for (WaitingCustomer wc : toNotify) {
            wc.notify("Still waiting... A " + freedSlotType
                    + " slot freed up but was assigned to a vehicle ahead in the queue.");
        }
    }

    // ───────────────────────────── Slot Management ──────────────────────────

    /**
     * Finds the first available (unoccupied) slot that matches the given vehicle type.
     *
     * @param vehicleType the type of vehicle slot to find ("Car", "Bike", "Truck")
     * @return the first available {@link ParkingSlot}, or null if none is available
     */
    private ParkingSlot findAvailableSlot(String vehicleType) {
        for (ParkingSlot slot : slots.values()) {
            if (slot.getSlotType().equals(vehicleType) && !slot.isOccupied()) {
                return slot;
            }
        }
        return null;
    }

    /**
     * Checks whether a vehicle with the given plate number is currently parked.
     *
     * @param plateNumber the license plate to search for
     * @return true if the plate is found in an active (unclosed) ticket
     */
    private boolean isAlreadyParked(String plateNumber) {
        for (Ticket<?> ticket : activeTickets.values()) {
            if (!ticket.isClosed()
                    && ticket.getVehicle().getPlateNumber()
                              .equalsIgnoreCase(plateNumber)) {
                return true;
            }
        }
        return false;
    }

    // ───────────────────────────── Search & Display ─────────────────────────

    /**
     * Displays the current occupancy status of all parking slots, grouped by type.
     */
    public void displayOccupancy() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║              CURRENT PARKING OCCUPANCY               ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");

        int carTotal = 0, carOccupied = 0;
        int bikeTotal = 0, bikeOccupied = 0;
        int truckTotal = 0, truckOccupied = 0;

        // ── Car Slots ────────────────────────────────────────────────────
        System.out.println("║  ── CAR SLOTS ──────────────────────────────────────║");
        for (ParkingSlot slot : slots.values()) {
            if (slot.getSlotType().equals("Car")) {
                carTotal++;
                if (slot.isOccupied()) carOccupied++;
                System.out.printf("║   %-52s║%n", slot.toString());
            }
        }

        // ── Bike Slots ───────────────────────────────────────────────────
        System.out.println("║  ── BIKE SLOTS ─────────────────────────────────────║");
        for (ParkingSlot slot : slots.values()) {
            if (slot.getSlotType().equals("Bike")) {
                bikeTotal++;
                if (slot.isOccupied()) bikeOccupied++;
                System.out.printf("║   %-52s║%n", slot.toString());
            }
        }

        // ── Truck Slots ──────────────────────────────────────────────────
        System.out.println("║  ── TRUCK SLOTS ────────────────────────────────────║");
        for (ParkingSlot slot : slots.values()) {
            if (slot.getSlotType().equals("Truck")) {
                truckTotal++;
                if (slot.isOccupied()) truckOccupied++;
                System.out.printf("║   %-52s║%n", slot.toString());
            }
        }

        System.out.println("╠══════════════════════════════════════════════════════╣");
        String summary = String.format("Cars: %d/%d  | Bikes: %d/%d  | Trucks: %d/%d",
                carOccupied, carTotal, bikeOccupied, bikeTotal, truckOccupied, truckTotal);
        System.out.printf("║  %-52s║%n", summary);

        // ── Waiting Queue ────────────────────────────────────────────────
        if (!waitingQueue.isEmpty()) {
            System.out.println("╠══════════════════════════════════════════════════════╣");
            System.out.println("║  ── WAITING QUEUE ──────────────────────────────────║");
            int pos = 1;
            for (WaitingCustomer wc : waitingCustomers) {
                System.out.printf("║   #%d: %-49s║%n", pos++, wc.toString());
            }
        }

        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    /**
     * Displays the daily revenue summary along with a list of all closed tickets.
     */
    public void displayRevenueSummary() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║              DAILY REVENUE SUMMARY                  ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.printf("║  Total Daily Revenue : Rs. %-26.2f║%n", dailyRevenue);
        System.out.printf("║  Total Tickets Issued: %-29d║%n", dailyTicketLog.size());
        System.out.printf("║  Currently Active    : %-29d║%n", activeTickets.size());
        System.out.printf("║  Closed Tickets      : %-29d║%n",
                dailyTicketLog.size() - activeTickets.size());
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║  TICKET LOG:                                        ║");

        if (dailyTicketLog.isEmpty()) {
            System.out.println("║   No tickets issued today.                           ║");
        } else {
            for (Ticket<?> t : dailyTicketLog) {
                System.out.printf("║   %-52s║%n", t.toString());
            }
        }

        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    /**
     * Searches for a vehicle by plate number across all active tickets, the ticket log,
     * and the waiting queue.
     *
     * @param plateNumber the plate number to search for (case-insensitive)
     */
    public void searchByPlate(String plateNumber) {
        System.out.println("\n  Searching for plate: " + plateNumber.toUpperCase());
        boolean found = false;

        // ── Search active tickets ─────────────────────────────────────────
        for (Ticket<?> ticket : activeTickets.values()) {
            if (ticket.getVehicle().getPlateNumber()
                      .equalsIgnoreCase(plateNumber)) {
                System.out.println("  ► Found in ACTIVE TICKETS:");
                System.out.println("    " + ticket);
                System.out.println("    " + ticket.getVehicle());
                found = true;
            }
        }

        // ── Search daily log (closed tickets) ─────────────────────────────
        for (Ticket<?> ticket : dailyTicketLog) {
            if (ticket.isClosed() && ticket.getVehicle().getPlateNumber()
                      .equalsIgnoreCase(plateNumber)) {
                System.out.println("  ► Found in CLOSED TICKETS:");
                System.out.println("    " + ticket);
                found = true;
            }
        }

        // ── Search waiting queue ──────────────────────────────────────────
        for (Vehicle v : waitingQueue) {
            if (v.getPlateNumber().equalsIgnoreCase(plateNumber)) {
                System.out.println("  ► Found in WAITING QUEUE:");
                System.out.println("    " + v);
                found = true;
            }
        }

        if (!found) {
            System.out.println("  ✘ No record found for plate: " + plateNumber.toUpperCase());
        }
    }

    // ───────────────────────────── File I/O ─────────────────────────────────

    /**
     * Saves the current session data to disk using Java Object Serialization.
     *
     * <p>Persists:</p>
     * <ul>
     *   <li>Daily ticket log ({@code ArrayList<Ticket<?>>})</li>
     *   <li>Daily revenue (double)</li>
     *   <li>Ticket ID counter (int) — so IDs don't reset on next run</li>
     * </ul>
     *
     * <p>OOP2 Concept: <b>File I/O</b> — uses ObjectOutputStream for serialization.</p>
     */
    public void saveToFile() {
        // Ensure data directory exists
        new File("data").mkdirs();

        // ── Save ticket log and revenue ───────────────────────────────────
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(DATA_FILE))) {
            oos.writeObject(dailyTicketLog);
            oos.writeDouble(dailyRevenue);
            System.out.println("  ✔ Session data saved to '" + DATA_FILE + "'.");
        } catch (IOException e) {
            System.err.println("  ✘ Error saving session data: " + e.getMessage());
        } finally {
            // ── Save ticket counter ───────────────────────────────────────
            try (ObjectOutputStream counterOut = new ObjectOutputStream(
                    new FileOutputStream(COUNTER_FILE))) {
                counterOut.writeInt(Ticket.getIdCounter());
            } catch (IOException e) {
                System.err.println("  ✘ Error saving ticket counter: " + e.getMessage());
            }
        }
    }

    /**
     * Loads previously saved session data from disk on application startup.
     *
     * <p>Restores:</p>
     * <ul>
     *   <li>Daily ticket log</li>
     *   <li>Daily revenue</li>
     *   <li>Ticket ID counter</li>
     * </ul>
     *
     * <p>OOP2 Concept: <b>File I/O</b> — uses ObjectInputStream for deserialization.</p>
     */
    @SuppressWarnings("unchecked")
    public void loadFromFile() {
        File dataFile = new File(DATA_FILE);
        if (!dataFile.exists()) {
            System.out.println("  ℹ No previous session data found. Starting fresh.");
            return;
        }

        // ── Load ticket log and revenue ───────────────────────────────────
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(DATA_FILE))) {

            List<Ticket<?>> loadedLog = (List<Ticket<?>>) ois.readObject();
            double loadedRevenue      = ois.readDouble();

            // Restore closed tickets into the daily log
            dailyTicketLog.addAll(loadedLog);
            dailyRevenue = loadedRevenue;

            // Re-add any active tickets back to the active map
            for (Ticket<?> t : loadedLog) {
                if (!t.isClosed()) {
                    activeTickets.put(t.getTicketId(), t);
                    // Note: slot assignment must be re-established manually
                    // since ParkingSlot.currentVehicle is transient
                    ParkingSlot slot = slots.get(t.getSlotId());
                    if (slot != null) {
                        slot.occupy(t.getVehicle());
                        t.getVehicle().setAssignedSlot(slot);
                    }
                }
            }

            System.out.printf("  ✔ Loaded %d ticket(s) from previous session. "
                    + "Revenue: Rs.%.2f%n", loadedLog.size(), loadedRevenue);

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("  ✘ Error loading session data: " + e.getMessage());
            System.err.println("    Starting with a fresh session.");
        } finally {
            // ── Load ticket counter ───────────────────────────────────────
            File counterFile = new File(COUNTER_FILE);
            if (counterFile.exists()) {
                try (ObjectInputStream counterIn = new ObjectInputStream(
                        new FileInputStream(COUNTER_FILE))) {
                    Ticket.setIdCounter(counterIn.readInt());
                } catch (IOException e) {
                    System.err.println("  ✘ Error loading ticket counter: " + e.getMessage());
                }
            }
        }
    }

    // ───────────────────────────── Accessors ────────────────────────────────

    /**
     * Returns the total daily revenue collected so far.
     *
     * @return daily revenue in rupees
     */
    public double getDailyRevenue() { return dailyRevenue; }

    /**
     * Returns the map of all parking slots.
     *
     * @return unmodifiable view of all slots
     */
    public Map<String, ParkingSlot> getSlots() {
        return Collections.unmodifiableMap(slots);
    }

    /**
     * Returns the active tickets map.
     *
     * @return unmodifiable view of active tickets
     */
    public Map<String, Ticket<?>> getActiveTickets() {
        return Collections.unmodifiableMap(activeTickets);
    }

    /**
     * Returns the waiting queue (vehicles waiting for a slot).
     *
     * @return the waiting queue
     */
    public Queue<Vehicle> getWaitingQueue() {
        return waitingQueue;
    }

    /**
     * Returns the full daily ticket log.
     *
     * @return unmodifiable view of the daily log
     */
    public List<Ticket<?>> getDailyTicketLog() {
        return Collections.unmodifiableList(dailyTicketLog);
    }

    /**
     * Returns the waiting customers queue (parallel to the vehicle waiting queue).
     * Each entry corresponds to one vehicle in the waiting queue.
     *
     * @return the queue of {@link WaitingCustomer} observers
     */
    public Queue<WaitingCustomer> getWaitingCustomers() {
        return waitingCustomers;
    }

    // ────────────────────────── Waiting Queue Management ────────────────────

    /**
     * Cancels a vehicle's position in the waiting queue by plate number.
     *
     * <p>Uses an {@link Iterator} to safely locate and remove the vehicle
     * and its corresponding {@link WaitingCustomer} from both queues.</p>
     *
     * <p>OOP2 Concept: <b>Iterator</b> — used here to safely remove from
     * two parallel queues simultaneously without ConcurrentModificationException.</p>
     *
     * @param plateNumber the plate number of the vehicle to cancel (case-insensitive)
     * @return true if the vehicle was found and removed, false otherwise
     */
    public boolean cancelFromWaitingQueue(String plateNumber) {
        Iterator<Vehicle>         vehicleIter  = waitingQueue.iterator();
        Iterator<WaitingCustomer> customerIter = waitingCustomers.iterator();

        while (vehicleIter.hasNext() && customerIter.hasNext()) {
            Vehicle v        = vehicleIter.next();
            WaitingCustomer wc = customerIter.next();

            if (v.getPlateNumber().equalsIgnoreCase(plateNumber)) {
                vehicleIter.remove();
                customerIter.remove();
                System.out.println("  ✔ Vehicle '" + plateNumber.toUpperCase()
                        + "' removed from the waiting queue.");
                return true;
            }
        }
        System.out.println("  ✘ Plate '" + plateNumber.toUpperCase()
                + "' not found in the waiting queue.");
        return false;
    }

    // ────────────────────────── Dynamic Slot Management ─────────────────────

    /**
     * Adds a new parking slot to the lot at runtime.
     *
     * <p>The slot ID is auto-generated based on the slot type and current count.
     * Throws {@link IllegalArgumentException} if the type is invalid.</p>
     *
     * @param slotType the type of slot to add ("Car", "Bike", or "Truck")
     * @return the ID of the newly added slot
     * @throws IllegalArgumentException if slotType is not Car, Bike, or Truck
     */
    public String addSlot(String slotType) {
        String prefix;
        String canonical;
        switch (slotType.trim().toLowerCase()) {
            case "car", "1"   -> { prefix = "C"; canonical = "Car";   }
            case "bike", "2"  -> { prefix = "B"; canonical = "Bike";  }
            case "truck", "3" -> { prefix = "T"; canonical = "Truck"; }
            default -> throw new IllegalArgumentException(
                    "Invalid slot type: '" + slotType + "'. Use Car (1), Bike (2), or Truck (3).");
        }

        // Determine next slot number by finding the max existing ID for this type
        int maxNum = 0;
        for (String id : slots.keySet()) {
            if (id.startsWith(prefix + "-")) {
                try {
                    int num = Integer.parseInt(id.substring(prefix.length() + 1));
                    maxNum = Math.max(maxNum, num);
                } catch (NumberFormatException ignored) { }
            }
        }
        String newId = String.format("%s-%02d", prefix, maxNum + 1);

        slots.put(newId, new ParkingSlot(newId, canonical));
        System.out.println("  ✔ New slot added: " + newId + " [" + canonical + "]");
        return newId;
    }

    /**
     * Removes a parking slot from the lot by slot ID, provided it is vacant.
     *
     * <p>A slot that is currently occupied cannot be removed; the caller must
     * first process the vehicle's exit.</p>
     *
     * @param slotId the ID of the slot to remove (e.g., "C-06")
     * @return true if removed successfully, false if not found or occupied
     */
    public boolean removeSlot(String slotId) {
        ParkingSlot slot = slots.get(slotId.toUpperCase().trim());
        if (slot == null) {
            System.out.println("  ✘ Slot '" + slotId + "' does not exist.");
            return false;
        }
        if (slot.isOccupied()) {
            System.out.println("  ✘ Slot '" + slotId + "' is currently OCCUPIED. "
                    + "Process the vehicle exit first.");
            return false;
        }
        // Guard: keep at least 1 slot of each type
        String type = slot.getSlotType();
        long remaining = slots.values().stream()
                .filter(s -> s.getSlotType().equals(type) && !s.getSlotId().equals(slotId))
                .count();
        if (remaining == 0) {
            System.out.println("  ✘ Cannot remove: '" + slotId
                    + "' is the last " + type + " slot.");
            return false;
        }
        slots.remove(slotId.toUpperCase().trim());
        System.out.println("  ✔ Slot '" + slotId + "' removed successfully.");
        return true;
    }

    // ───────────────────────── Revenue Breakdown ────────────────────────────

    /**
     * Returns a revenue breakdown as a map keyed by vehicle type.
     *
     * <p>OOP2 Concept: <b>Collections</b> — builds and returns a
     * {@link HashMap} of per-type revenue totals.</p>
     *
     * @return map of vehicleType → total revenue (only for closed tickets)
     */
    public Map<String, Double> getRevenueByType() {
        Map<String, Double> breakdown = new HashMap<>();
        breakdown.put("Car",   0.0);
        breakdown.put("Bike",  0.0);
        breakdown.put("Truck", 0.0);

        for (Ticket<?> t : dailyTicketLog) {
            if (t.isClosed()) {
                String type = t.getVehicle().getVehicleType();
                breakdown.merge(type, t.getFee(), Double::sum);
            }
        }
        return breakdown;
    }
}
