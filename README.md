# Smart Parking Lot Management System (SPLMS)
### Java OOP2 Console Project

---

## 📁 Project Structure

```
SPLMS/
├── src/
│   ├── interfaces/
│   │   ├── Parkable.java          ← Interface (park/unpark contract)
│   │   └── Notifiable.java        ← Interface (Observer pattern)
│   ├── model/
│   │   ├── Vehicle.java           ← Abstract class (abstraction, inheritance)
│   │   ├── Car.java               ← Concrete subclass (polymorphism)
│   │   ├── Bike.java              ← Concrete subclass (polymorphism)
│   │   ├── Truck.java             ← Concrete subclass (polymorphism)
│   │   ├── ParkingSlot.java       ← Slot model (encapsulation)
│   │   ├── Ticket.java            ← Generic class Ticket<T extends Vehicle>
│   │   └── WaitingCustomer.java   ← Observer (implements Notifiable)
│   ├── exceptions/
│   │   ├── SlotFullException.java              ← Custom checked exception
│   │   ├── InvalidTicketException.java         ← Custom checked exception
│   │   └── VehicleAlreadyParkedException.java  ← Custom checked exception
│   ├── manager/
│   │   ├── ParkingLotManager.java ← Singleton + all collections + file I/O
│   │   └── VehicleFactory.java    ← Factory pattern
│   └── main/
│       └── Main.java              ← Entry point, menu-driven UI
├── data/
│   └── parking_log.dat            ← Auto-created on first save
└── README.md
```

---

## ⚙️ How to Compile

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- A terminal (Command Prompt / PowerShell / Terminal)

### Step 1 — Navigate to the project root
```bash
cd SPLMS
```

### Step 2 — Create the output directory
```bash
mkdir out
```

### Step 3 — Compile all Java source files
```bash
javac -d out -sourcepath src src/main/Main.java
```

This compiles all source files reachable from `Main.java` (which is all of them,
through transitive imports) and puts the `.class` files into the `out/` directory.

### Step 4 — Run the application
```bash
java -cp out main.Main
```

### One-liner (Windows PowerShell)
```powershell
mkdir out; javac -d out -sourcepath src src/main/Main.java; java -cp out main.Main
```

### One-liner (Linux/macOS bash)
```bash
mkdir -p out && javac -d out -sourcepath src src/main/Main.java && java -cp out main.Main
```

---

## 🎮 How to Use

| Menu Option | Description |
|---|---|
| **1. Vehicle Entry** | Choose type (Car/Bike/Truck), enter plate → auto-assigned slot |
| **2. Vehicle Exit** | Enter your Ticket ID → fee receipt printed, slot freed |
| **3. View Occupancy** | Shows all slots (OCCUPIED/VACANT) and waiting queue |
| **4. Daily Revenue** | Summary of all tickets and total revenue for the day |
| **5. Search by Plate** | Find a vehicle across active tickets, log, and waiting queue |
| **6. Save & Exit** | Serializes session data to `data/parking_log.dat` |

> **Session Persistence**: Data is loaded automatically on startup so history is never lost between runs.

---

## 🎓 OOP2 Concept → File Mapping (for Viva/Defence)

| OOP2 Concept | File(s) | How it's demonstrated |
|---|---|---|
| **Abstraction** | `model/Vehicle.java` | Abstract class with abstract method `calculateFee()` |
| **Inheritance** | `model/Car.java`, `Bike.java`, `Truck.java` | All extend `Vehicle` |
| **Polymorphism** | `model/Car.java`, `Bike.java`, `Truck.java` | Each overrides `calculateFee()` differently |
| **Encapsulation** | All model classes | Private fields + public getters/setters |
| **Interfaces** | `interfaces/Parkable.java`, `interfaces/Notifiable.java` | Behavioral contracts |
| **Generics** | `model/Ticket.java` | `Ticket<T extends Vehicle>` — type-safe generic class |
| **Custom Exceptions** | `exceptions/` (3 files) | Checked exceptions with proper throw/catch/finally |
| **Singleton Pattern** | `manager/ParkingLotManager.java` | Private constructor + `getInstance()` |
| **Factory Pattern** | `manager/VehicleFactory.java` | `createVehicle(type, plate)` hides construction |
| **Observer Pattern** | `model/WaitingCustomer.java`, `interfaces/Notifiable.java`, `manager/ParkingLotManager.java` | Customers notified when slot opens |
| **Java Collections** | `manager/ParkingLotManager.java` | `HashMap`, `Queue`, `ArrayList`, `Iterator` |
| **File I/O** | `manager/ParkingLotManager.java` | `ObjectOutputStream`/`ObjectInputStream` for serialization |
| **Exception Handling** | `main/Main.java`, `manager/ParkingLotManager.java` | try-catch-finally blocks with meaningful messages |

---

## 💰 Parking Fee Structure

| Vehicle | Grace Period | Rate | Min. Charge |
|---|---|---|---|
| **Bike** | 30 minutes free | Rs. 10/hr | Rs. 5 |
| **Car** | 15 minutes free | Rs. 30/hr | Rs. 10 |
| **Truck** | None | Rs. 50/hr | Rs. 50 |

---

## 🅿️ Default Slot Configuration

| Type | Slots | IDs |
|---|---|---|
| Car | 5 | C-01 to C-05 |
| Bike | 5 | B-01 to B-05 |
| Truck | 3 | T-01 to T-03 |

---

## 🔄 What Happens When the Lot is Full?

1. `processEntry()` throws `SlotFullException`
2. `Main.java` catches it in the `catch` block
3. User is prompted for their name
4. Vehicle is added to the **waiting queue** (`Queue<Vehicle>`)
5. When another vehicle exits, `processWaitingQueue()` uses an **Iterator** to safely scan and remove the next matching waiting vehicle
6. The waiting customer is **notified** via `WaitingCustomer.notify()` (Observer pattern)
7. The vehicle is auto-assigned the freed slot and a new ticket is issued

---

## 📌 Key Design Decisions

- **Singleton** `ParkingLotManager` ensures all data (slots, tickets, revenue, queue) is in one place — no global variables needed.
- **Generic** `Ticket<T>` is type-safe: a `Ticket<Car>` only holds a `Car`, preventing accidental mixing.
- **Factory** `VehicleFactory` means `Main.java` never calls `new Car()` directly — it just calls `createVehicle("car", plate)`.
- **Serializable** on all model classes means the full object graph can be written to and read from disk with `ObjectOutputStream`.
- **Iterator** is used (not `for-each`) during waiting queue processing to allow safe removal during iteration — `for-each` would throw `ConcurrentModificationException`.
