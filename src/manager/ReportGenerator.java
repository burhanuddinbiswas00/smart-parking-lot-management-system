package manager;

import model.Ticket;
import model.Vehicle;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * ReportGenerator is a utility class that produces human-readable end-of-day
 * reports and exports them to a {@code .txt} file.
 *
 * <p>It pulls data from the {@link ParkingLotManager} singleton and formats
 * a full daily summary covering revenue, per-vehicle-type breakdowns, and a
 * complete ticket listing.</p>
 *
 * <p>OOP2 Concepts demonstrated:</p>
 * <ul>
 *   <li><b>File I/O</b> — writes plain-text report using {@link BufferedWriter} /
 *       {@link FileWriter}</li>
 *   <li><b>Generics</b> — iterates over {@code List<Ticket<?>>} (wildcard generics)</li>
 *   <li><b>Singleton access</b> — reads data via ParkingLotManager.getInstance()</li>
 *   <li><b>Encapsulation</b> — private constructor prevents direct instantiation</li>
 * </ul>
 *
 * @author SPLMS Team
 * @version 1.0
 */
public class ReportGenerator {

    /** Directory where reports are saved. */
    private static final String REPORT_DIR = "data" + File.separator + "reports";

    /** Date-time formatter for readable timestamps in reports. */
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Date-only formatter used in the report filename. */
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Private constructor — ReportGenerator is a utility class; no instances needed.
     */
    private ReportGenerator() {
        throw new UnsupportedOperationException("ReportGenerator is a utility class.");
    }

    /**
     * Generates a full end-of-day text report and writes it to a {@code .txt} file
     * inside the {@code data/reports/} directory.
     *
     * <p>The report covers:</p>
     * <ul>
     *   <li>Report header with date/time and lot configuration summary</li>
     *   <li>Revenue summary (total, per type breakdown)</li>
     *   <li>Full ticket log (ticket ID, plate, type, slot, duration, fee, status)</li>
     *   <li>Remaining waiting queue snapshot</li>
     * </ul>
     *
     * <p>OOP2 Concept: <b>File I/O</b> — uses try-with-resources on
     * {@link BufferedWriter} for safe, automatic resource closure.</p>
     *
     * @return the absolute path of the generated report file, or null if writing failed
     */
    public static String generateReport() {
        // Ensure report directory exists
        File dir = new File(REPORT_DIR);
        dir.mkdirs();

        // Build filename with today's date
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String filename  = REPORT_DIR + File.separator + "SPLMS_Report_" + timestamp + ".txt";

        ParkingLotManager mgr = ParkingLotManager.getInstance();

        // ── Collect statistics ─────────────────────────────────────────────
        List<Ticket<?>> log       = mgr.getDailyTicketLog();
        Map<String, model.ParkingSlot> slots = mgr.getSlots();

        double carRevenue   = 0, bikeRevenue = 0, truckRevenue = 0;
        int    carTickets   = 0, bikeTickets = 0, truckTickets = 0;

        for (Ticket<?> t : log) {
            if (t.isClosed()) {
                switch (t.getVehicle().getVehicleType()) {
                    case "Car"   -> { carRevenue   += t.getFee(); carTickets++;   }
                    case "Bike"  -> { bikeRevenue  += t.getFee(); bikeTickets++;  }
                    case "Truck" -> { truckRevenue += t.getFee(); truckTickets++; }
                }
            }
        }

        int totalSlots    = slots.size();
        long occupied     = slots.values().stream().filter(model.ParkingSlot::isOccupied).count();
        int waitingCount  = mgr.getWaitingQueue().size();

        // ── Write report file ──────────────────────────────────────────────
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {

            writeLine(writer, "================================================================");
            writeLine(writer, "       SMART PARKING LOT MANAGEMENT SYSTEM (SPLMS)");
            writeLine(writer, "                  END-OF-DAY REPORT");
            writeLine(writer, "================================================================");
            writeLine(writer, "  Generated   : " + LocalDateTime.now().format(FMT));
            writeLine(writer, "  Report Date : " + LocalDateTime.now().format(DATE_FMT));
            writeLine(writer, "================================================================");
            writeLine(writer, "");

            // ── LOT CONFIGURATION ─────────────────────────────────────────
            writeLine(writer, "  LOT CONFIGURATION");
            writeLine(writer, "  -----------------");
            writeLine(writer, "  Total Slots     : " + totalSlots);
            writeLine(writer, "  Occupied        : " + occupied);
            writeLine(writer, "  Vacant          : " + (totalSlots - occupied));
            writeLine(writer, "  Waiting Queue   : " + waitingCount + " vehicle(s)");
            writeLine(writer, "");

            // ── REVENUE SUMMARY ───────────────────────────────────────────
            writeLine(writer, "  REVENUE SUMMARY");
            writeLine(writer, "  ---------------");
            writeLine(writer, String.format("  %-22s: Rs. %10.2f  (%d tickets)",
                    "Car Revenue",   carRevenue,   carTickets));
            writeLine(writer, String.format("  %-22s: Rs. %10.2f  (%d tickets)",
                    "Bike Revenue",  bikeRevenue,  bikeTickets));
            writeLine(writer, String.format("  %-22s: Rs. %10.2f  (%d tickets)",
                    "Truck Revenue", truckRevenue, truckTickets));
            writeLine(writer, "  " + "-".repeat(50));
            writeLine(writer, String.format("  %-22s: Rs. %10.2f  (%d tickets)",
                    "TOTAL REVENUE", mgr.getDailyRevenue(), log.size()));
            writeLine(writer, "");

            // ── TICKET LOG ────────────────────────────────────────────────
            writeLine(writer, "  FULL TICKET LOG");
            writeLine(writer, "  ---------------");
            writeLine(writer, String.format("  %-10s %-14s %-7s %-7s %-22s %-22s %-10s %-10s",
                    "Ticket ID", "Plate", "Type", "Slot",
                    "Entry Time", "Exit Time", "Duration", "Fee/Status"));
            writeLine(writer, "  " + "-".repeat(110));

            if (log.isEmpty()) {
                writeLine(writer, "  No tickets issued today.");
            } else {
                for (Ticket<?> t : log) {
                    Vehicle v = t.getVehicle();
                    String entryStr = t.getEntryTime() != null ? t.getEntryTime().format(FMT) : "N/A";
                    String exitStr  = t.getExitTime()  != null ? t.getExitTime().format(FMT)  : "Active";
                    String durStr   = "Active";
                    String feeStr   = "Active";

                    if (t.isClosed() && t.getExitTime() != null) {
                        long mins = java.time.Duration.between(t.getEntryTime(), t.getExitTime()).toMinutes();
                        durStr = mins / 60 + "h " + mins % 60 + "m";
                        feeStr = String.format("Rs. %.2f", t.getFee());
                    }

                    writeLine(writer, String.format("  %-10s %-14s %-7s %-7s %-22s %-22s %-10s %-10s",
                            t.getTicketId(),
                            v.getPlateNumber(),
                            v.getVehicleType(),
                            t.getSlotId(),
                            entryStr,
                            exitStr,
                            durStr,
                            feeStr));
                }
            }

            writeLine(writer, "");

            // ── SLOT STATUS SNAPSHOT ──────────────────────────────────────
            writeLine(writer, "  SLOT STATUS SNAPSHOT");
            writeLine(writer, "  --------------------");
            for (model.ParkingSlot slot : slots.values()) {
                writeLine(writer, "  " + slot.toString());
            }

            writeLine(writer, "");
            writeLine(writer, "================================================================");
            writeLine(writer, "                        END OF REPORT");
            writeLine(writer, "================================================================");

        } catch (IOException e) {
            System.err.println("  ✘ Failed to write report: " + e.getMessage());
            return null;
        }

        return new File(filename).getAbsolutePath();
    }

    /**
     * Writes a single line followed by a system newline to the given writer.
     *
     * @param writer the {@link BufferedWriter} to write to
     * @param line   the text line to write
     * @throws IOException if an I/O error occurs
     */
    private static void writeLine(BufferedWriter writer, String line) throws IOException {
        writer.write(line);
        writer.newLine();
    }
}
