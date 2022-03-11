import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.swing.JFileChooser;
import java.sql.Timestamp;
import java.util.Date;
import java.io.*;
import java.net.*;

/**
 * Class FloorSubsystem imports request data from an input file and puts it in scheduler class.
 * @author Ali Fahd
 *
 */
public class FloorSubsytem implements Runnable {
	private static Scheduler scheduler;
	private static ArrayList<FloorRequest> requests = new ArrayList<FloorRequest>();	//list of requests
	private Map<Integer, Boolean> arrivalSensors = new HashMap<>();	// keeps track of arrival sensors
	private DatagramPacket sendPacket,receivePacket;
	private DatagramSocket sendReceiveSocket;

	/**
	 * Constructor for class FloorSubsystem. Initializes scheduler.
	 * @param Scheduler scheduler
	 *
	 */
	public FloorSubsytem (Scheduler scheduler) {
		this.scheduler = scheduler;	//initialize scheduler
		
		try
		{
			sendReceiveSocket = new DatagramSocket();
		} catch(SocketException se)
		{
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	public void initailize()
	{
		byte[] sendData = new byte[100];
		
		try 
		{
			this.sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(),23);
		} catch (UnknownHostException e) 
		{ 
			e.printStackTrace();
			System.exit(1);
		}
		
		try 
		{
			this.sendReceiveSocket.send(this.sendPacket);;
		} catch (IOException e) 
		{ 
			e.printStackTrace();
			System.exit(1);
		}
		
		byte[] dataByte = new byte[100];
		receivePacket = new DatagramPacket(dataByte, dataByte.length);
		try
		{
			sendReceiveSocket.receive(receivePacket);
		} catch(IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Parses through a file with a list of requests from the floor and creates a
	 * list of FloorRequest objects
	 *
	 * @param String fileLocation location of the file
	 */
	public static void addFloorRequest(String filename) {
		File file = new File(filename);
		//scans file and loops through each line (each request)
		try (Scanner scan = new Scanner(file)){
			while(scan.hasNextLine()) {
				String[] requestString = scan.nextLine().split(" ");	//breaks up data in each request
				requests.add(new FloorRequest(requestString[0], Integer.parseInt(requestString[1]), requestString[2], Integer.parseInt(requestString[3]))); // creates FloorRequest object and adds it to requests
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        Date date = new Date();
		//outputs each request imported
		for(FloorRequest fr: requests) {
			System.out.println(new Timestamp(date.getTime()) + " FLOOR SUBSYSTEM: " + fr.toString());
		}		
		System.out.println("\n");

		for(FloorRequest fr: requests) {
			scheduler.putRequest(fr);	//puts request data in scheduler
			System.out.println("FLOOR SUBSYSTEM: Request issued to scheduler.\n");
		}
	}
	
	/**
	 * Gets all requests in FloorSubsytem.
	 *
	 * @return ArrayList<FloorRequest> requests in FloorSubsystem
	 */
	public ArrayList<FloorRequest> getRequests() {
		return requests;
	}
	
	/**
	 * Gets a request at a specified index in FloorSubsytem.
	 *
	 * @return FloorRequest request in FloorSubsystem
	 */
	public FloorRequest getRequest(int n) {
		return requests.get(n);
	}
	
	/**
	 * Removes all requests in FloorSubsytem.
	 */
	public void clearRequests() {
		requests.clear();
	}
	
	/**
	 * Removes a request at a specific index in FloorSubsytem.
	 * 
	 * @param int n the index of which request to be removed
	 */
	public void deleteRequest(int n) {
		requests.remove(n);
	}
	
	/**
	 * Set the value of a arrival sensor. Turn on/off arrival sensor
	 * 
	 * @param int floor the floor of which request to set the arrival sensor
	 * @param boolean on whether the sensor is on or off
	 */
	public void setArrivalSensor(int floor, boolean on) {
		arrivalSensors.put(floor, on);	// changes/initialized hash map value
	}
	
	/**
	 * Gets all arrival sensors in FloorSubsytem.
	 *
	 * @return Map<Integer, Boolean> arrivalSensors in FloorSubsystem
	 */
	public Map<Integer, Boolean> getArrivalSensors() {
		return arrivalSensors;
	}

	/**
	 * Runs the thread for FloorSubsytem.
	 * Puts request data into scheduler and gets arrival sensor data from scheduler.
	 */
	@Override
	public void run() {
		JFileChooser fileChooser = new JFileChooser(); 	//	opens file browser to select request data file
		
		int response = fileChooser.showOpenDialog(null);
		
		if(response == JFileChooser.APPROVE_OPTION) {
			addFloorRequest(fileChooser.getSelectedFile().getAbsolutePath());	//sends file chosen to addFloorRequest method
		}
		while(true)
		{
			synchronized(scheduler)
			{
				String[] temp = scheduler.getArrivalSensor();	//retrieves arrival sensor data from scheduler
                arrivalSensors.put(Integer.parseInt(temp[0]), Boolean.parseBoolean(temp[1]));	//updates hash map
                scheduler.notifyAll();
			}
		}
	}
}
