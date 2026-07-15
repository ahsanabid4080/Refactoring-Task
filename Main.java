// =============================================================================
// FILE: Lab11_Complete.java
// DESC: All classes for the refactoring lab in one file.
//       Run with:  javac Lab11_Complete.java && java Main
//
// CLASS LIST:
//   1.  Customer
//   2.  Product
//   3.  Order
//   4.  InvoicePrinter
//   5.  DiscountCalculator
//   6.  OrderProcessor
//   7.  Main
//
// REFACTORING STEPS APPLIED (matches Lab Manual sections):
//   Step 1  — Rename variables & fields
//   Step 2  — Replace magic numbers with constants
//   Step 3  — Extract Method
//   Step 4  — Encapsulate fields (private + getters/setters)
//   Step 5  — Move Method + Seam (InvoicePrinter injected via constructor)
//   Step 6  — Sprout Method (printLoyaltyPoints)
//   Task 2  — Move discount logic to DiscountCalculator (new Seam)
//   Task 3  — Sprout isEligibleForPremium() on Customer
// =============================================================================


// =============================================================================
// CLASS 1: Customer
// =============================================================================
class Customer {

    private String name;
    private String address;
    private String email;
    private int    age;

    public Customer(String name, String address, String email, int age) {
        this.name    = name;
        this.address = address;
        this.email   = email;
        this.age     = age;
    }

    // Getters
    public String getName()    { return name; }
    public String getAddress() { return address; }
    public String getEmail()   { return email; }
    public int    getAge()     { return age; }

    // Task 3 — Sprout Method
    // Returns true if customer is 21 years or older (eligible for premium).
    public boolean isEligibleForPremium() {
        return age >= 21;
    }
}


// =============================================================================
// CLASS 2: Product
// =============================================================================
class Product {

    private String name;
    private double price;
    private int    quantity;

    public Product(String name, double price, int quantity) {
        this.name     = name;
        this.price    = price;
        this.quantity = quantity;
    }

    // Getters
    public String getName()     { return name; }
    public double getPrice()    { return price; }
    public int    getQuantity() { return quantity; }
}


// =============================================================================
// CLASS 3: Order
// =============================================================================
class Order {

    private Customer      customer;
    private java.util.List<Product> products;
    private String        status;

    public Order(Customer customer, java.util.List<Product> products) {
        this.customer = customer;
        this.products = products;
        this.status   = "PENDING";
    }

    // Getters & setter
    public Customer               getCustomer() { return customer; }
    public java.util.List<Product> getProducts() { return products; }
    public String                 getStatus()   { return status; }
    public void                   setStatus(String status) { this.status = status; }
}


// =============================================================================
// CLASS 4: InvoicePrinter  (Move Method + Seam — Refactoring Step 5)
// Printing responsibility is separated from processing.
// OrderProcessor receives this via constructor, so it can be swapped in tests.
// =============================================================================
class InvoicePrinter {

    public void print(java.util.List<Product> products,
                      double subtotal,
                      double discount,
                      double tax,
                      double grandTotal) {

        System.out.println("---- INVOICE ----");
        System.out.println("Items:");

        for (Product product : products) {
            System.out.println(
                product.getName()
                + " x" + product.getQuantity()
                + " @ " + product.getPrice()
            );
        }

        System.out.println("Subtotal:    " + subtotal);
        System.out.println("Discount:    " + discount);
        System.out.println("Tax (15%):   " + tax);
        System.out.println("Grand Total: " + grandTotal);
    }
}


// =============================================================================
// CLASS 5: DiscountCalculator  (Task 2 — Move Method + new Seam)
// Discount logic extracted from OrderProcessor and injected via constructor.
// =============================================================================
class DiscountCalculator {

    private static final double HIGH_DISCOUNT_THRESHOLD = 500.0;
    private static final double LOW_DISCOUNT_THRESHOLD  = 200.0;
    private static final double HIGH_DISCOUNT_RATE      = 0.10;
    private static final double LOW_DISCOUNT_RATE       = 0.05;

    public double calculate(double subtotal) {
        if (subtotal > HIGH_DISCOUNT_THRESHOLD) {
            return subtotal * HIGH_DISCOUNT_RATE;
        } else if (subtotal > LOW_DISCOUNT_THRESHOLD) {
            return subtotal * LOW_DISCOUNT_RATE;
        }
        return 0;
    }
}


// =============================================================================
// CLASS 6: OrderProcessor
// Orchestrates the full order processing pipeline.
// Dependencies (InvoicePrinter, DiscountCalculator) are injected via
// constructor — two independent Seams that can be replaced with mocks.
// =============================================================================
class OrderProcessor {

    // Step 2 — Constants replacing magic numbers
    private static final double TAX_RATE    = 0.15;
    private static final int    MINIMUM_AGE = 18;

    // Seams — injected dependencies
    private final InvoicePrinter      invoicePrinter;
    private final DiscountCalculator  discountCalculator;

    public OrderProcessor(InvoicePrinter invoicePrinter,
                          DiscountCalculator discountCalculator) {
        this.invoicePrinter     = invoicePrinter;
        this.discountCalculator = discountCalculator;
    }

    // -------------------------------------------------------------------------
    // Main entry point — Step 3 (Extract Method applied here)
    // -------------------------------------------------------------------------
    public void process(Order order) {
        printCustomerInfo(order.getCustomer());

        double subtotal   = calculateSubtotal(order.getProducts());
        double discount   = discountCalculator.calculate(subtotal);   // Task 2
        double tax        = calculateTax(subtotal - discount);
        double grandTotal = (subtotal - discount) + tax;

        invoicePrinter.print(order.getProducts(), subtotal, discount, tax, grandTotal);

        validateCustomerAge(order.getCustomer());
        checkPremiumEligibility(order.getCustomer());                 // Task 3
        updateOrderStatus(order, grandTotal);
        printLoyaltyPoints(grandTotal);                               // Step 6
    }

    // -------------------------------------------------------------------------
    // Extracted private methods (Step 3 — Extract Method)
    // -------------------------------------------------------------------------

    private void printCustomerInfo(Customer customer) {
        System.out.println("Customer: " + customer.getName());
        System.out.println("Email:    " + customer.getEmail());
        System.out.println("Age:      " + customer.getAge());
    }

    private double calculateSubtotal(java.util.List<Product> products) {
        double subtotal = 0;
        for (Product product : products) {
            subtotal += product.getPrice() * product.getQuantity();
        }
        return subtotal;
    }

    private double calculateTax(double amountAfterDiscount) {
        return amountAfterDiscount * TAX_RATE;
    }

    private void validateCustomerAge(Customer customer) {
        if (customer.getAge() < MINIMUM_AGE) {
            System.out.println("WARNING: Customer is under 18.");
        }
    }

    // Task 3 — calls the sprouted method on Customer
    private void checkPremiumEligibility(Customer customer) {
        if (customer.isEligibleForPremium()) {
            System.out.println("Premium Customer");
        }
    }

    private void updateOrderStatus(Order order, double grandTotal) {
        order.setStatus(grandTotal > 0 ? "PROCESSED" : "FAILED");
        System.out.println("Order Status: " + order.getStatus());
    }

    // Step 6 — Sprout Method: new feature added without touching process() logic
    private void printLoyaltyPoints(double grandTotal) {
        int points = (int)(grandTotal / 10);
        System.out.println("Loyalty Points Earned: " + points);
    }
}


// =============================================================================
// CLASS 7: Main  — entry point
// =============================================================================
public class Main {

    public static void main(String[] args) {

        // Build customer
        Customer customer = new Customer(
            "Ali Hassan", "Gujranwala", "ali@email.com", 22
        );

        // Build product list
        java.util.List<Product> products = new java.util.ArrayList<>();
        products.add(new Product("Laptop",   350.0, 1));
        products.add(new Product("Mouse",     25.0, 2));
        products.add(new Product("Keyboard",  45.0, 1));

        // Build order
        Order order = new Order(customer, products);

        // Wire up dependencies (Seams)
        InvoicePrinter     printer    = new InvoicePrinter();
        DiscountCalculator calculator = new DiscountCalculator();
        OrderProcessor     processor  = new OrderProcessor(printer, calculator);

        // Process
        processor.process(order);
    }
}
