# Anuj_Singh_10253_OnlineShopingCartSystem
# Online Shopping Cart System

A console-based Java application that lets a user view a product catalogue, add products to a shopping cart, view the cart, remove the last added item, sort the cart by item total and check out. Developed as a DSA project (PS No. 11) using a HashMap, a self-implemented singly linked list, a self-implemented stack and merge sort.

## Features

- View the product catalogue (loaded from `products.txt`)
- Add products to the cart with a quantity
- View the cart with item totals and the cart total
- Remove the last added item
- Sort the cart by item total, highest first
- Checkout: save the order to `orders.txt` and empty the cart
- Save the product catalogue on exit

## Data Structures Used

| Structure | Purpose |
|---|---|
| `HashMap<String, Product>` | Product catalogue keyed by product ID, for fast lookup |
| Singly linked list (`CartList`) | Stores the cart items in insertion order |
| Stack (`NodeStack`) | Keeps the most recently added item on top, for "remove last" |
| Merge sort | Sorts a copy of the cart by item total, high to low |

## How the Cart Is Stored

- Each product line in `products.txt` is `ID,Name,Price`.
- Adding an item creates a cart entry (product plus quantity) and appends it to the linked list. The same node is pushed on the stack.
- Removing the last item pops the stack and deletes that node from the linked list.
- Sorting works on a copy, so the real cart order and the stack stay in step.

## Calculation

```
Item Total = Price × Quantity
Cart Total = Sum of all item totals
```

Example: Notebook × 2 = 50 × 2 = 100; Bag × 1 = 800 × 1 = 800; Cart Total = 900.

## Requirements

- JDK 8 or above (JDK 21 recommended)
- VS Code, IntelliJ IDEA, Eclipse, or any text editor
- Command Prompt, Terminal, or the VS Code terminal

## How to Run

```bash
javac OnlineShoppingCart.java
java OnlineShoppingCart
```

Keep `products.txt` in the same folder as the program. If it does not exist, the program creates it with three default products.

## Default Products (`products.txt`)

```
P101,Notebook,50
P102,Pen,10
P103,Bag,800
```

## Menu

```
1. View products
2. Add to cart
3. View cart
4. Remove last added item
5. Sort cart by item total (high to low)
6. Checkout
7. Save and exit
```

## Sample Run

```
Enter choice:
