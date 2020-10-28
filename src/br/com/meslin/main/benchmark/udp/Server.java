/**
 * From: https://www.codejava.net/java-se/networking/java-udp-client-server-program-example
 * This app was designed to be used with Server. They send and receive a custom data containing a caption string and an image file
 * The Client send a bunch of custom data and calculate the elapsed time
 * This app also includes:
 * 1) It sends a message containing a text string and an image
 * 2) The image source location can be informed at the command line 
 * 
 * This App only works with very-very-small image files
 */
package br.com.meslin.main.benchmark.udp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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
 * This program demonstrates how to implement a UDP server program.
 *
 *
 * @author www.codejava.net
 */
public class Server {
	private static final int DATAGRAM_SIZE = 1 * 1000 * 1000;
	private DatagramSocket socket;
	private static int			gatewayPort;

	public Server(int port) throws SocketException {
		socket = new DatagramSocket(port);
	}

	public static void main(String[] args) {
		parseCommandLine(args);
		try {
			Server server = new Server(gatewayPort);
			System.out.println("Server ready at port " + gatewayPort);
			server.service();
		} catch (SocketException ex) {
			System.out.println("Socket error: " + ex.getMessage());
		} catch (IOException ex) {
			System.out.println("I/O error: " + ex.getMessage());
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

		// -f image filename
		option = new Option("f", "image", true, "Full path of an image filename");
		option.setRequired(false);
		options.addOption(option);

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
			formatter.printHelp("HelloCoreClient", options);
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
			formatter.printHelp("HelloCoreClient", options);
			System.exit(0);
		}
	}

	private void service() throws IOException {
		while (true) {
			byte[] buffer = new byte[DATAGRAM_SIZE];
			DatagramPacket request = new DatagramPacket(buffer, buffer.length);
			socket.receive(request);
			CustomData customData = (CustomData) toObject(buffer);

			InetAddress clientAddress = request.getAddress();
			int clientPort = request.getPort();

			buffer = customData.getCaption().getBytes();
			DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
			socket.send(response);
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