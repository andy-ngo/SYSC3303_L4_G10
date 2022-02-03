import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFileChooser;

/**
 * 
 */

/**
 * @author Ali Fahd
 *
 */
public class FloorSubsytem implements Runnable {
//	private Scheduler s;
	private static ArrayList<FloorRequest> requests = new ArrayList<FloorRequest>();
	private Map<Integer, Boolean> arrivalSensors = new HashMap<>();
	
//	public FloorSubsystem (Scheduler s) {
//		this.s = s;
//	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		addFloorRequest();
	}
	
	/**
	 * Parses through a file with a list of requests from the floor and creates a
	 * list of FloorRequest objects
	 *
	 * @param fileLocation location of the file
	 */
	public static void addFloorRequest() {
		JFileChooser fileChooser = new JFileChooser(); 
		
		int response = fileChooser.showOpenDialog(null);
		
		if(response == JFileChooser.APPROVE_OPTION) {
			File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
			try (Scanner scan = new Scanner(file)){
				while(scan.hasNextLine()) {
					String[] requestString = scan.nextLine().split(" ");
					requests.add(new FloorRequest(requestString[0], Integer.parseInt(requestString[1]), requestString[2], Integer.parseInt(requestString[3])));
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		for(FloorRequest fr: requests) {
			System.out.println(fr.toString());
		}		
	}
	
	public ArrayList<FloorRequest> getRequests() {
		return requests;
	}
	
	public FloorRequest getRequest(int n) {
		return requests.get(n);
	}
	
	public void clearRequests() {
		requests.clear();
	}
	
	public void deleteRequest(int n) {
		requests.remove(n);
	}
	
	public void setArrivalSensor(int floor, boolean on) {
		// Turn on/off arrival sensor
		arrivalSensors.put(floor, on);
	}

	@Override
	public void run() {
	}
}
