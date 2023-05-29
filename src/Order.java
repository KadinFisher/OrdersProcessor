package processor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents an order for processing. Each order has the potential to be 
 * processed by a single thread. An order has an ID for the key, an array of 
 * products to order, and a total cost.
 * 
 * @author King Bass
 *
 */
class Order {
	
	private int id;
	private double totalCost;
	private ArrayList<Product> productsOrdered;
	
	/**
	 * Initializes an order to be processed by a thread.
	 * 
	 * @param id
	 * @param cost
	 */
	public Order(int id) {
		this.id = id;
		this.productsOrdered = new ArrayList<Product>();
	}
	
	/**
	 * Returns the Id of an order.
	 * 
	 * @return The id.
	 */
	public int getId() {
		return this.id;
	}
	
	@Override
	public String toString() {
		String answer = "";
		Collections.sort(productsOrdered);
		answer += "----- Order details for client with Id: " + id + " -----\n";
		for (Product product: productsOrdered) {
			answer += product.toStringForOrder();
		}
		answer += "Order Total: $" + formatMoney(totalCost) + "\n"; 
		return answer;
	}
	
	/**
	 * Adds a product to the order accordingly by updating correct instance 
	 * variables. The product added is a deep copy of the Processor's product.
	 * 
	 * @param product The product you'd like to add to the order. Intended to be
	 * 				  a direct product from the OrdersProcessors products.
	 * 
	 * @throws IllegalArgumentException If the product being added is null.
	 */
	public void addProduct(Product product) {
		if (!(product == null)) {
			this.totalCost += product.getCost();
			if (productsOrdered.contains(product)) {
				Product p = getSpecificProduct(product.getName());
				p.incrementTotalNumOrder(1);
			} else {
				Product p = new Product(product);
				this.productsOrdered.add(p);
			}
		} else {
			throw new IllegalArgumentException("Product being added to order is"
					+ " null.");
		}
	}
	
	/**
	 * Gets the specified product from the productsOrdered ArrayList based off 
	 * of it's name.
	 * 
	 * @param product The products name.
	 * 
	 * @throws IllegalArgumentException If the product does not exist in 
	 * 									productsOrdered.
	 * 
	 * @return The product you'd like to return.
	 */
	private Product getSpecificProduct(String product) {
		Product answer = null;
		for (Product p : productsOrdered) {
			if (p.getName().equals(product)) {
				answer = p;
			}
		}
		if (answer == null) {
			throw new IllegalArgumentException("Product does not exist to "
					+ "get from productsOrdered ArrayList");
		}
		return answer;
	}
	
	/**
	 * Takes a double and returns it in the correct format for U.S. currency.
	 * 
	 * @param amount The double you'd like formatted.
	 * 
	 * @return The double as a String in the correct format.
	 */
	private static String formatMoney(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        return formatter.format(amount);
    }
}
