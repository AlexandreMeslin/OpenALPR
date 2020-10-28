/**
 * From: https://www.codejava.net/java-se/networking/java-socket-server-examples-tcp-ip
 * This app was designed to be used with Server. They send and receive a custom data containing a caption string and an image file
 * The Client send a bunch of custom data and calculate the elapsed time
 * This app also includes:
 * 1) It sends a message containing a text string and an image
 * 2) The image source location can be informed at the command line 
 */
package br.com.meslin.main.benchmark.tcp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Vector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import br.com.meslin.alpr.aux.connection.Constants;
import br.com.meslin.lac.CustomData;

public class ClientTCP {
	private static String		gatewayIP;
	private static int			gatewayPort;

	
	/** image filename */
	private static String imageName;
	/** number of iteration */
	private static int nIterations;
	/** transmission start time */
	private static volatile Vector<Date> startTime;
	/** transmission end time */
	private static volatile Vector<Date> endTime;
	
	public ClientTCP() {
	}

	public static void main(String[] args) {
		parseCommandLine(args);
		System.err.println(
			"Server address: " + gatewayIP + "\n" +
			"Server port: " + gatewayPort +  "\n" +
			"Image filename: " + imageName + "\n" +
			"Iterations: " + nIterations + "\n" 
		);
		
		startTime = new Vector<Date>(nIterations);
		endTime = new Vector<Date>(nIterations);
		for(int i=0; i<nIterations; i++) {
			startTime.add(i, null);
			endTime.add(i, null);
		}
		
		Thread server = new Thread(new Runnable() {
			@Override
			public void run() {
				ServerSocket serverSocket;
				try {
					serverSocket = new ServerSocket(gatewayPort +1);
					System.err.println("Server is listening on port " + serverSocket.getLocalPort());

					while(true) {
						Socket socket = serverSocket.accept();
						System.err.println("New client connected");
						// Receive answer
						byte[] buffer = new byte[1000];
						InputStream is = socket.getInputStream();
						int qtd = is.read(buffer);
						socket.close();
						Date date = new Date();

						String quote = new String(buffer, 0, qtd);
						System.err.println("data received: " + quote);
						if(quote.startsWith("CustomData")) {
							endTime.add(Integer.parseInt(quote.substring("CustomData".length())), date);
						}
					}
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		server.start();
		
		try {
			for(int i=0; i<nIterations; i++) {
				System.err.println("Sending iteration #" +  i);
				// generate data do send
				CustomData customData = new CustomData("CustomData" + i, imageName);
				// make a TCP connection
				Socket socket = new Socket(InetAddress.getByName(gatewayIP), gatewayPort);
				OutputStream os = socket.getOutputStream();
				// Send data
    			startTime.add(i, new Date());
				os.write(toByte(customData));
				System.err.println("data sent");
				socket.close();
				Thread.sleep(1000); 	// There is no *real* reason to sleep, but it's necessary to count the endTime correctly
			}

			boolean ready = false;
			while(!ready) {
				ready = true;
				int elapsedTime = 0;
	   			int nTime = 0;
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
	   				System.out.println(nIterations + "," + elapsedTime + "," + nTime + "," + imageName);
	   			}
	   			else {
	   				System.err.println("No elapsed time to show (?!?!?)");
	   			}
   				Thread.sleep(2 * 1000);
			}
			for(int i=0; i<nIterations; i++) {
				System.out.println(i + "," + startTime.get(i).getTime() + "," + endTime.get(i).getTime());
			}
		}
		catch (IOException | InterruptedException e) {
			e.printStackTrace();
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
		if(cmd.getOptionValue("help") != null) {
			formatter.printHelp("Client", options);
			System.exit(0);
		}
		// Number of iterations
		try {
			nIterations = Integer.parseInt(cmd.getOptionValue("iterations"));
		} catch(Exception e) {
			nIterations = 1;
		}
	}
    
    /**
     * Converts a Serializable object to byte array<br>
     * From: https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
     * @param object
     * @return the object as a byte array
     */
    private static byte[] toByte(Serializable object) {
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	ObjectOutputStream out = null;
    	byte[] yourBytes = null;
    	try {
    	  out = new ObjectOutputStream(bos);   
    	  out.writeObject(object);
    	  out.flush();
    	  yourBytes = bos.toByteArray();
    	}
		catch (IOException e) {
			e.printStackTrace();
		} finally {
    	  try {
    	    bos.close();
    	  } catch (IOException ex) {
    	    // ignore close exception
    	  }
    	}
    	return yourBytes;
    }
}
