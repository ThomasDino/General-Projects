// Pace University
// Fall 2022
// Operating Systems/Architecture
//
// Course: CS 371
// Team Authors: Thomas Dinopoulos
// Collaborators: none
// References: Java, CS371 Lecture slides and provided code
//
// Assignment: 1
// Description: Synchronization between threads by example of burger
//
// Part 1. Question 4. Upon Running  the CookServerSafeWindow.java a lot of times the total number of burgers cooked,
// was consistently equal to the number of burgers served.
//
// I increased the variables for NUM_COOKS, COOK_MAXRUN, NUM_SERVERS and WINDOW_SIZE by increments
// of 10 with a limit of 100, and CookServerSafeWindow.java provided equal number of burgers cooked  
// and served for any combination of those variables
// 
// Part 2 Question 3. 
// When executing, the output shows that the number of burgers cooked is equal to the number of burgers served
//
// Extra Credit:
// When Comparing the output of the safe and unsafe version, the latter consistently produced less burgers cooked 
// and served than the safe. To test this I ran the code with the same values for the variables NUM_COOKS, COOK_MAXRUN, 
// NUM_SERVERS and WINDOW_SIZE.
package CS371;

import java.util.LinkedList;
import java.util.Queue;

public class CookServerSafeWindow {
	// DO NOT MODIFY THIS CLASS.
	public static class Tray {
		public String cook;
		public int numBurgers;
		
		// A Tray class represents a tray of cooked burgers with the cook's name and the number of burgers.
		public Tray(String cook, int numBurgers) {
			this.cook = cook;
			this.numBurgers = numBurgers;
		}
	}
	
	// SafeWindow class represents a synchronized window for managing cooked burger trays.
	public static class SafeWindow {
		private Queue<Tray> window = new LinkedList<Tray>();
		private int maxSize;
		private int cooked;
		private int served;
		
		public SafeWindow(int maxSize) {
			this.maxSize = maxSize;
			this.cooked = 0;
			this.served = 0;
		}
		
		// Method to add a tray to the window.
		public boolean add(Tray d) {
		    synchronized (this) {
		        if (window.size() < maxSize) {
		            window.add(d);
		            // counting the number of trays
		            cooked++; // Increment the cooked tray counter.
		            System.out.println("Tray added by Cook: " + d.cook + " Tray's currently on window: " + window.size());
		            return true;
		        } else {
		            System.out.println("Window is currently full " + d.cook + " has to wait.");
		            return false;
		        }
		    }
		}

		
		// Method to remove a tray from the window.
		public Tray remove() {
		    synchronized (this) {
		        if (!window.isEmpty()) {
		            Tray tray = window.poll();
		            served++; // Increment the served tray counter.
		            System.out.println("Removed Tray by Server: " + tray.cook + " Value: " + tray.numBurgers + " Size: " + window.size());
		            return tray;
		        } else {
		            System.out.println("Window is empty. Server " + Thread.currentThread().getName() + " has nothing to serve.");
		            return null;
		        }
		    }
		}

		
		public int getCooked() {
			return cooked;
		}
		
		public int getServed() {
			return served;
		}
		
		public int size() {
			return window.size();
		}
	}
	
	// Cook class represents a thread simulating a cook in the pizzeria.
	public static class Cook extends Thread {
		private String cookName;
		private SafeWindow window;
		private int maxRun;
		public Cook(String cookName, SafeWindow window, int maxRun) {
			this.cookName = cookName;
			this.window = window;
			this.maxRun = maxRun;
		}
		
		private Tray cook() {
			int numBurgers = (int)(Math.random()*5) + 1;
			System.out.println(">>> Cook: " + cookName + " Value: " + numBurgers + " Size: " + window.size());
			return new Tray(cookName, numBurgers);
		}
		
		public void run() {
			int i = 0; 
			Tray d = null;
			while (i < maxRun) {
				d = cook();
				window.add(d);
				i++;
				try {
					Thread.sleep((long)(100 * Math.random()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	// Server class represents a thread simulating a server in the pizzeria.
	public static class Server extends Thread {
		private String serverName;
		private SafeWindow window;
		private boolean canContinue;
		
		public Server(String serverName, SafeWindow window) {
			this.serverName = serverName;
			this.window = window;
			this.canContinue = true;
		}
		
		public synchronized boolean canContinue() {
			return this.canContinue;
		}
		
		public synchronized void stopRun() {
			this.canContinue = false;
		}
		
		// Simulate serving a tray of burgers.
		private Tray serve() {
			Tray d = window.remove();
			if (d == null) {
				return null;
			}
			System.out.println("<<< Cook: " + d.cook + " Server: " + serverName + " Value: " + d.numBurgers + " Size: " + window.size());
			return d;
		}
		
		public void run() {
			while (canContinue()) {	
				serve();
				try {
					Thread.sleep((long)(100 * Math.random()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
	
		int NUM_COOKS = 5;
		int COOK_MAXRUN = 10;
		int NUM_SERVERS = 5;
		int WINDOW_SIZE = 3;
		
		// Create a SafeWindow for managing burger trays.
		SafeWindow window = new SafeWindow(WINDOW_SIZE);
		Cook[] cooks = new Cook[NUM_COOKS];
		Server[] servers = new Server[NUM_SERVERS];
		
		// Start Cook threads.
		for (int i = 0; i < NUM_COOKS; i++) {
			cooks[i] = new Cook("p" + i, window, COOK_MAXRUN);
			cooks[i].start();
		}
		
		// Start Server threads.
		for (int i = 0; i < NUM_SERVERS; i++) {
			servers[i] = new Server("c" + i, window);
			servers[i].start();
		}
		
		// Wait for Cook threads to finish.
		for (int i = 0; i < NUM_COOKS; i++) {
			cooks[i].join();
		}
		
		// Stop Server threads
		for (int i = 0; i < NUM_SERVERS; i++) {
			servers[i].stopRun();
		}
		
		System.out.println(window.getCooked() + " " + window.getServed());
		System.exit(0);
	}
}
