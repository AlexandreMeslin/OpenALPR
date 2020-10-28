/**
 * From: https://www.codejava.net/java-se/networking/java-socket-server-examples-tcp-ip
 * This app was designed to be used with Server. They send and receive a custom data containing a caption string and an image file
 * The Client send a bunch of custom data and calculate the elapsed time
 * This app also includes:
 * 1) It sends a message containing a text string and an image
 * 2) The image source location can be informed at the command line 
 */
package br.com.meslin.main.benchmark.tcp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import br.com.meslin.alpr.aux.connection.Constants;
import br.com.meslin.lac.CustomData;

/**
 * This program demonstrates a simple TCP/IP socket server that echoes every
 * message from the client in reversed form.
 * This server is multi-threaded.
 *
 * @author www.codejava.net
 */
public class ServerTCP {
	private static int			gatewayPort;

	/**
	 * the main function
	 * @param args
	 */
	public static void main(String[] args) {
		parseCommandLine(args);

		try (ServerSocket serverSocket = new ServerSocket(gatewayPort)) {
			System.out.println("Server is listening on port " + gatewayPort);

			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("New client connected");

				new ServerThread(socket, gatewayPort).start();
			}

		} catch (IOException ex) {
			System.out.println("Server exception: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Parses the command-line options
	 * @param args
	 */
	private static void parseCommandLine(String[] args) {
		// get command line options
		Options options = new Options();
		Option option;

		// -h help
		option = new Option("h", "help", false, "Print help");
		option.setRequired(false);
		options.addOption(option);

		// -p Contextnet gateway TCP port
		option = new Option("p", "port", true, "ContextNet Gateway IP port number");
		option.setRequired(false);
		options.addOption(option);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		}
		catch(ParseException e) {
			System.err.println("Date = " + new Date());
			formatter.printHelp("Server", options);
			e.printStackTrace();
			System.exit(-1);
		}

		// ContextNet TCP port number
		try {
			gatewayPort = Integer.parseInt(cmd.getOptionValue("port"));
		} catch(Exception e) {
			gatewayPort = Constants.GATEWAY_PORT;
		}
		if(cmd.getOptionValue("help") != null) {
			formatter.printHelp("Server", options);
			System.exit(0);
		}
	}
}

/**
 * 
 * @author meslin
 *
 */
class ServerThread extends Thread {
	private Socket socket;
	private int gatewayPort;

	public ServerThread(Socket socket, int gatewayPort) {
		this.socket = socket;
		this.gatewayPort = gatewayPort;
	}

	public void run() {
		int count;
		try {
			InputStream input = socket.getInputStream();
			
			byte[] byteArray = new byte[10 * 1000 * 1000];
			byte[] buffer = new byte[0];
			while((count = input.read(byteArray)) > 0) {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				outputStream.write(buffer);
				outputStream.write(byteArray, 0, count);
				buffer = outputStream.toByteArray();
			}
			
			// Send the answer to the client
			// We are using port+1 to allow the client and the server app to run on the same computer
			InetAddress address = socket.getInetAddress();
			Socket clientSocket = new Socket(address, gatewayPort +1);
			OutputStream output = clientSocket.getOutputStream();
			CustomData customData = (CustomData) toObject(buffer);
			output.write(customData.getCaption().getBytes());
			System.out.println(customData);
			socket.close();
			clientSocket.close();
		} catch (IOException ex) {
			System.out.println("Server exception: " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	/**
	 * Convert an object serialized as byte array to the object<br>
	 * From: https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
	 * @param yourBytes
	 * @return
	 */
	private Object toObject(byte[] yourBytes) {
		ByteArrayInputStream bis = new ByteArrayInputStream(yourBytes);
		ObjectInput in = null;
		Object o = null;
		try {
			in = new ObjectInputStream(bis);
			o = in.readObject(); 
		}
		catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				// ignore close exception
			}
		}
		return o;
	}
}
