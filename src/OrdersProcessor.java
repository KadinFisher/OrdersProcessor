package processor;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;


/**
 * This class reads items from a file, then orders from a file set,
 * and processes them (writes results to a results file) either single thread
 * or multiple thread. The idea of this class is to compare computing time 
 * between single thread processing and multiple thread processing.
 * 
 * @author King Bass
 *
 */
// Fix the summary order total price. Maybe add instance variable.\
public class OrdersProcessor {
	
	/** The Products available to purchase */
	private ArrayList<Product> products = new ArrayList<Product>();
	
	/** Whether we use single thread or multiple threading. */
	private boolean threadsBool;
	
	/** The number of orders to process */
	private int ordersToProcess;
	
	/** The orders being processed */
	private Map<Integer, Order> ordersFiles;
	
	/** The result file where the output will be written */
	private File resultsFile;
	
	/** An ArrayList utilized for holding all threads created. */
	private ArrayList<Thread> threads;
	
	/**
	 * Constructor that initializes all instance variables accordingly.
	 * 
	 * @throws FileNotFoundException This should never throw. See explanation in
	 * 								 initializeOrder methods' Javadocs.
	 */
	public OrdersProcessor() throws FileNotFoundException {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter item's data file name: "); 
		this.initializeProducts(scanner);
		System.out.print("Enter 'y' for multiple threads, any other character "
				+ "otherwise: ");
		threadsBool = scanner.next().equalsIgnoreCase("y") ? true : false;
		if (threadsBool) {
			threads = new ArrayList<Thread>();
		}
		System.out.print("Enter number of orders to process: ");
		ordersToProcess = scanner.nextInt();
		System.out.print("Enter order's base filename: ");
		String baseFileName = scanner.next();
		this.ordersFiles = new TreeMap<Integer, Order>();
		Long startTime = System.currentTimeMillis();
		if (threadsBool == true) {
			initializeOrdersMultiThread(baseFileName);
		} else {
			initializeOrdersSingleThread(baseFileName);
		}
		long endTime = System.currentTimeMillis();
		System.out.print("Enter result's filename: ");
		this.resultsFile  = new File(scanner.next());
		scanner.close();
		writeOrders();
		System.out.println("Results can be found in the file: " + resultsFile.getName());
		System.out.println("Processing time (msec): " + (endTime - startTime));
	}

	/**
	 * Initializes and creates products based on the input in the main method.
	 * Only used within constructor.
	 * 
	 * @param scanner  The scanner to read user input.
	 * @param products The products instance variable.
	 */
	private void initializeProducts(Scanner scanner) {
		try {
			Scanner reader = new Scanner(new File(scanner.next()));
			while (reader.hasNextLine()) {
				String productName = reader.next();
				double cost = Double.valueOf(reader.next());
				this.products.add(new Product(productName, cost));
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return;
	}
	
	/**
	 * Initializes and creates products based on the input in the main method.
	 * Only used within constructor.
	 * 
	 * @param scanner  The scanner to read user input.
	 * @param products The products instance variable.
	 * @throws FileNotFoundException This should never throw since Scanner is 
	 * 								 initiated inside for-each loop.
	 */
	private void initializeOrdersSingleThread(String baseFileName)  {
		Scanner reader;
		for (int i = 1; i < ordersToProcess + 1; i++) {
			String fileName = baseFileName + i + ".txt";
			File file = new File(fileName);
			try {
				reader = new Scanner(file);
				reader.next();
				int orderId = reader.nextInt();
				reader.nextLine();
				Order order = new Order(orderId);
				this.ordersFiles.put(order.getId(), order);
				while (reader.hasNextLine()) {
					Product productToAdd = this.getSpecificProduct(reader.next());
					reader.nextLine();
					productToAdd.incrementTotalNumOrder(1);
					order.addProduct(productToAdd);
				}
			} catch (IllegalArgumentException e) {
				System.err.print("An order requested a product that " + "didn't exist.");
			} catch (FileNotFoundException e) {
				break;
			}
		}
		return;
	}
	
	/**
	 * Initializes and creates products based on the input in the main method.
	 * Only used within constructor. This is a multi-threaded initialization
	 * where each thread will handle a different processing a seperate order.
	 * 
	 * @param baseFileName
	 */
	private void initializeOrdersMultiThread(String baseFileName) {
		for (int i = 1; i < ordersToProcess + 1; i++) {
			String fileName = baseFileName + i + ".txt";
			File file = new File(fileName);
			ThreadObj threadObj = new ThreadObj(this, file, baseFileName);
			threads.add(new Thread(threadObj));
			
		}
		runThreads();
	}
	
	/**
	 * Runs and joins all the threads created in initializeOrdersMultiThread().
	 */
	private void runThreads() {
		for (Thread thread : threads) {
			thread.start();
		}
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.err.print("Thread interrupted");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Writes all the orders into the resultsFile including a summary of the 
	 * entire order.
	 */
	private void writeOrders() {
		try {
			FileWriter writer = new FileWriter(resultsFile);
			for (Map.Entry<Integer, Order> entry : ordersFiles.entrySet()) {
				System.out.print("Reading order for client with id: ");
				System.out.println(entry.getKey());
				writer.write(entry.getValue().toString());
			}
			writer.write(this.summaryToString());
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the specified product from the products ArrayList based off of it's
	 * name.
	 * 
	 * @param product The products name.
	 * 
	 * @throws IllegalArgumentException If the product does not exist in products.
	 * 
	 * @return The product you'd like to return.
	 */
	private Product getSpecificProduct(String product) {
		Product answer = null;
		for (Product p : this.products) {
			if (p.getName().equals(product)) {
				answer = p;
			}
		}
		if (answer == null) {
			throw new IllegalArgumentException("Product does not exist to "
					+ "get from products ArrayList");
		}
		return answer;
	}
	
	/**
	 * Returns a String of the summary of all the orders. (Used in toString() 
	 * method).
	 * 
	 * @return The order summary's String.
	 */
	private String summaryToString() {
		String answer = "";
		double totalCost = 0.0;
		Collections.sort(products);
		answer += "***** Summary of all orders *****\n";
		for (Product product : products) {
			totalCost += product.getCost() * product.getTotalNumOrdered();
			answer += product.toStringForOrdersSummary();
		}
		answer += "Summary Grand Total: $" + formatMoney(totalCost) + "\n";
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
	
	/**
	 * My thread object that defines the run method for my threads.
	 * 
	 * @author King Bass
	 */
	private class ThreadObj implements Runnable {
		private OrdersProcessor op;
		private File file;
		
		/**
		 * Constructor to initialize our lock object as the OrdersProcessor.
		 * 
		 * @param op The OrdersProcessor that will serve as a lock.
		 * @param file The file that will be processed in the run method.
		 */
		public ThreadObj(OrdersProcessor op, File file, String baseFileName) {
			this.op = op;
			this.file = file;
		}
		
		/**
		 * Overridden run method for the thread. The run method will process 
		 * each file that needs to be used in the 
		 * initializeOrdersMultiThreadMethod().
		 */
		@Override
		public void run() {
			try {
				Scanner reader = new Scanner(file);
				reader.next();
				int orderId = reader.nextInt();
				Order order = new Order(orderId);
				synchronized (op) {
					ordersFiles.put(order.getId(), order);
				}
				while (reader.hasNextLine()) {
					try {
						Product productToAdd = op.getSpecificProduct(reader.next());
						reader.nextLine();
						synchronized(op) {
							productToAdd.incrementTotalNumOrder(1);
						}
						order.addProduct(productToAdd);
					} catch (IllegalArgumentException e) {
						System.err.print("An order requested a product that " + "didn't exist.");
					}

				}
				reader.close();
			} catch (FileNotFoundException e1) {
				// Stops looping if files to process is greater than actual
				// file number.
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			OrdersProcessor op = new OrdersProcessor();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
