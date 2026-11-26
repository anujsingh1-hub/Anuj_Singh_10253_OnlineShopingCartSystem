import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class OnlineShoppingCart {

    static final String PRODUCT_FILE = "products.txt";
    static final String ORDER_FILE = "orders.txt";

    // Product record stored as the value of the HashMap (key = product ID).
    static class Product {
        String id, name;
        int price;
        Product(String id, String name, int price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }
    }

    // One line of the cart: a product and the quantity added in one operation.
    static class CartItem {
        Product product;
        int quantity;
        CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
        long total() { return (long) product.price * quantity; }
    }

    // Node of the self-implemented singly linked list.
    static class Node {
        CartItem data;
        Node next;
        Node(CartItem data) { this.data = data; }
    }

    // Singly linked list that stores the cart (insertion at tail is O(1)).
    static class CartList {
        Node head, tail;
        int size;

        Node add(CartItem item) {
            Node node = new Node(item);
            if (head == null) head = tail = node;
            else { tail.next = node; tail = node; }
            size++;
            return node;
        }

        boolean remove(Node target) {
            if (head == null || target == null) return false;
            if (head == target) {
                head = head.next;
                if (head == null) tail = null;
                size--;
                return true;
            }
            Node prev = head;
            while (prev.next != null && prev.next != target) prev = prev.next;
            if (prev.next == null) return false;
            prev.next = target.next;
            if (target == tail) tail = prev;
            size--;
            return true;
        }

        boolean isEmpty() { return head == null; }

        long grandTotal() {
            long sum = 0;
            for (Node c = head; c != null; c = c.next) sum += c.data.total();
            return sum;
        }

        void clear() { head = tail = null; size = 0; }

        // Copy used for sorting, so the real cart order (and the stack) stays unchanged.
        CartList copy() {
            CartList c = new CartList();
            for (Node n = head; n != null; n = n.next) c.add(n.data);
            return c;
        }

        // Merge sort on the linked list: item total, high to low (stable).
        void sortByTotalDescending() {
            head = mergeSort(head);
            tail = head;
            while (tail != null && tail.next != null) tail = tail.next;
        }

        private Node mergeSort(Node node) {
            if (node == null || node.next == null) return node;
            Node slow = node, fast = node.next;
            while (fast != null && fast.next != null) {
                slow = slow.next;
                fast = fast.next.next;
            }
            Node second = slow.next;
            slow.next = null;
            return merge(mergeSort(node), mergeSort(second));
        }

        private Node merge(Node a, Node b) {
            Node dummy = new Node(null), t = dummy;
            while (a != null && b != null) {
                if (a.data.total() >= b.data.total()) { t.next = a; a = a.next; }
                else { t.next = b; b = b.next; }
                t = t.next;
            }
            t.next = (a != null) ? a : b;
            return dummy.next;
        }
    }

    // Array-based stack of cart nodes; the top is the last added item.
    static class NodeStack {
        private Node[] data = new Node[10];
        private int top = 0;

        void push(Node n) {
            if (top == data.length) data = Arrays.copyOf(data, top * 2);
            data[top++] = n;
        }

        Node pop() {
            if (top == 0) return null;
            Node n = data[--top];
            data[top] = null;
            return n;
        }

        boolean isEmpty() { return top == 0; }

        void clear() {
            Arrays.fill(data, 0, top, null);
            top = 0;
        }
    }

    static final Map<String, Product> products = new HashMap<>();
    static final CartList cart = new CartList();
    static final NodeStack undoStack = new NodeStack();
    static Scanner sc;

    // ---------- File handling ----------
    static void loadProducts() {
        File file = new File(PRODUCT_FILE);
        if (!file.exists()) {
            products.put("P101", new Product("P101", "Notebook", 50));
            products.put("P102", new Product("P102", "Pen", 10));
            products.put("P103", new Product("P103", "Bag", 800));
            saveProducts();
            System.out.println(PRODUCT_FILE + " not found. Default products created.");
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                try {
                    if (parts.length != 3) throw new NumberFormatException();
                    String id = parts[0].trim().toUpperCase();
                    int price = Integer.parseInt(parts[2].trim());
                    if (id.isEmpty() || price <= 0) throw new NumberFormatException();
                    products.put(id, new Product(id, parts[1].trim(), price));
                } catch (NumberFormatException e) {
                    System.out.println("Skipped invalid line in " + PRODUCT_FILE + ": " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read " + PRODUCT_FILE + ": " + e.getMessage());
        }
    }

    static void saveProducts() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(PRODUCT_FILE))) {
            for (Product p : new TreeMap<>(products).values())
                pw.println(p.id + "," + p.name + "," + p.price);
        } catch (IOException e) {
            System.out.println("Could not save " + PRODUCT_FILE + ": " + e.getMessage());
        }
    }

    static void saveOrder(long total) {
        StringBuilder sb = new StringBuilder();
        for (Node c = cart.head; c != null; c = c.next) {
            if (sb.length() > 0) sb.append("; ");
            sb.append(c.data.product.name).append(" x").append(c.data.quantity)
              .append(" = ").append(c.data.total());
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(ORDER_FILE, true))) {
            pw.println(LocalDateTime.now().withNano(0) + " | " + sb + " | Total: " + total);
        } catch (IOException e) {
            System.out.println("Could not write " + ORDER_FILE + ": " + e.getMessage());
        }
    }

    // ---------- Input helpers ----------
    static String readLine(String prompt) {
        System.out.print(prompt);
        if (!sc.hasNextLine()) return null;
        return sc.nextLine().trim();
    }

    // Returns a positive whole number, or -1 if input has ended.
    static int readQuantity() {
        while (true) {
            String s = readLine("Quantity: ");
            if (s == null) return -1;
            try {
                int q = Integer.parseInt(s);
                if (q > 0) return q;
            } catch (NumberFormatException e) { /* fall through */ }
            System.out.println("Invalid quantity. Enter a positive whole number.");
        }
    }

    // ---------- Features ----------
    static void viewProducts() {
        System.out.println("\nID\tName\t\tPrice");
        for (Product p : new TreeMap<>(products).values())
            System.out.println(p.id + "\t" + p.name + "\t\t" + p.price);
    }

    static void addToCart() {
        String id = readLine("Product ID: ");
        if (id == null) return;
        id = id.toUpperCase();
        if (id.isEmpty()) { System.out.println("Product ID cannot be empty."); return; }
        Product p = products.get(id);
        if (p == null) { System.out.println("Product " + id + " not found."); return; }
        int qty = readQuantity();
        if (qty < 0) return;
        CartItem item = new CartItem(p, qty);
        undoStack.push(cart.add(item));
        System.out.println("Added: " + p.name + " x" + qty + " = " + item.total());
    }

    static void removeLast() {
        Node last = undoStack.pop();
        if (last == null) { System.out.println("Cart is empty. Nothing to remove."); return; }
        cart.remove(last);
        System.out.println("Removed: " + last.data.product.name + " x"
                + last.data.quantity + " = " + last.data.total());
    }

    static void viewCart() {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty.");
            System.out.println("Total: 0");
            return;
        }
        System.out.println("Your cart:");
        int i = 1;
        for (Node c = cart.head; c != null; c = c.next)
            System.out.println(i++ + ". " + c.data.product.name + " x" + c.data.quantity
                    + " | " + c.data.total());
        System.out.println("Total: " + cart.grandTotal());
    }

    static void sortCart() {
        if (cart.isEmpty()) { System.out.println("Your cart is empty."); return; }
        CartList sorted = cart.copy();      // the real cart order is not disturbed
        sorted.sortByTotalDescending();
        System.out.println("Cart sorted by item total (high to low):");
        int i = 1;
        for (Node c = sorted.head; c != null; c = c.next)
            System.out.println(i++ + ". " + c.data.product.name + " | " + c.data.total());
    }

    static void checkout() {
        if (cart.isEmpty()) { System.out.println("Your cart is empty. Nothing to checkout."); return; }
        long total = cart.grandTotal();
        saveOrder(total);
        System.out.println("Order placed. Total: " + total);
        cart.clear();
        undoStack.clear();
    }

    public static void main(String[] args) {
        sc = new Scanner(System.in);
        loadProducts();
        System.out.println("===== ONLINE SHOPPING CART SYSTEM =====");
        boolean running = true;
        while (running) {
            System.out.println("\n1. View products");
            System.out.println("2. Add to cart");
            System.out.println("3. View cart");
            System.out.println("4. Remove last added item");
            System.out.println("5. Sort cart by item total (high to low)");
            System.out.println("6. Checkout");
            System.out.println("7. Save and exit");
            String choice = readLine("Enter choice: ");
            if (choice == null) break;           // end of input: save and exit
            switch (choice) {
                case "1": viewProducts(); break;
                case "2": addToCart(); break;
                case "3": viewCart(); break;
                case "4": removeLast(); break;
                case "5": sortCart(); break;
                case "6": checkout(); break;
                case "7": running = false; break;
                default: System.out.println("Invalid choice. Enter a number from 1 to 7.");
            }
        }
        saveProducts();
        System.out.println("\nProducts saved to " + PRODUCT_FILE + ". Goodbye!");
        sc.close();
    }
}