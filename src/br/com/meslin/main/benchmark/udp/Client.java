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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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

/**
 * This program demonstrates how to implement a UDP client program.
 *
 *
 * @author www.codejava.net
 */
public class Client {
	private static String		gatewayIP;
	private static int			gatewayPort;
	
	/** image filename */
	private static String imageName;
	/** number of iteration */
	private static int nIterations = 10;
	/** transmission start time */
	private static volatile Vector<Date> startTime;
	/** transmission end time */
	private static volatile Vector<Date> endTime;
 
    public static void main(String[] args) {
    	parseCommandLine(args);
    	
        try {
            InetAddress address = InetAddress.getByName(gatewayIP);
			DatagramSocket socket = new DatagramSocket();
 
			startTime = new Vector<Date>(nIterations);
			endTime = new Vector<Date>(nIterations);
            for(int i=0; i<nIterations; i++) {
                DatagramPacket request;
                byte[] bytes = toByte(new CustomData("CustomData" + Integer.toString(i), imageName));
                request = new DatagramPacket(bytes, bytes.length, address, gatewayPort);
    			startTime.add(i, new Date());
                socket.send(request);
 
                byte[] buffer = new byte[512];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                socket.receive(response);
        		Date date = new Date();		// when the xfer ends
                String quote = new String(buffer, 0, response.getLength());
                
                // calculate and store the end time
        		if(quote.startsWith("CustomData")) {
        			endTime.add(Integer.parseInt(quote.substring("CustomData".length())), date);
        		}
 
                System.out.println(quote);
                System.out.println();
            }
            socket.close();

   			int elapsedTime = 0;
   			int nTime = 0;
   			for(int i=0; i<nIterations; i++) {
   				try {
					elapsedTime += endTime.get(i).getTime() - startTime.get(i).getTime();
					nTime++;
   				}
   				catch(Exception e) {
   					// do nothing if there is an exception here
   				}
   			}
   			if(nTime != 0) {
   				System.out.println("Xfer average time = " + (elapsedTime/nTime) + " ms (" + nTime + ")");
   			}
   			else {
   				System.out.println("No elapsed time to show (?!?!?)");
   			}
 
        } catch (IOException e) {
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
		if(cmd.getOptionValue("help") != null) {
			formatter.printHelp("Client", options);
			System.exit(0);
		}
		// Number of iterations
		try {
			nIterations = Integer.parseInt(cmd.getOptionValue("iterations"));
		} catch(Exception e) {
			//nIterations = 10;
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