import java.io.*; // Import the File class
import java.net.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner; //Import this class to accept input

public class FloorSubsystem implements Runnable {

	private String data;
	private ArrayList<FloorRequest> requests;
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendReceiveSocket;
	private String wait = "waiting";
	private int lastRequest;
	private int numOfElevators = 0;
	public int requestCount = 0;
	private Map<Integer, Boolean[]> floorLamps;
	private Map<Integer, ArrayList<Boolean>> arrivalSensors;

	private static ReadPropertyFile r = new ReadPropertyFile();

	/**
	 * Instantiates all the variables and tries to find and read the input file
	 *
	 * @param file String that indicates the name and path of the input file
	 */
	public FloorSubsystem(String file) {
		requests = new ArrayList<FloorRequest>();
		floorLamps = new HashMap<Integer, Boolean[]>();
		arrivalSensors = new HashMap<Integer, ArrayList<Boolean>>();
		for (int i = 0; i < r.getNumFloors(); i++) {
			Boolean[] b = { false, false };
			floorLamps.put(i + 1, b);
		}
		for (int i = 0; i < r.getNumFloors(); i++) {
			ArrayList<Boolean> b = new ArrayList<>();
			for (int j = 0; j < r.getNumElevators(); j++) {
				b.add(false);
			}
			arrivalSensors.put(i + 1, b);
		}

		this.addFloorRequest(file);
		this.lastRequest = requests.get(requests.size() - 1).getFloorDestination();
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}

	public void Initialize() {
		byte[] toSend = new byte[100];
		try {
			this.sendPacket = new DatagramPacket(toSend, toSend.length, InetAddress.getLocalHost(), r.getFloorPort());
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

		try {
			this.sendReceiveSocket.send(this.sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		byte[] data = new byte[100];
		receivePacket = new DatagramPacket(data, data.length);
		try {
			sendReceiveSocket.receive(receivePacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		numOfElevators = Integer.parseInt(new String(receivePacket.getData(), 0, this.receivePacket.getLength()));
	}

	/**
	 * Parses through a file with a list of requests from the floor and creates a
	 * list of FloorRequest objects
	 *
	 * @param fileLocation location of the file
	 */
	public void addFloorRequest(String file) {
		long travelTime = 1L;
		long doorTime = 1L;

		try {
			File myObj = new File(file);
			Scanner s = new Scanner(myObj);

			while (s.hasNextLine()) {
				this.data = s.nextLine();
				FloorRequest request = new FloorRequest();
				String[] requestArray = this.data.split(" ");
				/*
				 * for(String x: requestArray){ System.out.println(x); }
				 */
				if (requestArray[0].equals("error")) {
					if (requestArray[1].equals("doorStuck")) {
						request.setFloorDestination(-1);
					} else if (requestArray[1].equals("floorStuck")) {
						request.setFloorDestination(-2);
					}
				} else {
					String direction = requestArray[2];
					Boolean[] currLampStatus = floorLamps.get(Integer.parseInt(requestArray[1]));
					ElevatorMotor requestDirection;
					if (direction.equals("Up")) {
						currLampStatus[0] = true;
						requestDirection = ElevatorMotor.Up;
					} else if (direction.equals("Down")) {
						requestDirection = ElevatorMotor.Down;
						currLampStatus[1] = true;
					} else {
						requestDirection = ElevatorMotor.Stop;
					}

					request = new FloorRequest(requestArray[0], Integer.parseInt(requestArray[1]), requestDirection, Integer.parseInt(requestArray[3]), travelTime, doorTime);
				}
				this.requests.add(request);
			}
			s.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found.");
			e.printStackTrace();
		}

	}

	// TODO: Turn Lamps, buttons etc on
	public void setLampsSensors(String floor, String elevator, boolean on) {
		// Turn on Arrival Sensor when elevator arrives at a floor
		ArrayList<Boolean> b = arrivalSensors.get(Integer.parseInt(floor));
		char c = elevator.charAt(elevator.length() - 1);
		int elevatorNum = Character.getNumericValue(c);
		if (on) {
			b.set(elevatorNum - 1, true);
		} else {
			if (b.get(elevatorNum - 1)) {
				b.set(elevatorNum - 1, false);
			}
		}
		arrivalSensors.remove(Integer.parseInt(floor));
		arrivalSensors.put(Integer.parseInt(floor), b);
		if (on) {
			System.out.println("Arrival Sensor on Floor " + floor + " for Elevator " + elevatorNum + " turned on!");
		} else {
			System.out.println("Arrival Sensor on Floor " + floor + " for Elevator " + elevatorNum + " turned off!");
		}
	}

	private void setFloorLampsOff(String floor) {
		// Turn off Floor lamp at the floor it was requested at when elevator arrives at
		// requested floor
		Boolean[] b = floorLamps.get(Integer.parseInt(floor));
		if (b[0]) {
			b[0] = false;
			System.out.println("Floor " + floor + " lamp UP turned off");
		}
		if (b[1]) {
			b[1] = false;
			System.out.println("Floor " + floor + " lamp DOWN turned off");
		}
	}

	/**
	 * Runs forever until the system exits, and communicates with the Scheduler.
	 */
	@Override
	public void run() {
		while (true) {
			// TODO: Change if statement to a loop so we can process more than 1 request
			if (requestCount == 0) {
				FloorRequest fr = requests.get(0);
				System.out.println(fr);
				this.requests.remove(fr);
				requestCount++;
			}

			System.out.println("Floor Sent: " + this.data);
			this.data = "";
			System.out.println("Floor Received: " + this.data);
			String[] splitElevatorResponse = this.data.split(" ");
			if (splitElevatorResponse[1].equals("-1")) {
				System.exit(0);
			}
			if (splitElevatorResponse[0].equals("arrived")) {
				this.data = "go";
			}

			try {
				Thread.sleep(1500L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void TestSend() {
		if (requests.size() == 0) {
			String status = "go";
			byte[] toSend = status.getBytes();
			try {
				this.sendPacket = new DatagramPacket(toSend, toSend.length, InetAddress.getLocalHost(),
						r.getFloorPort());
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.exit(1);
			}

			try {
				this.sendReceiveSocket.send(this.sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			if (wait.equals("waiting")) {
				System.out.println("Floor has nothing to send");
				wait = "";
				while (true) {
					testing();
				}
			}
		} else {
			FloorRequest floorRequest = requests.get(0);
			wait = "waiting";
			String floorRequestData = floorRequest.toString();
			if (floorRequest.getFloorDestination() < 0) {
				floorRequestData = "error " + floorRequest.getFloorDestination();
			}
			byte[] toSend = floorRequestData.getBytes();
			try {
				this.sendPacket = new DatagramPacket(toSend, toSend.length, InetAddress.getLocalHost(),
						r.getFloorPort());
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.exit(1);
			}

			try {
				this.sendReceiveSocket.send(this.sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			System.out.println("Floor Sent: " + floorRequestData);

			byte[] data = new byte[100];
			receivePacket = new DatagramPacket(data, data.length);
			try {
				sendReceiveSocket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			// Receive data from Scheduler
			String floorStatus = "";
			String toPrint = new String(receivePacket.getData(), 0, this.receivePacket.getLength());
			String[] splitElevatorResponse = (new String(receivePacket.getData(), 0, this.receivePacket.getLength()))
					.split(" ");

			String[] elevators = new String[numOfElevators];
			/*
			 * for(String s : elevators){ System.out.println(s); }
			 */
			for (int i = 0; i < numOfElevators; i++) {
				String splitResponse = splitElevatorResponse[i];
				String[] individualElevator = splitResponse.split("-");
				try {
					elevators[Integer.parseInt(individualElevator[0].substring(individualElevator[0].length() - 1))
							- 1] = splitResponse;
				} catch (UnknownError e) {
					elevators[i] = splitResponse;
				}
				if (individualElevator[1].equals("arrived")) {
					this.setLampsSensors(individualElevator[2], individualElevator[0], true);
					// Turn off floor lamp when elevator reaches requested floor
					setFloorLampsOff(individualElevator[2]);
					floorStatus = "go";
				}
				if (individualElevator[1].equals("moving")) {
					floorStatus = "go";
				}
				if (individualElevator[1].equals("door_closing")) {
					floorStatus = "go";
				}
				if (individualElevator[1].equals("door_closed")) {
					floorStatus = "go";
					setLampsSensors(individualElevator[2], individualElevator[0], false);
				}
				if (individualElevator[1].equals("door_opening")) {
					floorStatus = "go";
				}
				if (individualElevator[1].equals("error")) {
					floorStatus = "go";
				}
			}

			String print = "";

			for (String p : elevators) {
				if (print.equals("")) {
					print = p;
				} else {
					print = print + " " + p;
				}
			}

			System.out.println("Floor received: " + print);

			boolean elevatorWait = true;
			for (int i = 0; i < numOfElevators; i++) {
				String splitResponse = splitElevatorResponse[i];
				String[] individualElevator = splitResponse.split("-");
				if (individualElevator[1].equals("waiting")) {
					elevatorWait = false;
				}
			}

			while (elevatorWait) {
				byte[] toSend2 = floorStatus.getBytes();
				try {
					this.sendPacket = new DatagramPacket(toSend2, toSend2.length, InetAddress.getLocalHost(),
							r.getFloorPort());
				} catch (UnknownHostException e) {
					e.printStackTrace();
					System.exit(1);
				}

				try {
					this.sendReceiveSocket.send(this.sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				System.out.println("Floor Sent: " + floorStatus);

				data = new byte[100];
				receivePacket = new DatagramPacket(data, data.length);
				try {
					sendReceiveSocket.receive(receivePacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				// Receive data from Scheduler
				floorStatus = "";
				toPrint = new String(receivePacket.getData(), 0, this.receivePacket.getLength());
				splitElevatorResponse = (new String(receivePacket.getData(), 0, this.receivePacket.getLength()))
						.split(" ");

				for (int i = 0; i < splitElevatorResponse.length; i++) {
					String splitResponse = splitElevatorResponse[i];
					String[] individualElevator = splitResponse.split("-");
					try {
						elevators[Integer.parseInt(individualElevator[0].substring(individualElevator[0].length() - 1))
								- 1] = splitResponse;
					} catch (UnknownError e) {
						elevators[i] = splitResponse;
					}
					if (individualElevator[1].equals("arrived")) {
						this.setLampsSensors(individualElevator[2], individualElevator[0], true);
						// Turn off floor lamp when elevator reaches requested floor
						setFloorLampsOff(individualElevator[2]);
						floorStatus = "go";
					}
					if (individualElevator[1].equals("moving")) {
						floorStatus = "go";
					}
					if (individualElevator[1].equals("door_closing")) {
						floorStatus = "go";
					}
					if (individualElevator[1].equals("door_closed")) {
						floorStatus = "go";
						setLampsSensors(individualElevator[2], individualElevator[0], false);
					}
					if (individualElevator[1].equals("door_opening")) {
						floorStatus = "go";
					}
					if (individualElevator[1].equals("error")) {
						floorStatus = "go";
					}
				}

				print = "";

				for (String p : elevators) {
					if (print.equals("")) {
						print = p;
					} else {
						print = print + " " + p;
					}
				}

				elevatorWait = true;
				for (int x = 0; x < numOfElevators; x++) {
					String t = splitElevatorResponse[x];
					String[] individualElevator = t.split("-");
					if (individualElevator[1].equals("waiting")) {
						elevatorWait = false;
					}
				}
				System.out.println("Floor received: " + print);
			}
			requests.remove(0);
		}
	}

	public void testing() {
		wait = "go";
		String floorRequestData = wait.toString();
		byte[] toSend = floorRequestData.getBytes();
		try {
			this.sendPacket = new DatagramPacket(toSend, toSend.length, InetAddress.getLocalHost(), r.getFloorPort());
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

		try {
			this.sendReceiveSocket.send(this.sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Floor Sent: " + floorRequestData);

		byte[] data = new byte[100];
		receivePacket = new DatagramPacket(data, data.length);
		try {
			sendReceiveSocket.receive(receivePacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		// Receive data from Scheduler
		String floorStatus = "";
		String toPrint = new String(receivePacket.getData(), 0, this.receivePacket.getLength());
		String[] splitElevatorResponse = (new String(receivePacket.getData(), 0, this.receivePacket.getLength()))
				.split(" ");

		String[] elevators = new String[numOfElevators];
		for (int i = 0; i < numOfElevators; i++) {
			String splitResponse = splitElevatorResponse[i];
			String[] individualElevator = splitResponse.split("-");
			try {
				elevators[Integer.parseInt(individualElevator[0].substring(individualElevator[0].length() - 1))
						- 1] = splitResponse;
			} catch (UnknownError e) {
				elevators[i] = splitResponse;
			}
			if (individualElevator[1].equals("arrived")) {
				this.setLampsSensors(individualElevator[2], individualElevator[0], true);
				// Turn off floor lamp when elevator reaches requested floor
				setFloorLampsOff(individualElevator[2]);
				floorStatus = "go";
			}
			if (individualElevator[1].equals("moving")) {
				floorStatus = "go";
			}
			if (individualElevator[1].equals("door_closing")) {
				floorStatus = "go";
			}
			if (individualElevator[1].equals("door_closed")) {
				floorStatus = "go";
				setLampsSensors(individualElevator[2], individualElevator[0], false);
			}
			if (individualElevator[1].equals("door_opening")) {
				floorStatus = "go";
			}
			if (individualElevator[1].equals("error")) {
				floorStatus = "go";
			}
		}

		String print = "";

		for (String p : elevators) {
			if (print.equals("")) {
				print = p;
			} else {
				print = print + " " + p;
			}
		}

		System.out.println("Floor received: " + print);

		boolean elevatorWait = true;
		for (int i = 0; i < numOfElevators; i++) {
			String splitResponse = splitElevatorResponse[i];
			String[] individualElevator = splitResponse.split("-");
			if (individualElevator[1].equals("waiting")) {
				elevatorWait = false;
			}
		}

		while (elevatorWait) {
			byte[] toSend2 = floorStatus.getBytes();
			try {
				this.sendPacket = new DatagramPacket(toSend2, toSend2.length, InetAddress.getLocalHost(),
						r.getFloorPort());
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.exit(1);
			}

			try {
				this.sendReceiveSocket.send(this.sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			System.out.println("Floor Sent: " + floorStatus);

			data = new byte[100];
			receivePacket = new DatagramPacket(data, data.length);
			try {
				sendReceiveSocket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			// Receive data from Scheduler
			floorStatus = "";
			toPrint = new String(receivePacket.getData(), 0, this.receivePacket.getLength());
			splitElevatorResponse = (new String(receivePacket.getData(), 0, this.receivePacket.getLength())).split(" ");

			for (int i = 0; i < splitElevatorResponse.length; i++) {
				String splitResponse = splitElevatorResponse[i];
				String[] individualElevator = splitResponse.split("-");
				try {
					elevators[Integer.parseInt(individualElevator[0].substring(individualElevator[0].length() - 1))
							- 1] = splitResponse;
				} catch (UnknownError e) {
					elevators[i] = splitResponse;
				}
				if (individualElevator[1].equals("arrived")) {
					this.setLampsSensors(individualElevator[2], individualElevator[0], true);
					// Turn off floor lamp when elevator reaches requested floor
					setFloorLampsOff(individualElevator[2]);
					floorStatus = "go";
				}
				if (individualElevator[1].equals("moving")) {
					floorStatus = "go";
				}
				if (individualElevator[1].equals("door_closing")) {
					floorStatus = "go";
				}
				if (individualElevator[1].equals("door_closed")) {
					floorStatus = "go";
					setLampsSensors(individualElevator[2], individualElevator[0], false);
				}
				if (individualElevator[1].equals("door_opening")) {
					floorStatus = "go";
				}
				if (individualElevator[1].equals("error")) {
					floorStatus = "go";
				}
			}

			print = "";

			for (String p : elevators) {
				if (print.equals("")) {
					print = p;
				} else {
					print = print + " " + p;
				}
			}

			elevatorWait = true;
			for (int x = 0; x < numOfElevators; x++) {
				String t = splitElevatorResponse[x];
				String[] individualElevator = t.split("-");
				if (individualElevator[1].equals("waiting")) {
					elevatorWait = false;
				}
			}
			System.out.println("Floor received: " + print);
		}
	}

	/*
	 * Getters for Unit Testing
	 */
	/**
	 * @return requests
	 */
	public ArrayList<FloorRequest> getRequests() {
		return this.requests;
	}

	/**
	 * @return arrivalSensors
	 */
	public Map<Integer, ArrayList<Boolean>> getArrivalSensors() {
		return this.arrivalSensors;
	}

	/**
	 * @return floorLamps
	 */
	public Map<Integer, Boolean[]> getFloorLamps() {
		return this.floorLamps;
	}

	public static void main(String[] args) {
		FloorSubsystem floor = new FloorSubsystem("requests.txt");
		floor.Initialize();
		while (true) {
			floor.TestSend();
		}
	}
}