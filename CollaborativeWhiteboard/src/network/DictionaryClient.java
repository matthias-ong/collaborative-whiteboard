/**
* Author: Matthias Si En Ong
* Student Id: 1590392
* Email: matthiaso@student.unimelb.edu.au
*/
package network;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.swing.JTextArea;

import constants.Constants;

/**
 * This class contains the main Dictionary Client back-end logic, it interfaces with
 * the network components like TCP to communicate with the Client.
 * @version 1.0
 * @author Matthias Si En Ong
*/
public class DictionaryClient {

	private BufferedReader in;
	private BufferedWriter out;
	private Socket socket = null;
	private Scanner scanner = null;
	private Boolean status = false;
	private int port;
	private String address;

	/**
     * The constructor of the DictionaryClient, it validates the command arguments.
     *
     * @param args Command line arguments, it should be in the order server-address, server-port.
     */
	public DictionaryClient(String[] args) {
		if (!isValidArgs(args)) {
			System.exit(1);
		}
	}

	/**
     * This function contains the Retry mechanism and connection attempt.
     *
     * @param textArea JTextArea to output any messages from the connection.
     */
	public void attemptConnection(JTextArea textArea) {
		int attempts = 0; // retry mechanism
		this.status = false;
		while (attempts < 3 && !this.status) {
			try {
				if (Thread.currentThread().isInterrupted()) {
	                System.out.println("Connection attempt interrupted.");
	                break;
	            }
				// Create a stream socket bounded to any port and connect it to the socket
				socket = new Socket(this.address, this.port);
				System.out.println(Constants.CONNECT_SUCCESS);

				// Get the input/output streams for reading/writing data from/to the socket
				this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
				scanner = new Scanner(System.in);
				this.status = true;
			} catch (UnknownHostException e) {
				System.out.println("Error: Connection Failed - Unknown Host.");
				attempts++;
			} catch (IOException e) {
				System.out.println(Constants.ERROR_CONN_IOEXCEPT);
				attempts++;
			}

			// wait a while before retrying
			if (attempts < 3 && !this.status) {
				try {
					textArea.setText("Retrying connection... Attempt " + (attempts + 1));
					System.out.println("Retrying connection... Attempt " + (attempts + 1));
					Thread.sleep(2000); // 2 second pause
				} catch (InterruptedException e) {
					System.out.println("Retry interrupted.");
					break; // stop running thread
				}
			}
		}
	}

	// This function cleans up any resources used by DictionaryClient
	public void cleanup() {
		
	    this.status = false;
		if (this.scanner != null) {
			scanner.close();
		}
		this.status = false;
		// Close the socket
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

//	private void displayMenu() {
//		System.out.println("\nSelect an operation:");
//		System.out.println("1. Query Word");
//        System.out.println("2. Add Word");
//        System.out.println("3. Add Meaning");
//        System.out.println("4. Delete Word");
//        System.out.println("5. Edit Meaning");
//		System.out.println("6. Exit");
//		System.out.print("Enter choice (1-6): ");
//	}
//
//	private void runMainLoop(Scanner scanner) {
//		Boolean endLoop = false;
//		while (endLoop == false) {
//			displayMenu();
//			try {
//				int option  = Integer.parseInt(scanner.nextLine());
//				switch (option) {
//                	case 1:
//                		queryWord(scanner);
//                		break;
//                	case 6:
//                		endLoop = true;
//                		break;
//				}
//			}
//			catch (Exception e) {
//                System.out.printf("Invalid Input.");
//            }
//		}
//	}

	// Function to query a word
	public String queryWord(String word) {
		StringBuilder strBuilder = new StringBuilder("");
		if (this.status == false) {
			strBuilder.append(Constants.ERROR_CONN_FAILED);
		} else {
			try {
				this.out.write("QUERY " + word); // Send request to server
				this.out.newLine();
				this.out.flush();

				String responseLine;
				while (!(responseLine = this.in.readLine()).equals("DONE")) {
					strBuilder.append(responseLine + "\n");
				}

			} catch (IOException e) {
				strBuilder.append(Constants.ERROR_CONN_IOEXCEPT);
			}
		}
		return strBuilder.toString();

	}

	// Function to add a word
	public String addWord(String word, String meaning) {
		StringBuilder strBuilder = new StringBuilder("");
		if (this.status == false) {
			strBuilder.append(Constants.ERROR_CONN_FAILED);
		} else {
			try {
				this.out.write("ADDWORD " + word + " " + meaning); // Send request to server
				this.out.newLine();
//				this.out.write("ENDSEND");
//				this.out.newLine();
				this.out.flush();

				String responseLine;
				while (!(responseLine = this.in.readLine()).equals("DONE")) {
					strBuilder.append(responseLine + "\n");
				}

			} catch (IOException e) {
				strBuilder.append(Constants.ERROR_CONN_IOEXCEPT);
			}
		}
		return strBuilder.toString();
	}

	// Function to add a word
	public String addMeaning(String word, String meaning) {
		StringBuilder strBuilder = new StringBuilder("");
		if (this.status == false) {
			strBuilder.append(Constants.ERROR_CONN_FAILED);
		} else {
			try {
				this.out.write("ADDMEANING " + word + " " + meaning); // Send request to server
				this.out.newLine();
				this.out.flush();

				String responseLine;
				while (!(responseLine = this.in.readLine()).equals("DONE")) {
					strBuilder.append(responseLine + "\n");
				}

			} catch (IOException e) {
				strBuilder.append(Constants.ERROR_CONN_IOEXCEPT);
			}
		}
		return strBuilder.toString();
	}

	// Function to delete a word
	public String deleteWord(String word) {
		StringBuilder strBuilder = new StringBuilder("");
		if (this.status == false) {
			strBuilder.append(Constants.ERROR_CONN_FAILED);
		} else {
			try {
				this.out.write("DELETE " + word); // Send request to server
				this.out.newLine();
				this.out.flush();

				String responseLine;
				while (!(responseLine = this.in.readLine()).equals("DONE")) {
					strBuilder.append(responseLine + "\n");
				}

			} catch (IOException e) {
				strBuilder.append(Constants.ERROR_CONN_IOEXCEPT);
			}
		}
		return strBuilder.toString();
	}

	// Function to update a word's meaning
	public String updateMeaning(String word, int meaningIdx, String meaning) {
		StringBuilder strBuilder = new StringBuilder("");
		if (this.status == false) {
			strBuilder.append(Constants.ERROR_CONN_FAILED);
		} else {
			try {
				this.out.write("UPDATEMEANING " + word + " " + meaningIdx + " " + meaning); // Send request to server
				this.out.newLine();
				this.out.flush();

				String responseLine;
				while (!(responseLine = this.in.readLine()).equals("DONE")) {
					strBuilder.append(responseLine + "\n");
				}

			} catch (IOException e) {
				strBuilder.append(Constants.ERROR_CONN_IOEXCEPT);
			}
		}
		return strBuilder.toString();
	}

	// Function to save entire dictionary
	public String saveEntireDictionary() {
		String result = "";
		if (this.status == false) {
			result = Constants.ERROR_CONN_FAILED;
		} else {
			try {
				this.out.write("SAVE"); // Send request to server
				this.out.newLine();
				this.out.flush();

				if (this.in.readLine().equals("DONE")) {
					result = "Saved successfully!";
				}

			} catch (IOException e) {
				result = Constants.ERROR_CONN_IOEXCEPT;
			}
		}

		return result;
	}

	// Function to display instructions
	public String displayInstructions() {
		StringBuilder strBuilder = new StringBuilder("");
		strBuilder.append("Expected Inputs:\n");
		strBuilder.append("1. Query Word: <word>\n");
		strBuilder.append("2. Add Word: <word>\n");
		strBuilder.append("3. Delete Word: <word>\n");
		strBuilder.append("4. Add Meaning: <word> <meaning>\n");
		strBuilder.append("5. Update Meaning: <word> <meaning index> <new meaning>\n");
		strBuilder.append("6. Save changes to server's JSON File Inputs: None\n");
		return strBuilder.toString();
	}

	// Function to validate arguments
	private Boolean isValidArgs(String[] args) {
		if (args.length != 2) {
			System.out.println("java -jar DictionaryClient.jar <server-address> <server-port>");
			return false;
		}
		try {
			this.port = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.out.println("Invalid Port Number");
			return false;
		}
		this.address = args[0];
		return true;
	}

	// Getter function to check status of function
	public Boolean getStatus() {
		return this.status;
	}
}
