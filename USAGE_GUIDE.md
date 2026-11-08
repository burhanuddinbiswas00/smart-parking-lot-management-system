# 📖 SPLMS — Usage Guide (Startup & Sample Inputs)

> A complete walkthrough of how to compile, run, and interact with every feature
> of the **Smart Parking Lot Management System**.

---

## 🚀 Starting the Server

### Prerequisites

- **JDK 17** or higher installed and added to `PATH`
- A terminal (Command Prompt / PowerShell / Terminal)

### Option A — Using the batch script (Windows)

```cmd
run.bat
```

### Option B — Manual compile & run

```bash
# Step 1: Compile all source files
javac -d out -sourcepath src src\main\Main.java

# Step 2: Run the application
java -cp out main.Main
```

### Option C — One-liner (PowerShell)

```powershell
mkdir out -ErrorAction SilentlyContinue; javac -d out -sourcepath src src\main\Main.java; java -cp out main.Main
```

### Startup Output

```
  ╔══════════════════════════════════════════════════╗
  ║   SMART PARKING LOT MANAGEMENT SYSTEM (SPLMS)   ║
  ║         OOP2 Project — Java Edition              ║
  ╚══════════════════════════════════════════════════╝

  Loading previous session data...
  ✔ Loaded 1 ticket(s) from previous session. Revenue: Rs.0.00
```

After loading, the **Main Menu** appears and waits for your input.

---

## 🎮 Main Menu

```
  ┌─── MAIN MENU ───────────────────────────────┐
  │  1.  Vehicle Entry                            │
  │  2.  Vehicle Exit (enter ticket ID)           │
  │  3.  View Parking Occupancy                   │
  │  4.  View Daily Revenue Summary               │
  │  5.  Search Vehicle by Plate Number           │
  │  6.  View Waiting Queue                       │
  │  7.  Cancel Vehicle from Waiting Queue        │
  │  8.  Export End-of-Day Report (.txt)          │
  │  9.  Manage Slots (Add / Remove)              │
  │  10. Save & Exit                              │
  └───────────────────────────────────────────────┘
  Enter your choice:
```

Type a number from **1** to **10** and press **Enter**.

---

## 📝 Sample Inputs for Each Menu Option

---

### 1️⃣ Vehicle Entry (Option `1`)

Registers a new vehicle and assigns it a parking slot.

| Prompt | Sample Input | Notes |
| --- | --- | --- |
| `Enter your choice:` | `1` | Selects Vehicle Entry |
| `Enter vehicle type (Car/Bike/Truck):` | `Car` | Case-insensitive — `car`, `CAR`, `Car` all work |
| `Enter plate number:` | `ABC-1234` | Any non-empty string |

**✅ Success output:**

```
  ✔ Entry successful. Ticket ID: TKT-001
```

**⚠️ If lot is full** (SlotFullException):

```
  ⚠ No vacant Car slots available.
  Vehicle type: Car
  Enter your name for the waiting queue: Ali
  You will be notified when a slot becomes available.
```

**❌ If vehicle already parked** (duplicate plate):

```
  ✘ Vehicle with plate 'ABC-1234' is already parked.
  Plate number: ABC-1234
```

**❌ If invalid vehicle type:**

```
  ✘ Unknown vehicle type: 'Van'. Valid types: Car (1), Bike (2), Truck (3).
```

> **💡 Tip:** You can type either the **word** (`Car`, `Bike`, `Truck`) or the **number** (`1`, `2`, `3`) for the vehicle type. Both formats are accepted and case-insensitive.

#### More entry examples

| Vehicle Type Input | Plate Input | Expected Result |
| --- | --- | --- |
| `Car` | `ABC-1234` | ✔ Assigned a Car slot (C-xx) |
| `1` | `ABC-1234` | ✔ Same as typing `Car` |
| `Bike` | `KHI-5678` | ✔ Assigned a Bike slot (B-xx) |
| `2` | `KHI-5678` | ✔ Same as typing `Bike` |
| `truck` | `LHR-9999` | ✔ Assigned a Truck slot (T-xx) |
| `3` | `LHR-9999` | ✔ Same as typing `Truck` |
| `Van` | `XYZ-0000` | ✘ Error: unknown type |

---

### 2️⃣ Vehicle Exit (Option `2`)

Exits a parked vehicle using its ticket ID. Calculates and displays the parking fee.

| Prompt | Sample Input | Notes |
| --- | --- | --- |
| `Enter your choice:` | `2` | Selects Vehicle Exit |
| `Enter Ticket ID (e.g. TKT-001):` | `TKT-001` | Must match an active ticket |

**✅ Success output:**

```
  Active tickets:
    → TKT-001 | Car [ABC-1234] @ Slot C-01 | ...

  Enter Ticket ID (e.g. TKT-001): TKT-001
  ✔ Exit processed successfully.
```

**❌ If ticket ID is invalid:**

```
  ✘ Ticket 'TKT-999' not found or already closed.
  Ticket ID provided: TKT-999
```

**❌ If no active tickets:**

```
  No active tickets at this time.
```

---

### 3️⃣ View Parking Occupancy (Option `3`)

Displays all slots — occupied and vacant. **No additional input required.**

| Prompt | Sample Input |
|---|---|
| `Enter your choice:` | `3` |

**Sample output:**

```
  Slot C-01 [Car]   → OCCUPIED | Plate: ABC-1234
  Slot C-02 [Car]   → VACANT
  Slot B-01 [Bike]  → VACANT
  Slot T-01 [Truck] → VACANT
  ...
```

---

### 4️⃣ View Daily Revenue Summary (Option `4`)

Displays all tickets (active + closed) and total revenue earned. **No additional input required.**

| Prompt | Sample Input |
|---|---|
| `Enter your choice:` | `4` |

**Sample output:**

```
  Daily Revenue Summary
  ─────────────────────
  TKT-001 | Car [ABC-1234] | Fee: Rs. 30.00 | CLOSED
  TKT-002 | Bike [KHI-5678] | Fee: Rs. 0.00 | ACTIVE
  ─────────────────────
  Total Revenue: Rs. 30.00
```

---

### 5️⃣ Search Vehicle by Plate Number (Option `5`)

Searches across active tickets, ticket log, and waiting queue.

| Prompt | Sample Input | Notes |
| --- | --- | --- |
| `Enter your choice:` | `5` | Selects Search |
| `Enter plate number to search:` | `ABC-1234` | Case-insensitive search |

**Sample output (found):**

```
  🔍 Found in active tickets:
    → TKT-001 | Car [ABC-1234] @ Slot C-01
```

**Sample output (not found):**

```
  ✘ Vehicle with plate 'XYZ-0000' not found anywhere.
```

---

### 6️⃣ View Waiting Queue (Option `6`)

Lists all vehicles waiting for a slot. **No additional input required.**

| Prompt | Sample Input |
|---|---|
| `Enter your choice:` | `6` |

**Sample output (queue has entries):**

```
  Pos  Plate            Type    Customer Name
  ──────────────────────────────────────────────────
  #1   ABC-5555         Car     Ali
  #2   KHI-6666         Bike    Sara
  ──────────────────────────────────────────────────
  Total waiting: 2
```

**Sample output (empty queue):**

```
  No vehicles currently in the waiting queue.
```

---

### 7️⃣ Cancel Vehicle from Waiting Queue (Option `7`)

Removes a vehicle from the waiting queue by plate number.

| Prompt | Sample Input | Notes |
| --- | --- | --- |
| `Enter your choice:` | `7` | Selects Cancel |
| `Enter plate number to cancel:` | `ABC-5555` | Must match a waiting vehicle |

**Sample output:**

```
  Currently waiting:
    → [Car] ABC-5555 (Customer: Ali)
    → [Bike] KHI-6666 (Customer: Sara)

  Enter plate number to cancel: ABC-5555
  ✔ Vehicle ABC-5555 removed from waiting queue.
```

**If queue is empty:**

```
  Waiting queue is empty — nothing to cancel.
```

---

### 8️⃣ Export End-of-Day Report (Option `8`)

Generates a `.txt` report file in the `data/` directory. **No additional input required.**

| Prompt | Sample Input |
|---|---|
| `Enter your choice:` | `8` |

**Sample output:**

```
  Revenue breakdown (closed tickets only):
    Car     : Rs. 60.00
    Bike    : Rs. 10.00
    Truck   : Rs. 0.00
    TOTAL   : Rs. 70.00

  ✔ Report exported successfully!
  File: data\SPLMS_Report_2026-09-02.txt
```

---

### 9️⃣ Manage Slots — Add / Remove (Option `9`)

Dynamically add or remove parking slots at runtime.

| Prompt | Sample Input | Notes |
| --- | --- | --- |
| `Enter your choice:` | `9` | Selects Manage Slots |
| `Enter choice (A/R/X):` | `A` | **A** = Add, **R** = Remove, **X** = Back |

#### 9A — Add a Slot

| Prompt | Sample Input |
| --- | --- |
| `Enter choice (A/R/X):` | `A` |
| `Enter slot type to add (Car/Bike/Truck):` | `Car` |

```
  ✔ Slot C-06 is now available.
```

#### 9R — Remove a Slot

| Prompt | Sample Input |
| --- | --- |
| `Enter choice (A/R/X):` | `R` |
| `Enter Slot ID to remove (e.g. C-05):` | `C-06` |

```
  Vacant slots available for removal:
    → C-06 [Car]
    → B-05 [Bike]

  Enter Slot ID to remove (e.g. C-05): C-06
  ✔ Slot C-06 removed.
```

#### 9X — Return to Main Menu

| Prompt | Sample Input |
|---|---|
| `Enter choice (A/R/X):` | `X` |

```
  Returning to main menu.
```

---

### 🔟 Save & Exit (Option `10`)

Saves all session data to `data\parking_log.dat` and exits the application. **No additional input required.**

| Prompt | Sample Input |
|---|---|
| `Enter your choice:` | `10` |

**Sample output:**

```
  ✔ Session data saved to 'data\parking_log.dat'.
  ✔ Data saved successfully.

  Thank you for using SPLMS. Goodbye!
```

---

## 💰 Fee Reference

| Vehicle | Grace Period | Rate | Min. Charge |
| --- | --- | --- | --- |
| **Bike** | 30 min free | Rs. 10/hr | Rs. 5 |
| **Car** | 15 min free | Rs. 30/hr | Rs. 10 |
| **Truck** | None | Rs. 50/hr | Rs. 50 |

---

## 🅿️ Default Slot Layout

| Type | Slots | IDs |
| --- | --- | --- |
| Car | 5 | C-01 → C-05 |
| Bike | 5 | B-01 → B-05 |
| Truck | 3 | T-01 → T-03 |

---

## 🔁 Full Sample Session (Copy-Paste Ready)

Below is a complete sample session you can follow step-by-step:

```
Enter your choice: 1
Enter vehicle type (Car/Bike/Truck): Car
Enter plate number: ABC-1234
→ ✔ Entry successful. Ticket ID: TKT-001

Enter your choice: 1
Enter vehicle type (Car/Bike/Truck): Bike
Enter plate number: KHI-5678
→ ✔ Entry successful. Ticket ID: TKT-002

Enter your choice: 3
→ (displays all slot occupancy)

Enter your choice: 5
Enter plate number to search: ABC-1234
→ 🔍 Found in active tickets

Enter your choice: 2
Enter Ticket ID (e.g. TKT-001): TKT-001
→ ✔ Exit processed successfully. Fee: Rs. XX.XX

Enter your choice: 4
→ (displays revenue summary)

Enter your choice: 9
Enter choice (A/R/X): A
Enter slot type to add (Car/Bike/Truck): Truck
→ ✔ Slot T-04 is now available.

Enter your choice: 8
→ ✔ Report exported successfully!

Enter your choice: 10
→ ✔ Data saved. Goodbye!
```
