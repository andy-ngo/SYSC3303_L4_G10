import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FloorSubsystem implements Runnable {

	private String data;
	private ArrayList<FloorRequest> requests;
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendReceiveSocket;
	private String wait = "waiting";
	private int numOfElevators = 0;
	public int requestCount = 0;
	private Map<Integer, Boolean[]> floorLamps;
	private Map<Integer, ArrayList<Boolean>> arrivalSensors;

	private static ReadPropertyFile r = new ReadPropertyFile();

	/**
	 * Constructor for class FloorSubsytem. Initializes all variables and reads input file.
	 *
	 * @param file String path of input file
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
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Initializes UDP stuff.
	 */
	public void initialize() {
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
	 * Goes through file of requests and creates FloorRequest objects
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
			System.out.println(Timestamp.from(Instant.now()) + "  -  File not found.");
			e.printStackTrace();
		}

	}

	/**
	 * @param String num - floor number to turn light off
	 * @param boolean on - true or false for on or off
	 */
	public void setLampsSensors(String floor, String elevator, boolean on) {
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
			System.out.println(Timestamp.from(Instant.now()) + "  -  Arrival Sensor on Floor " + floor + " for Elevator " + elevatorNum + " turned on!");
		} else {
			System.out.println(Timestamp.from(Instant.now()) + "  -  Arrival Sensor on Floor " + floor + " for Elevator " + elevatorNum + " turned off!");
		}
	}

	/**
	 * @param String num - floor number to turn light off
	 */
	private void setFloorLampsOff(String num) {
		Boolean[] b = floorLamps.get(Integer.parseInt(num));
		if (b[0]) {
			b[0] = false;
			System.out.println(Timestamp.from(Instant.now()) + "  -  Floor " + num + " lamp UP turned off");
		}
		if (b[1]) {
			b[1] = false;
			System.out.println(Timestamp.from(Instant.now()) + "  -  Floor " + num + " lamp DOWN turned off");
		}
	}

	/**
	 * Communicates with scheduler.
	 */
	@Override
	public void run() {}

	/**
	 * Sends requests to the scheduler.
	 */
	public void send() {
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
				System.out.println(Timestamp.from(Instant.now()) + "  -  Floor has nothing to send");
				wait = "";
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
			System.out.println(Timestamp.from(Instant.now()) + "  -  Floor Sent: " + floorRequestData);

			byte[] data = new byte[100];
			receivePacket = new DatagramPacket(data, data.length);
			try {
				sendReceiveSocket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			String floorStatus = "";
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
					setFloorLampsOff(individualElevator[2]);
					floorStatus = "go";
				}
				if (individualElevator[1].equals("moving")) {
					floorStatus = "go";
				}
				if (individualElevator[1].equals("door is closing!")) {
					floorStatus = "go";
				}
				if (individualElevator[1].equals("door is closed")) {
					floorStatus = "go";
					setLampsSensors(individualElevator[2], individualElevator[0], false);
				}
				if (individualElevator[1].equals("door is opening.")) {
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
				System.out.println(Timestamp.from(Instant.now()) + "  -  Floor Sent: " + floorStatus);

				data = new byte[100];
				receivePacket = new DatagramPacket(data, data.length);
				try {
					sendReceiveSocket.receive(receivePacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				floorStatus = "";
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
						setFloorLampsOff(individualElevator[2]);
						floorStatus = "go";
					}
					if (individualElevator[1].equals("moving")) {
						floorStatus = "go";
					}
					if (individualElevator[1].equals("door is closing!")) {
						floorStatus = "go";
					}
					if (individualElevator[1].equals("door is closed")) {
						floorStatus = "go";
						setLampsSensors(individualElevator[2], individualElevator[0], false);
					}
					if (individualElevator[1].equals("door is opening.")) {
						floorStatus = "go";
					}
					if (individualElevator[1].equals("error")) {
						floorStatus = "go";
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
			}
			requests.remove(0);
		}
	}

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
		floor.initialize();
		while (true) {
			floor.send();
		}
	}
}