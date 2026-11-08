package main;

import exceptions.InvalidTicketException;
import exceptions.SlotFullException;
import exceptions.VehicleAlreadyParkedException;
import manager.ParkingLotManager;
import manager.ReportGenerator;
import manager.VehicleFactory;
import model.Ticket;
import model.Vehicle;
import model.WaitingCustomer;

import java.util.InputMismatchException;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

/**
 * Main is the entry point for the Smart Parking Lot Management System (SPLMS).
 *
 * <p>It provides a menu-driven console interface that allows users to:</p>
 * <ul>
 *   <li>Enter a vehicle (Car, Bike, or Truck) and receive a parking ticket</li>
 *   <li>Exit a vehicle using a ticket ID and receive a fee receipt</li>
 *   <li>View current slot occupancy and the waiting queue</li>
 *   <li>View daily revenue and full ticket log</li>
 *   <li>Search for a vehicle by plate number</li>
 *   <li>View the waiting queue in detail</li>
 *   <li>Cancel a vehicle from the waiting queue</li>
 *   <li>Export an end-of-day report to a .txt file</li>
 *   <li>Manage slots (add/remove) dynamically</li>
 *   <li>Save session data and exit</li>
 * </ul>
 *
 * <p>OOP2 Concepts demonstrated in this file:</p>
 * <ul>
 *   <li><b>Singleton Pattern</b> — accesses ParkingLotManager.getInstance()</li>
 *   <li><b>Factory Pattern</b> — uses VehicleFactory.createVehicle()</li>
 *   <li><b>Exception Handling</b> — try-catch-finally for all custom exceptions</li>
 *   <li><b>Polymorphism</b> — Vehicle reference used throughout</li>
 * </ul>
 *
 * @author SPLMS Team
 * @version 1.0
 */
public class Main {

    /** ANSI color codes for prettier console output. */
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN   = "\u001B[36m";
    private static final String RED    = "\u001B[31m";
    private static final String BLUE   = "\u001B[34m";

    /** Scanner for reading console input. */
    private static final Scanner scanner = new Scanner(System.in);

    /** The singleton manager instance. */
    private static final ParkingLotManager manager = ParkingLotManager.getInstance();

    /**
     * Application entry point.
     * Loads existing session data, displays the menu, and handles user input
     * in a loop until the user chooses to exit.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        printBanner();

        // ── Load previous session data on startup ─────────────────────────
        System.out.println(CYAN + "\n  Loading previous session data..." + RESET);
        manager.loadFromFile();

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("  Enter your choice: ");

            switch (choice) {
                case 1  -> handleVehicleEntry();
                case 2  -> handleVehicleExit();
                case 3  -> manager.displayOccupancy();
                case 4  -> manager.displayRevenueSummary();
                case 5  -> handlePlateSearch();
                case 6  -> handleViewWaitingQueue();
                case 7  -> handleCancelWaiting();
                case 8  -> handleExportReport();
                case 9  -> handleManageSlots();
                case 10 -> {
                    handleSaveAndExit();
                    running = false;
                }
                default -> System.out.println(RED + "  ✘ Invalid option. Please choose 1–10." + RESET);
            }
        }

        scanner.close();
    }

    // ─────────────────────────── Menu Handlers ───────────────────────────────

    /**
     * Handles the Vehicle Entry flow.
     *
     * <p>OOP2 Concepts:</p>
     * <ul>
     *   <li><b>Factory Pattern</b> — VehicleFactory.createVehicle() creates the right type</li>
     *   <li><b>Exception Handling</b> — catches SlotFullException,
     *       VehicleAlreadyParkedException, and IllegalArgumentException</li>
     *   <li><b>try-catch-finally</b> — finally always prints a separator</li>
     * </ul>
     */
    private static void handleVehicleEntry() {
        System.out.println(CYAN + "\n  ┌─── VEHICLE ENTRY ───────────────────────────┐" + RESET);
        System.out.println("  Supported vehicle types:");
        System.out.println(VehicleFactory.getSupportedTypes());
        System.out.println(YELLOW + "  (Type 0 to go back)" + RESET);

        String typeInput  = readString("  Enter vehicle type (Car/Bike/Truck): ");
        if (typeInput.equals("0")) { System.out.println(BLUE + "  └──────────────────────────────────────────────" + RESET); return; }
        String plateInput = readString("  Enter plate number: ");
        if (plateInput.equals("0")) { System.out.println(BLUE + "  └──────────────────────────────────────────────" + RESET); return; }

        try {
            // Factory Pattern — creates the correct Vehicle subclass
            Vehicle vehicle = VehicleFactory.createVehicle(typeInput, plateInput);

            // Singleton Singleton — delegates to the one ParkingLotManager instance
            Ticket<?> ticket = manager.processEntry(vehicle);

            System.out.println(GREEN + "  ✔ Entry successful. Ticket ID: "
                    + ticket.getTicketId() + RESET);

        } catch (SlotFullException e) {
            // Slot is full — handle by adding to waiting queue
            System.out.println(YELLOW + "\n  ⚠ " + e.getMessage() + RESET);
            System.out.println("  Vehicle type: " + e.getVehicleType());

            String customerName = readString("  Enter your name for the waiting queue: ");

            try {
                // Re-create vehicle since original may have thrown before assignment
                Vehicle waitingVehicle = VehicleFactory.createVehicle(typeInput, plateInput);
                manager.addToWaitingQueue(waitingVehicle, customerName);
                System.out.println(YELLOW
                        + "  You will be notified when a slot becomes available." + RESET);
            } catch (IllegalArgumentException ex) {
                System.out.println(RED + "  ✘ Error: " + ex.getMessage() + RESET);
            }

        } catch (VehicleAlreadyParkedException e) {
            // Duplicate plate number — reject entry
            System.out.println(RED + "\n  ✘ " + e.getMessage() + RESET);
            System.out.println("  Plate number: " + e.getPlateNumber());

        } catch (IllegalArgumentException e) {
            // Invalid vehicle type string from factory
            System.out.println(RED + "\n  ✘ " + e.getMessage() + RESET);

        } finally {
            // Always executed — separates output cleanly
            System.out.println(BLUE + "  └──────────────────────────────────────────────" + RESET);
        }
    }

    /**
     * Handles the Vehicle Exit flow.
     *
     * <p>OOP2 Concepts:</p>
     * <ul>
     *   <li><b>Exception Handling</b> — catches InvalidTicketException</li>
     *   <li><b>Polymorphism</b> — fee is calculated via overridden calculateFee()</li>
     *   <li><b>try-catch-finally</b> — finally always prints separator</li>
     * </ul>
     */
    private static void handleVehicleExit() {
        System.out.println(CYAN + "\n  ┌─── VEHICLE EXIT ────────────────────────────┐" + RESET);

        // Show active tickets for reference
        if (manager.getActiveTickets().isEmpty()) {
            System.out.println("  No active tickets at this time.");
            System.out.println(BLUE + "  └──────────────────────────────────────────────" + RESET);
            return;
        }

        System.out.println("  Active tickets:");
        for (Ticket<?> t : manager.getActiveTickets().values()) {
            System.out.println("    → " + t);
        }

        String ticketId = readString("\n  Enter Ticket ID or Plate Number (0 to go back): ");
        if (ticketId.equals("0")) { System.out.println(BLUE + "  └──────────────────────────────────────────────" + RESET); return; }

        try {
            // processExit handles: lookup, fee calculation, slot freeing, queue notification
            manager.processExit(ticketId);
            System.out.println(GREEN + "  ✔ Exit processed successfully." + RESET);

        } catch (InvalidTicketException e) {
            System.out.println(RED + "\n  ✘ " + e.getMessage() + RESET);
            System.out.println("  Ticket ID provided: " + e.getTicketId());

        } finally {
            System.out.println(BLUE + "  └──────────────────────────────────────────────" + RESET);
        }
    }

    /**
     * Handles searching for a vehicle by plate number.
     */
    private static void handlePlateSearch() {
        System.out.println(CYAN + "\n  ┌─── SEARCH BY PLATE ─────────────────────────┐" + RESET);
        String plate = readString("  Enter plate number to search (0 to go back): ");
        if (plate.equals("0")) { System.out.println(BLUE + "  └──────────────────────────────────────────────" + RESET); return; }

        try {
            manager.searchByPlate(plate);
        } finally {
            System.out.println(BLUE + "  └──────────────────────────────────────────────" + RESET);
        }
    }

    /**
     * Displays a detailed, formatted view of the current waiting queue.
     *
     * <p>OOP2 Concept: <b>Collections</b> — iterates over a {@link Queue} of
     * {@link WaitingCustomer} objects.</p>
     */
    private static void handleViewWaitingQueue() {
        System.out.println(CYAN + "\n  ┌─── WAITING QUEUE ───────────────────────────┐" + RESET);
        Queue<WaitingCustomer> queue = manager.getWaitingCustomers();

        try {
            if (queue.isEmpty()) {
                System.out.println("  No vehicles currently in the waiting queue.");
            } else {
                System.out.printf("  %-4s %-16s %-7s %-18s%n",
                        "Pos", "Plate", "Type", "Customer Name");
                System.out.println("  " + "─".repeat(50));
                int pos = 1;
                for (WaitingCustomer wc : queue) {
                    System.out.printf("  #%-3d %-16s %-7s %-18s%n",
                            pos++,
                            wc.getVehicle().getPlateNumber(),
                            wc.getVehicle().getVehicleType(),
                            wc.getCustomerName());
                }
                System.out.println("  " + "─".repeat(50));
                System.out.println("  Total waiting: " + queue.size());
            }
        } finally {
            System.out.println(BLUE + "  └──────────────────────────────────────────────" + RESET);
        }
    }

    /**
     * Handles cancelling a vehicle's entry in the waiting queue.
     *
     * <p>OOP2 Concept: <b>Iterator</b> — manager uses Iterator internally
     * to safely remove from both parallel queues simultaneously.</p>
     */
    private static void handleCancelWaiting() {
        System.out.println(CYAN + "\n  ┌─── CANCEL FROM WAITING QUEUE ───────────────┐" + RESET);

        Queue<WaitingCustomer> queue = manager.getWaitingCustomers();
        try {
            if (queue.isEmpty()) {
                System.out.println("  Waiting queue is empty — nothing to cancel.");
            } else {
                System.out.println("  Currently waiting:");
                for (WaitingCustomer wc : queue) {
                    System.out.println("    → " + wc);
                }
                String plate = readString("\n  Enter plate number to cancel (0 to go back): ");
                if (!plate.equals("0")) {
                    manager.cancelFromWaitingQueue(plate);
                }
            }
        } finally {
            System.out.println(BLUE + "  └──────────────────────────────────────────────" + RESET);
        }
    }

    /**
     * Exports a full end-of-day report to a plain-text file using
     * {@link ReportGenerator}.
     *
     * <p>OOP2 Concept: <b>File I/O</b> — ReportGenerator writes the
     * report using {@link java.io.BufferedWriter}.</p>
     */
    private static void handleExportReport() {
        System.out.println(CYAN + "\n  ┌─── EXPORT REPORT ───────────────────────────┐" + RESET);

        try {
            // Show a quick revenue breakdown before exporting
            Map<String, Double> breakdown = manager.getRevenueByType();
            System.out.println("  Revenue breakdown (closed tickets only):");
            breakdown.forEach((type, revenue) ->
                System.out.printf("    %-7s : Rs. %.2f%n", type, revenue));
            System.out.printf("    %-7s : Rs. %.2f%n", "TOTAL", manager.getDailyRevenue());
            System.out.println();

            String path = ReportGenerator.generateReport();
            if (path != null) {
                System.out.println(GREEN + "  ✔ Report exported successfully!" + RESET);
                System.out.println("  File: " + path);
            } else {
                System.out.println(RED + "  ✘ Report export failed. Check console for errors." + RESET);
            }
        } finally {
            System.out.println(BLUE + "  └──────────────────────────────────────────────" + RESET);
        }
    }

    /**
     * Handles dynamic slot management — allows the admin to add or remove slots
     * at runtime.
     *
     * <p>OOP2 Concept: <b>Encapsulation</b> — slot state is fully managed by
     * {@link ParkingLotManager}; this method only provides the UI shell.</p>
     */
    private static void handleManageSlots() {
        System.out.println(CYAN + "\n  ┌─── MANAGE SLOTS ────────────────────────────┐" + RESET);
        System.out.println("  │  A. Add a new slot                            │");
        System.out.println("  │  R. Remove an existing vacant slot            │");
        System.out.println("  │  X. Back to main menu                         │");
        System.out.println(CYAN + "  └───────────────────────────────────────────────┘" + RESET);

        String action = readString("  Enter choice (A/R/X): ").toUpperCase();

        try {
            switch (action) {
                case "A" -> {
                    String type = readString("  Enter slot type to add (Car/Bike/Truck): ");
                    try {
                        String newId = manager.addSlot(type);
                        System.out.println(GREEN + "  ✔ Slot " + newId + " is now available." + RESET);
                    } catch (IllegalArgumentException e) {
                        System.out.println(RED + "  ✘ " + e.getMessage() + RESET);
                    }
                }
                case "R" -> {
                    // Show only vacant slots
                    System.out.println("  Vacant slots available for removal:");
                    boolean any = false;
                    for (model.ParkingSlot s : manager.getSlots().values()) {
                        if (!s.isOccupied()) {
                            System.out.println("    → " + s.getSlotId() + " [" + s.getSlotType() + "]");
                            any = true;
                        }
                    }
                    if (!any) {
                        System.out.println("  No vacant slots to remove.");
                    } else {
                        String slotId = readString("  Enter Slot ID to remove (e.g. C-05): ");
                        manager.removeSlot(slotId);
                    }
                }
                case "X" -> System.out.println("  Returning to main menu.");
                default  -> System.out.println(RED + "  ✘ Unknown action. Use A, R, or X." + RESET);
            }
        } finally {
            System.out.println(BLUE + "  └──────────────────────────────────────────────" + RESET);
        }
    }

    /**
     * Saves all session data to disk and exits the application.
     *
     * <p>OOP2 Concept: <b>File I/O</b> — delegates to ParkingLotManager.saveToFile()
     * which uses ObjectOutputStream serialization.</p>
     */
    private static void handleSaveAndExit() {
        System.out.println(CYAN + "\n  ┌─── SAVE & EXIT ─────────────────────────────┐" + RESET);

        try {
            manager.saveToFile();
            System.out.println(GREEN + "  ✔ Data saved successfully." + RESET);
        } finally {
            System.out.println(BLUE + "  └──────────────────────────────────────────────" + RESET);
            System.out.println(BOLD + GREEN + "\n  Thank you for using SPLMS. Goodbye!\n" + RESET);
        }
    }

    // ─────────────────────────── UI Helpers ──────────────────────────────────

    /**
     * Prints the main application banner.
     */
    private static void printBanner() {
        System.out.println(BOLD + BLUE);
        System.out.println("  ╔══════════════════════════════════════════════════╗");
        System.out.println("  ║   SMART PARKING LOT MANAGEMENT SYSTEM (SPLMS)   ║");
        System.out.println("  ║         OOP2 Project — Java Edition              ║");
        System.out.println("  ╚══════════════════════════════════════════════════╝");
        System.out.println(RESET);
    }

    /**
     * Prints the main navigation menu.
     */
    private static void printMenu() {
        System.out.println(BOLD + CYAN + "\n  ┌─── MAIN MENU ───────────────────────────────┐" + RESET);
        System.out.println("  │  1.  Vehicle Entry                            │");
        System.out.println("  │  2.  Vehicle Exit (enter ticket ID)           │");
        System.out.println("  │  3.  View Parking Occupancy                   │");
        System.out.println("  │  4.  View Daily Revenue Summary               │");
        System.out.println("  │  5.  Search Vehicle by Plate Number           │");
        System.out.println("  │  6.  View Waiting Queue                       │");
        System.out.println("  │  7.  Cancel Vehicle from Waiting Queue        │");
        System.out.println("  │  8.  Export End-of-Day Report (.txt)          │");
        System.out.println("  │  9.  Manage Slots (Add / Remove)              │");
        System.out.println("  │  10. Save & Exit                              │");
        System.out.println(BOLD + CYAN + "  └───────────────────────────────────────────────┘" + RESET);
    }

    /**
     * Reads an integer from the console, re-prompting on invalid input.
     *
     * @param prompt the prompt message to display
     * @return the integer entered by the user
     */
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int val = scanner.nextInt();
                scanner.nextLine(); // consume newline
                return val;
            } catch (InputMismatchException e) {
                scanner.nextLine(); // clear invalid input
                System.out.println(RED + "  ✘ Please enter a valid number." + RESET);
            }
        }
    }

    /**
     * Reads a non-empty string from the console.
     *
     * @param prompt the prompt message to display
     * @return the trimmed string entered by the user (guaranteed non-empty)
     */
    private static String readString(String prompt) {
        String input = "";
        while (input.isEmpty()) {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println(RED + "  ✘ Input cannot be empty." + RESET);
            }
        }
        return input;
    }
}
