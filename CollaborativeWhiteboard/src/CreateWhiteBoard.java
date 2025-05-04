/**
* Author: Matthias Si En Ong
* Student Id: 1590392
* Email: matthiaso@student.unimelb.edu.au
*/
import java.awt.EventQueue;

import WhiteboardApp.MainWindow;

/**
 * This class contains the entry point of the Dictionary Client.
 * @version 1.0
 * @author Matthias Si En Ong
*/
public class CreateWhiteBoard {
	/**
     * The entry point of the Dictionary Client.
     *
     * @param args Command line arguments, it should be in the order server-address, server-port.
     */
	public static void main(String[] args) {
		// Run this on Event dispatch thread, Swing code is run on the same thread.
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow(args);
					System.out.println("Create Whiteboard!");
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
