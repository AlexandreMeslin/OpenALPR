/**
 * From: http://wiki.lac.inf.puc-rio.br/doku.php?id=hellocore
 * This app was designed to be used with HelloCoreServer. They send and receive a custom data containing a caption string and an image file
 * The HelloCoreClient send a bunch of custom data and calculate the elapsed time
 * This app also includes:
 * 1) It sends a message containing a text string and an image
 * 2) The image source location can be informed at the command line 
 */
package br.com.meslin.main.benchmark.mrudp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import br.com.meslin.alpr.aux.connection.Constants;
import br.com.meslin.lac.CustomData;
import lac.cnclib.net.NodeConnection;
import lac.cnclib.net.NodeConnectionListener;
import lac.cnclib.net.mrudp.MrUdpNodeConnection;
import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.message.Message;

public class HelloCoreClient implements NodeConnectionListener {
	private static String		gatewayIP;
	private static int			gatewayPort;
	private MrUdpNodeConnection	connection;
	private static UUID			myUUID;
	
	/** image filename */
	private static String imageName;
	/** number of iteration */
	private static int nIterations;
	/** transmission start time */
	private static volatile Vector<Date> startTime;
	/** transmission end time */
	private static volatile Vector<Date> endTime;

	public HelloCoreClient() {
		// From now on, UUID is set on main method
		//myUUID = UUID.fromString("788b2b22-baa6-4c61-b1bb-01cff1f5f878");
		System.err.println("Trying to connect to " + gatewayIP + ":" + gatewayPort);
		InetSocketAddress address = new InetSocketAddress(gatewayIP, gatewayPort);
		try {
			connection = new MrUdpNodeConnection(myUUID);
			connection.addNodeConnectionListener(this);
			connection.connect(address);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Logger.getLogger("").setLevel(Level.OFF);
		
		parseCommandLine(args);
		System.err.println(
				"Server address: " + gatewayIP + "\n" +
				"Server port: " + gatewayPort +  "\n" +
				"Image filename: " + imageName + "\n" +
				"Iterations: " + nIterations + "\n" 
			);

		// let the command-line parser set the UUID
		//myUUID = UUID.fromString("788b2b22-baa6-4c61-b1bb-01cff1f5f878");
		
		// send the message
		HelloCoreClient sender = new HelloCoreClient();
		
		startTime = new Vector<Date>(nIterations);
		endTime = new Vector<Date>(nIterations);
		for(int i=0; i<nIterations; i++) {
			sender.sendPicture(Integer.toString(i) , imageName);
			try {
				Thread.sleep(2000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		boolean ready = false;
		int elapsedTime = 0;
		int nTime = 0;
		while(!ready) {
			ready = true;
			elapsedTime = 0;
			nTime = 0;
			for(int i=0; i<nIterations; i++) {
				try {
					if(startTime.get(i) != null && endTime.get(i) != null) {
						elapsedTime += endTime.get(i).getTime() - startTime.get(i).getTime();
						nTime++;
					}
					else {
						System.err.println("Missing " + i + " because " + endTime.get(i));
						ready = false;
					}
				}
				catch(Exception e) {
					ready = false;
				}
			}
			if(nTime != 0) {
				System.err.println("Xfer average time = " + (elapsedTime/nTime) + " ms (" + nTime + ")");
			}
			else {
   				System.err.println("No elapsed time to show (?!?!?)");
			}
			try {
				Thread.sleep(2 * 1000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println(nIterations + "," + elapsedTime + "," + nTime + "," + imageName);
		for(int i=0; i<nIterations; i++) {
			System.out.println(i + "," + startTime.get(i).getTime() + "," + endTime.get(i).getTime());
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
		
		// -a ContextNet gateway IP address
		option = new Option("a", "address", true, "ContextNet Gateway IP address");
		option.setRequired(false);
		options.addOption(option);

		// -f image filename
		option = new Option("f", "image", true, "Full path of an image filename");
		option.setRequired(false);
		options.addOption(option);
		
		// -h help
		option = new Option("h", "help", false, "Print help");
		option.setRequired(false);
		options.addOption(option);
		
		// -i number of interactions
		option = new Option("i", "iterations", true, "Number of iterations");
		option.setRequired(false);
		options.addOption(option);
		
		// -p Contextnet gateway TCP port
		option = new Option("p", "port", true, "ContextNet Gateway IP port number");
		option.setRequired(false);
		options.addOption(option);
		
		// -u source UUID
		option = new Option("u", "uuid", true, "Source UUID");
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
		
		// ContextNet gateway IP address
		if((gatewayIP = cmd.getOptionValue("address")) == null) {
			gatewayIP = Constants.GATEWAY_IP;
		}
		// ContextNet TCP port number
		try {
			gatewayPort = Integer.parseInt(cmd.getOptionValue("port"));
		} catch(Exception e) {
			gatewayPort = Constants.GATEWAY_PORT;
		}
		// Image filename
		if((imageName = cmd.getOptionValue("image")) == null) {
			imageName = "/media/meslin/DA32492932490BC7/Users/meslin/Google Drive/workspace-desktop-ubuntu/OpenALPRSample/src/rio.jpg";
		}
		// UUID
		try {
			myUUID = UUID.fromString(cmd.getOptionValue("uuid"));
		}
		catch(Exception e) {
			myUUID = UUID.fromString("788b2b22-baa6-4c61-b1bb-01cff1f5f878");
		}
		if(cmd.getOptionValue("help") != null) {
			formatter.printHelp("HelloCoreClient", options);
			System.exit(0);
		}
		// Number of iterations
		try {
			nIterations = Integer.parseInt(cmd.getOptionValue("iterations"));
		} catch(Exception e) {
			nIterations = 10;
		}
	}
	
	/**
	 * Sends a message to the core containing a caption string and an image
	 * @param caption
	 * @param imageName
	 */
	public void sendPicture(String caption, String imageName) {
		CustomData serializableContent = new CustomData(caption, imageName);
		ApplicationMessage message = new ApplicationMessage();
		message.setContentObject(serializableContent);
		System.err.println("SendPicture: " + message.getContentObject().getClass().getCanonicalName());
		// Recipient ID is just for peers?
//		message.setRecipientID(UUID.fromString("788b2b22-baa6-4c61-b1bb-01cff1f5f878"));

		try {
			System.err.println("Sending image + caption");
			startTime.add(Integer.parseInt(caption), new Date());
			connection.sendMessage(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void connected(NodeConnection remoteCon) {
		ApplicationMessage message = new ApplicationMessage();
		message.setContentObject("Registering");
		try {
			connection.sendMessage(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void newMessageReceived(NodeConnection remoteCon, Message message) {
		Date date = new Date();

		String caption = message.getContentObject().toString();
		if(caption.startsWith("CustomData")) {
			endTime.add(Integer.parseInt(caption.substring("CustomData".length())), date);
		}
		System.err.println("Sender received the message!!");
		System.err.println(message.getContentObject());
	}

	// other methods

	@Override
	public void reconnected(NodeConnection remoteCon, SocketAddress endPoint, boolean wasHandover, boolean wasMandatory) {}

	@Override
	public void disconnected(NodeConnection remoteCon) {}

	@Override
	public void unsentMessages(NodeConnection remoteCon, List<Message> unsentMessages) {}

	@Override
	public void internalException(NodeConnection remoteCon, Exception e) {}
}