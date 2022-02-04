import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.swing.JFileChooser;

/**
 * @author Ali Fahd
 *
 */
public class FloorSubsytem implements Runnable {
	private Scheduler scheduler;
	private static ArrayList<FloorRequest> requests = new ArrayList<FloorRequest>();
	private Map<Integer, Boolean> arrivalSensors = new HashMap<>();
	
	public FloorSubsytem (Scheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	/**
	 * Parses through a file with a list of requests from the floor and creates a
	 * list of FloorRequest objects
	 *
	 * @param fileLocation location of the file
	 */
	public static void addFloorRequest(String filename) {
		File file = new File(filename);
		try (Scanner scan = new Scanner(file)){
			while(scan.hasNextLine()) {
				String[] requestString = scan.nextLine().split(" ");
				requests.add(new FloorRequest(requestString[0], Integer.parseInt(requestString[1]), requestString[2], Integer.parseInt(requestString[3])));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
	
	public Map<Integer, Boolean> getArrivalSensors() {
		return arrivalSensors;
	}

	@Override
	public void run() {
		JFileChooser fileChooser = new JFileChooser(); 
		
		int response = fileChooser.showOpenDialog(null);
		
		if(response == JFileChooser.APPROVE_OPTION) {
			addFloorRequest(fileChooser.getSelectedFile().getAbsolutePath());
		}
		while(true)
		{
			synchronized(scheduler)
			{
				scheduler.putRequests(requests);
			}
			synchronized(scheduler)
			{
				String[] temp = scheduler.getArrivalSensor();
                arrivalSensors.put(Integer.parseInt(temp[0]), Boolean.parseBoolean(temp[1]));
        		System.out.println(arrivalSensors);
                scheduler.notifyAll();
			}
		}
	}
}
