import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class PhoneStoreApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static User loggedInUser = null;

    public static void main(String[] args) {
        initializeFiles();
        seedDefaultUsers();

        System.out.println("==========================================");
        System.out.println("   WELCOME TO PokePhone Store     ");
        System.out.println("==========================================");

        while (true) {
            if (loggedInUser == null) {
                showLoginMenu();
            } else {
                showDashboard();
            }
        }
    }

    private static void initializeFiles() {
        try {
            new File("users.txt").createNewFile();
            new File("phones.txt").createNewFile();
            new File("sales.txt").createNewFile();
            new File("discounts.txt").createNewFile();   // NEW
        } catch (IOException e) {
            System.out.println("Error initializing storage files: " + e.getMessage());
        }
    }

    private static void seedDefaultUsers() {
        List<User> users = FileHandler.readUsers();
        if (users.isEmpty()) {
            FileHandler.writeUser(new User("admin", "admin123", "ADMIN"));
            FileHandler.writeUser(new User("manager", "mgr123", "MANAGER"));
            FileHandler.writeUser(new User("staff", "staff123", "STAFF"));
            FileHandler.writeUser(new User("customer", "cust123", "CUSTOMER"));
        }
    }

    private static void showLoginMenu() {
        System.out.println("\n--- LOGIN MENU ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        User user = FileHandler.authenticate(username, password);
        if (user != null) {
            loggedInUser = user;
            System.out.println("\nLogin successful! Welcome, " + user.getUsername() + " (" + user.getRole() + ")");
        } else {
            System.out.println("\nInvalid username or password. Please try again.");
        }
    }

    private static void logout() {
        System.out.println("\nLogging out... Goodbye, " + loggedInUser.getUsername());
        loggedInUser = null;
    }

    private static void showDashboard() {
        System.out.println("\n==========================================");
        System.out.println(" MAIN DASHBOARD | Role: " + loggedInUser.getRole());
        System.out.println("==========================================");

        if (loggedInUser.getRole().equals("CUSTOMER")) {
            System.out.println("1. View Phone Inventory");
            System.out.println("2. Purchase a Phone (Checkout)");
            System.out.println("0. Logout");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    PhoneManager.viewPhones();
                    break;
                case "2":
                    PhoneManager.sellPhone(loggedInUser);   // CHANGED: pass the User, not just the name
                    break;
                case "0":
                    logout();
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } else {
            // Staff, Manager, Admin dashboards
            System.out.println("1. View Phone Inventory");
            System.out.println("2. Sell a Phone (Checkout)");

            if (!loggedInUser.getRole().equals("STAFF")) {
                System.out.println("3. Add New Phone (Create)");
                System.out.println("4. Update Phone Details (Update)");
            }

            if (loggedInUser.getRole().equals("ADMIN")) {
                System.out.println("5. Delete Phone (Delete)");
                System.out.println("6. Manage Users (Create/Delete Users)");
            }

            if (!loggedInUser.getRole().equals("STAFF")) {
                System.out.println("7. Sales Reports (Weekly / Monthly / Annual)");
            }

            System.out.println("8. Manage Customer Discounts");   // NEW (all staff roles)

            System.out.println("0. Logout");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    PhoneManager.viewPhones();
                    break;
                case "2":
                    PhoneManager.sellPhone(loggedInUser);   // CHANGED
                    break;
                case "3":
                    if (checkPermission("MANAGER", "ADMIN")) PhoneManager.addPhone();
                    break;
                case "4":
                    if (checkPermission("MANAGER", "ADMIN")) PhoneManager.updatePhone();
                    break;
                case "5":
                    if (checkPermission("ADMIN")) PhoneManager.deletePhone();
                    break;
                case "6":
                    if (checkPermission("ADMIN")) UserManager.manageUsers();
                    break;
                case "7":
                    if (checkPermission("MANAGER", "ADMIN")) ReportManager.showReportMenu();
                    break;
                case "8":                                                     // NEW
                    if (checkPermission("STAFF", "MANAGER", "ADMIN")) DiscountManager.manageDiscounts();
                    break;
                case "0":
                    logout();
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static boolean checkPermission(String... allowedRoles) {
        for (String role : allowedRoles) {
            if (loggedInUser.getRole().equalsIgnoreCase(role)) {
                return true;
            }
        }
        System.out.println("Access Denied: Your role (" + loggedInUser.getRole() + ") cannot perform this action.");
        return false;
    }
}

class User {
    private String username;
    private String password;
    private String role;

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    public String toFileString() {
        return username + "|" + password + "|" + role;
    }
}

class Phone {
    private String id;
    private String brand;
    private String model;
    private double price;
    private int stock;

    public Phone(String id, String brand, String model, double price, int stock) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.price = price;
        this.stock = stock;
    }

    public String getId() { return id; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }

    public void setBrand(String brand) { this.brand = brand; }
    public void setModel(String model) { this.model = model; }
    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }

    public String toFileString() {
        return id + "|" + brand + "|" + model + "|" + price + "|" + stock;
    }
}

class Sale {
    private String saleId, phoneId, username, customer;
    private int quantity;
    private double total;
    private double discountPercent;
    private LocalDateTime timestamp;

    // NEW: full constructor (customer + discount recorded for auditing)
    public Sale(String saleId, String phoneId, int quantity, double total,
                String username, LocalDateTime timestamp,
                String customer, double discountPercent) {
        this.saleId = saleId;
        this.phoneId = phoneId;
        this.quantity = quantity;
        this.total = total;
        this.username = username;
        this.timestamp = timestamp;
        this.customer = customer;
        this.discountPercent = discountPercent;
    }

    // Old constructor kept so nothing else breaks
    public Sale(String saleId, String phoneId, int quantity, double total,
                String username, LocalDateTime timestamp) {
        this(saleId, phoneId, quantity, total, username, timestamp, "-", 0);
    }

    public String getSaleId() { return saleId; }
    public String getPhoneId() { return phoneId; }
    public int getQuantity() { return quantity; }
    public double getTotal() { return total; }
    public String getUsername() { return username; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getCustomer() { return customer; }
    public double getDiscountPercent() { return discountPercent; }
}

class FileHandler {
    public static List<User> readUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    users.add(new User(parts[0], parts[1], parts[2]));
                }
            }
        } catch (IOException e) {
            // File might not exist yet
        }
        return users;
    }

    public static void writeUser(User user) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.txt", true))) {
            writer.write(user.toFileString());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error writing user.");
        }
    }

    public static void saveAllUsers(List<User> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.txt", false))) {
            for (User user : users) {
                writer.write(user.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users list.");
        }
    }

    public static User authenticate(String username, String password) {
        List<User> users = readUsers();
        for (User u : users) {
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    // NEW: look up a user by username (case-insensitive)
    public static User findUser(String username) {
        for (User u : readUsers()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    // NEW: discounts.txt format -> username|percent
    public static Map<String, Double> readDiscounts() {
        Map<String, Double> discounts = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("discounts.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    try {
                        discounts.put(parts[0], Double.parseDouble(parts[1]));
                    } catch (NumberFormatException e) {
                        // skip malformed record
                    }
                }
            }
        } catch (IOException e) {
            // File might not exist yet
        }
        return discounts;
    }

    // NEW
    public static void saveAllDiscounts(Map<String, Double> discounts) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("discounts.txt", false))) {
            for (Map.Entry<String, Double> e : discounts.entrySet()) {
                writer.write(e.getKey() + "|" + e.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving discounts list.");
        }
    }

    public static List<Phone> readPhones() {
        List<Phone> phones = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("phones.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    phones.add(new Phone(parts[0], parts[1], parts[2], Double.parseDouble(parts[3]), Integer.parseInt(parts[4])));
                }
            }
        } catch (IOException e) {
            // File might not exist yet
        }
        return phones;
    }

    public static void saveAllPhones(List<Phone> phones) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("phones.txt", false))) {
            for (Phone p : phones) {
                writer.write(p.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving phones list.");
        }
    }

    public static void writeSale(String saleRecord) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("sales.txt", true))) {
            writer.write(saleRecord);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error recording sale.");
        }
    }

    public static List<Sale> readSales() {
        List<Sale> sales = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try (BufferedReader reader = new BufferedReader(new FileReader("sales.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                // CHANGED: accept old 6-field records and new 8-field records
                if (parts.length == 6 || parts.length == 8) {
                    try {
                        String customer = (parts.length == 8) ? parts[6] : "-";
                        double discount = (parts.length == 8) ? Double.parseDouble(parts[7]) : 0;
                        sales.add(new Sale(parts[0], parts[1], Integer.parseInt(parts[2]),
                                Double.parseDouble(parts[3]), parts[4],
                                LocalDateTime.parse(parts[5], fmt), customer, discount));
                    } catch (NumberFormatException | DateTimeParseException e) {
                        // skip malformed record
                    }
                }
            }
        } catch (IOException e) {
            // File might not exist yet
        }
        return sales;
    }
}

class PhoneManager {
    private static final Scanner scanner = new Scanner(System.in);

    public static void viewPhones() {
        List<Phone> phones = FileHandler.readPhones();
        System.out.println("\n--- PHONE INVENTORY ---");
        if (phones.isEmpty()) {
            System.out.println("No phones found in inventory.");
            return;
        }
        System.out.printf("%-10s | %-12s | %-15s | %-10s | %-5s%n", "ID", "Brand", "Model", "Price", "Stock");
        System.out.println("---------------------------------------------------------------");
        for (Phone p : phones) {
            System.out.printf("%-10s | %-12s | %-15s | $%-9.2f | %-5d%n",
                p.getId(), p.getBrand(), p.getModel(), p.getPrice(), p.getStock());
        }
    }

    public static void addPhone() {
        System.out.println("\n--- ADD NEW PHONE (CREATE) ---");
        System.out.print("Enter Phone ID (e.g., P001): ");
        String id = scanner.nextLine().trim();

        List<Phone> phones = FileHandler.readPhones();
        for (Phone p : phones) {
            if (p.getId().equalsIgnoreCase(id)) {
                System.out.println("Error: Phone ID already exists!");
                return;
            }
        }

        System.out.print("Enter Brand (e.g., Apple, Samsung): ");
        String brand = scanner.nextLine().trim();
        System.out.print("Enter Model (e.g., iPhone 15): ");
        String model = scanner.nextLine().trim();
        System.out.print("Enter Price: ");
        double price = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Enter Initial Stock Quantity: ");
        int stock = Integer.parseInt(scanner.nextLine().trim());

        phones.add(new Phone(id, brand, model, price, stock));
        FileHandler.saveAllPhones(phones);
        System.out.println("Success: Phone added to inventory!");
    }

    public static void updatePhone() {
        viewPhones();
        System.out.println("\n--- UPDATE PHONE DETAILS ---");
        System.out.print("Enter Phone ID to update: ");
        String id = scanner.nextLine().trim();

        List<Phone> phones = FileHandler.readPhones();
        Phone target = null;
        for (Phone p : phones) {
            if (p.getId().equalsIgnoreCase(id)) {
                target = p;
                break;
            }
        }

        if (target == null) {
            System.out.println("Phone ID not found.");
            return;
        }

        System.out.print("Enter new Brand (leave blank to keep '" + target.getBrand() + "'): ");
        String brand = scanner.nextLine().trim();
        if (!brand.isEmpty()) target.setBrand(brand);

        System.out.print("Enter new Model (leave blank to keep '" + target.getModel() + "'): ");
        String model = scanner.nextLine().trim();
        if (!model.isEmpty()) target.setModel(model);

        System.out.print("Enter new Price (leave blank to keep '" + target.getPrice() + "'): ");
        String priceStr = scanner.nextLine().trim();
        if (!priceStr.isEmpty()) target.setPrice(Double.parseDouble(priceStr));

        System.out.print("Enter new Stock (leave blank to keep '" + target.getStock() + "'): ");
        String stockStr = scanner.nextLine().trim();
        if (!stockStr.isEmpty()) target.setStock(Integer.parseInt(stockStr));

        FileHandler.saveAllPhones(phones);
        System.out.println("Success: Phone record updated!");
    }

    public static void deletePhone() {
        viewPhones();
        System.out.println("\n--- DELETE PHONE ---");
        System.out.print("Enter Phone ID to delete: ");
        String id = scanner.nextLine().trim();

        List<Phone> phones = FileHandler.readPhones();
        boolean removed = phones.removeIf(p -> p.getId().equalsIgnoreCase(id));

        if (removed) {
            FileHandler.saveAllPhones(phones);
            System.out.println("Success: Phone deleted from inventory.");
        } else {
            System.out.println("Phone ID not found.");
        }
    }

    // CHANGED: takes the logged-in User so we know whether the buyer is the
    // user themselves (CUSTOMER) or someone the staff is serving.
    public static void sellPhone(User seller) {
        viewPhones();
        System.out.println("\n--- PURCHASE / CHECKOUT ---");

        // 1) Work out who the customer is (null = walk-in, no discount)
        String customer = null;
        if (seller.getRole().equals("CUSTOMER")) {
            customer = seller.getUsername();
        } else {
            System.out.print("Enter customer username (leave blank for walk-in, no discount): ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                User found = FileHandler.findUser(input);
                if (found == null || !found.getRole().equals("CUSTOMER")) {
                    System.out.println("Customer not found. Sale cancelled.");
                    return;
                }
                customer = found.getUsername();
            }
        }

        // 2) Look up their discount
        double discountPercent = (customer == null) ? 0 : DiscountManager.getDiscountPercent(customer);
        if (discountPercent > 0) {
            System.out.println("Customer discount applied: " + DiscountManager.formatPercent(discountPercent) + "% off");
        }

        System.out.print("Enter Phone ID to purchase: ");
        String id = scanner.nextLine().trim();

        List<Phone> phones = FileHandler.readPhones();
        Phone target = null;
        for (Phone p : phones) {
            if (p.getId().equalsIgnoreCase(id)) {
                target = p;
                break;
            }
        }

        if (target == null) {
            System.out.println("Phone ID not found.");
            return;
        }

        if (target.getStock() <= 0) {
            System.out.println("Error: Item is out of stock!");
            return;
        }

        System.out.print("Enter quantity: ");
        int qty;
        try {
            qty = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity.");
            return;
        }

        if (qty <= 0) {
            System.out.println("Error: Quantity must be at least 1.");
            return;
        }

        if (qty > target.getStock()) {
            System.out.println("Error: Not enough stock available. Current stock: " + target.getStock());
            return;
        }

        // 3) Totals (rounded to cents at each step to avoid floating-point noise)
        double subtotal = Math.round(target.getPrice() * qty * 100) / 100.0;
        double discountAmount = Math.round(subtotal * discountPercent) / 100.0;
        double totalPrice = Math.round((subtotal - discountAmount) * 100) / 100.0;

        target.setStock(target.getStock() - qty);
        FileHandler.saveAllPhones(phones);

        // 4) Record the sale: ...|username|timestamp|customer|discountPercent
        String saleId = "S" + System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String record = saleId + "|" + target.getId() + "|" + qty + "|" + totalPrice + "|"
                + seller.getUsername() + "|" + timestamp + "|"
                + (customer == null ? "-" : customer) + "|" + discountPercent;

        FileHandler.writeSale(record);

        System.out.println("\nTransaction Successful!");
        System.out.printf("Subtotal     : $%.2f%n", subtotal);
        if (discountPercent > 0) {
            System.out.printf("Discount (%s%%): -$%.2f%n", DiscountManager.formatPercent(discountPercent), discountAmount);
        }
        System.out.printf("Total Amount : $%.2f%n", totalPrice);
        System.out.println("Remaining Stock for " + target.getModel() + ": " + target.getStock());
    }
}

// NEW: manages per-customer discount percentages (stored in discounts.txt)
class DiscountManager {
    private static final Scanner scanner = new Scanner(System.in);

    // Highest discount a customer can be given. Change this to suit your store.
    public static final double MAX_DISCOUNT_PERCENT = 30.0;

    public static double getDiscountPercent(String username) {
        for (Map.Entry<String, Double> e : FileHandler.readDiscounts().entrySet()) {
            if (e.getKey().equalsIgnoreCase(username)) {
                return e.getValue();
            }
        }
        return 0;
    }

    public static void removeDiscount(String username) {
        Map<String, Double> discounts = FileHandler.readDiscounts();
        if (discounts.keySet().removeIf(k -> k.equalsIgnoreCase(username))) {
            FileHandler.saveAllDiscounts(discounts);
        }
    }

    // 10.0 -> "10", 7.5 -> "7.5"
    public static String formatPercent(double percent) {
        return (percent == Math.floor(percent)) ? String.valueOf((long) percent) : String.valueOf(percent);
    }

    public static void manageDiscounts() {
        while (true) {
            System.out.println("\n--- CUSTOMER DISCOUNTS ---");
            System.out.println("1. View Customer Discounts");
            System.out.println("2. Set / Update Customer Discount");
            System.out.println("3. Remove Customer Discount");
            System.out.println("0. Back to Dashboard");
            System.out.print("Choose option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": {
                    Map<String, Double> discounts = FileHandler.readDiscounts();
                    if (discounts.isEmpty()) {
                        System.out.println("No customer discounts have been set.");
                        break;
                    }
                    System.out.printf("%n%-15s | %-10s%n", "Customer", "Discount");
                    System.out.println("-------------------------");
                    for (Map.Entry<String, Double> e : discounts.entrySet()) {
                        System.out.printf("%-15s | %s%%%n", e.getKey(), formatPercent(e.getValue()));
                    }
                    break;
                }
                case "2": {
                    System.out.print("Enter customer username: ");
                    User customer = FileHandler.findUser(scanner.nextLine().trim());
                    if (customer == null || !customer.getRole().equals("CUSTOMER")) {
                        System.out.println("Customer not found.");
                        break;
                    }

                    System.out.print("Enter discount percent (0-" + formatPercent(MAX_DISCOUNT_PERCENT)
                            + ", 0 removes the discount): ");
                    double percent;
                    try {
                        percent = Double.parseDouble(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number.");
                        break;
                    }
                    if (!(percent >= 0 && percent <= MAX_DISCOUNT_PERCENT)) {   // also rejects NaN
                        System.out.println("Discount must be between 0 and " + formatPercent(MAX_DISCOUNT_PERCENT) + ".");
                        break;
                    }

                    String name = customer.getUsername();
                    Map<String, Double> discounts = FileHandler.readDiscounts();
                    discounts.keySet().removeIf(k -> k.equalsIgnoreCase(name));
                    if (percent > 0) {
                        discounts.put(name, percent);
                    }
                    FileHandler.saveAllDiscounts(discounts);

                    if (percent > 0) {
                        System.out.println("Success: " + name + " now gets " + formatPercent(percent) + "% off.");
                    } else {
                        System.out.println("Discount removed for " + name + ".");
                    }
                    break;
                }
                case "3": {
                    System.out.print("Enter customer username: ");
                    String name = scanner.nextLine().trim();
                    if (getDiscountPercent(name) > 0) {
                        removeDiscount(name);
                        System.out.println("Discount removed for " + name + ".");
                    } else {
                        System.out.println("That customer has no discount.");
                    }
                    break;
                }
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}

class UserManager {
    private static final Scanner scanner = new Scanner(System.in);

    public static void manageUsers() {
        while (true) {
            System.out.println("\n--- USER MANAGEMENT (ADMIN) ---");
            System.out.println("1. View Users");
            System.out.println("2. Create New User");
            System.out.println("3. Delete User");
            System.out.println("0. Back to Dashboard");
            System.out.print("Choose option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    List<User> users = FileHandler.readUsers();
                    System.out.printf("\n%-15s | %-10s%n", "Username", "Role");
                    System.out.println("-------------------------");
                    for (User u : users) {
                        System.out.printf("%-15s | %-10s%n", u.getUsername(), u.getRole());
                    }
                    break;
                case "2":
                    System.out.print("Enter Username: ");
                    String uname = scanner.nextLine().trim();
                    System.out.print("Enter Password: ");
                    String pass = scanner.nextLine().trim();
                    System.out.print("Enter Role (ADMIN, MANAGER, STAFF, CUSTOMER): ");
                    String role = scanner.nextLine().trim().toUpperCase();

                    if (!role.equals("ADMIN") && !role.equals("MANAGER") && !role.equals("STAFF") && !role.equals("CUSTOMER")) {
                        System.out.println("Invalid role specified.");
                        break;
                    }

                    FileHandler.writeUser(new User(uname, pass, role));
                    System.out.println("User created successfully!");
                    break;
                case "3":
                    System.out.print("Enter Username to delete: ");
                    String targetUser = scanner.nextLine().trim();
                    if (targetUser.equalsIgnoreCase("admin")) {
                        System.out.println("Cannot delete the primary admin account!");
                        break;
                    }
                    List<User> allUsers = FileHandler.readUsers();
                    boolean removed = allUsers.removeIf(u -> u.getUsername().equalsIgnoreCase(targetUser));
                    if (removed) {
                        FileHandler.saveAllUsers(allUsers);
                        DiscountManager.removeDiscount(targetUser);   // NEW: don't leave an orphaned discount
                        System.out.println("User deleted successfully.");
                    } else {
                        System.out.println("User not found.");
                    }
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}

class ReportManager {
    private static final Scanner scanner = new Scanner(System.in);

    public static void showReportMenu() {
        while (true) {
            System.out.println("\n--- SALES REPORTS ---");
            System.out.println("1. Weekly Report");
            System.out.println("2. Monthly Report");
            System.out.println("3. Annual Report");
            System.out.println("0. Back to Dashboard");
            System.out.print("Choose option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": weeklyReport(); break;
                case "2": monthlyReport(); break;
                case "3": annualReport(); break;
                case "0": return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void weeklyReport() {
        System.out.print("Enter any date in the week (yyyy-MM-dd, blank = this week): ");
        String input = scanner.nextLine().trim();
        LocalDate ref;
        try {
            ref = input.isEmpty() ? LocalDate.now() : LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Use yyyy-MM-dd.");
            return;
        }
        LocalDate start = ref.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = start.plusDays(6);
        generateReport("WEEKLY SALES REPORT (" + start + " to " + end + ")",
                start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }

    private static void monthlyReport() {
        System.out.print("Enter month (yyyy-MM, blank = this month): ");
        String input = scanner.nextLine().trim();
        YearMonth ym;
        try {
            ym = input.isEmpty() ? YearMonth.now() : YearMonth.parse(input);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid format. Use yyyy-MM (e.g., 2026-10).");
            return;
        }
        generateReport("MONTHLY SALES REPORT (" + ym + ")",
                ym.atDay(1).atStartOfDay(), ym.plusMonths(1).atDay(1).atStartOfDay());
    }

    private static void annualReport() {
        System.out.print("Enter year (yyyy, blank = this year): ");
        String input = scanner.nextLine().trim();
        int year;
        try {
            year = input.isEmpty() ? LocalDate.now().getYear() : Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid year.");
            return;
        }
        generateReport("ANNUAL SALES REPORT (" + year + ")",
                LocalDate.of(year, 1, 1).atStartOfDay(),
                LocalDate.of(year + 1, 1, 1).atStartOfDay());
    }

    // 'from' is inclusive, 'to' is exclusive
    private static void generateReport(String title, LocalDateTime from, LocalDateTime to) {
        List<Sale> sales = new ArrayList<>();
        for (Sale s : FileHandler.readSales()) {
            if (!s.getTimestamp().isBefore(from) && s.getTimestamp().isBefore(to)) {
                sales.add(s);
            }
        }

        System.out.println("\n==========================================================");
        System.out.println(" " + title);
        System.out.println("==========================================================");

        if (sales.isEmpty()) {
            System.out.println("No sales recorded for this period.");
            return;
        }

        // Map phone ID -> display name (phone may have been deleted since the sale)
        Map<String, String> names = new HashMap<>();
        for (Phone p : FileHandler.readPhones()) {
            names.put(p.getId().toUpperCase(), p.getBrand() + " " + p.getModel());
        }

        Map<String, int[]> unitsByPhone = new TreeMap<>();
        Map<String, Double> revenueByPhone = new HashMap<>();
        int totalUnits = 0;
        double totalRevenue = 0;

        for (Sale s : sales) {
            String key = s.getPhoneId().toUpperCase();
            unitsByPhone.computeIfAbsent(key, k -> new int[1])[0] += s.getQuantity();
            revenueByPhone.merge(key, s.getTotal(), Double::sum);
            totalUnits += s.getQuantity();
            totalRevenue += s.getTotal();
        }

        System.out.printf("%-8s | %-24s | %-6s | %-12s%n", "ID", "Phone", "Units", "Revenue");
        System.out.println("----------------------------------------------------------");
        String bestId = null;
        for (Map.Entry<String, int[]> e : unitsByPhone.entrySet()) {
            String id = e.getKey();
            int units = e.getValue()[0];
            System.out.printf("%-8s | %-24s | %-6d | $%-11.2f%n",
                    id, names.getOrDefault(id, "(deleted item)"), units, revenueByPhone.get(id));
            if (bestId == null || units > unitsByPhone.get(bestId)[0]) bestId = id;
        }

        System.out.println("----------------------------------------------------------");
        System.out.println("Total Transactions : " + sales.size());
        System.out.println("Total Units Sold   : " + totalUnits);
        System.out.printf("Total Revenue      : $%.2f%n", totalRevenue);
        System.out.printf("Average per Sale   : $%.2f%n", totalRevenue / sales.size());
        System.out.println("Best Seller        : " + names.getOrDefault(bestId, bestId)
                + " (" + unitsByPhone.get(bestId)[0] + " units)");
    }
}