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
	private static ReadPropertyFile rpf = new ReadPropertyFile();

	/**
	 * Constructor for class floor subsystem
	 *
	 * @param FileLocation path for requests file
	 */
	public FloorSubsystem(String FileLocation) {
		this.requests = new ArrayList<FloorRequest>();
		floorLamps = new HashMap<Integer, Boolean[]>();
		arrivalSensors = new HashMap<Integer, ArrayList<Boolean>>();
		for (int i = 0; i < rpf.getNumFloors(); i++) {
			Boolean[] b = { false, false };
			floorLamps.put(i + 1, b);
		}
		for (int i = 0; i < rpf.getNumFloors(); i++) {
			ArrayList<Boolean> b = new ArrayList<>();
			for (int j = 0; j < rpf.getNumElevators(); j++) {
				b.add(false);
			}
			arrivalSensors.put(i + 1, b);
		}
		this.addFloorRequest(FileLocation);
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * initialize UDP
	 */
	public void initialize() {
		byte[] toSend = new byte[100];
		try {
			this.sendPacket = new DatagramPacket(toSend, toSend.length, InetAddress.getLocalHost(), rpf.getFloorPort());
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
		String toPrint = new String(receivePacket.getData(), 0, this.receivePacket.getLength());
		numOfElevators = Integer.parseInt(toPrint);
	}

	/**
	 * Goes through file with requests and creates FloorRequest objects
	 *
	 * @param fileLocation location of the file
	 */
	public void addFloorRequest(String fileLocation) {
		long travelTime = 1L;//temporary
		long doorTime = 1L;

		try {
			File myObj = new File(fileLocation);
			Scanner myReader = new Scanner(myObj);
			//scan file while there are more requests
			while (myReader.hasNextLine()) {
				this.data = myReader.nextLine();
				FloorRequest request = new FloorRequest();
				String[] requestArray = this.data.split(" ");
				if (requestArray[0].equals("error")) {	//error request
					if (requestArray[1].equals("doorStuck")) {
						request.setFloorDestination(-1);
					} else if (requestArray[1].equals("floorStuck")) {	//will shut down the elevator
						request.setFloorDestination(-2);
					}
				} else {
					String direction = requestArray[2];
					Boolean[] currLampStatus = floorLamps.get(Integer.parseInt(requestArray[1]));
					ElevatorMotor requestElevatorMotor;
					if (direction.equals("Up")) {
						currLampStatus[0] = true;
						requestElevatorMotor = ElevatorMotor.UP;
					} else if (direction.equals("Down")) {
						requestElevatorMotor = ElevatorMotor.DOWN;
						currLampStatus[1] = true;
					} else {
						requestElevatorMotor = ElevatorMotor.STOP;
					}

					request = new FloorRequest(requestArray[0], Integer.parseInt(requestArray[1]), requestElevatorMotor, Integer.parseInt(requestArray[3]), travelTime, doorTime);
				}
				this.requests.add(request);
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println(Timestamp.from(Instant.now()) + "  -  File not found.");
			e.printStackTrace();
		}

	}

	/**
	 * arrival sensor when elevator arrives on a floor
	 * 
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
	 * floor lamp for requested floor off when elevator arrives at requested floor
	 * 
	 * @param String floor - floor number to turn light off
	 */
	private void setFloorLampsOff(String floor) {
		//turn off Floor lamp at the floor it was requested at when elevator arrives at requested floor
		Boolean[] b = floorLamps.get(Integer.parseInt(floor));
		if (b[0]) {
			b[0] = false;
			System.out.println(Timestamp.from(Instant.now()) + "  -  Floor " + floor + " lamp UP turned off");
		}
		if (b[1]) {
			b[1] = false;
			System.out.println(Timestamp.from(Instant.now()) + "  -  Floor " + floor + " lamp DOWN turned off");
		}
	}
	
	/**
	 * sends requests to scheduler
	 */
	public void send() {
		if (requests.size() == 0) {	//no more requests
			String status = "ok";
			byte[] toSend = status.getBytes();
			try {
				this.sendPacket = new DatagramPacket(toSend, toSend.length, InetAddress.getLocalHost(), rpf.getFloorPort());
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
				while (true) {
					checking();
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
				this.sendPacket = new DatagramPacket(toSend, toSend.length, InetAddress.getLocalHost(), rpf.getFloorPort());
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
					elevators[Integer.parseInt(individualElevator[0].substring(individualElevator[0].length() - 1)) - 1] = splitResponse;
				} catch (UnknownError e) {
					elevators[i] = splitResponse;
				}
				if (individualElevator[1].equals("arrived")) {
					this.setLampsSensors(individualElevator[2], individualElevator[0], true);
					setFloorLampsOff(individualElevator[2]);
					floorStatus = "ok";
				}
				if (individualElevator[1].equals("moving")) {
					floorStatus = "ok";
				}
				if (individualElevator[1].equals("door_closing")) {
					floorStatus = "ok";
				}
				if (individualElevator[1].equals("door_closed")) {
					floorStatus = "ok";
					setLampsSensors(individualElevator[2], individualElevator[0], false);
				}
				if (individualElevator[1].equals("door_opening")) {
					floorStatus = "ok";
				}
				if (individualElevator[1].equals("error")) {
					floorStatus = "ok";
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

			System.out.println(Timestamp.from(Instant.now()) + "  -  Floor received: " + print);

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
					this.sendPacket = new DatagramPacket(toSend2, toSend2.length, InetAddress.getLocalHost(), rpf.getFloorPort());
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
				if(floorStatus != "ok") System.out.println(Timestamp.from(Instant.now()) + "  -  Floor Sent: " + floorStatus);

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
						floorStatus = "ok";
					}
					if (individualElevator[1].equals("moving")) {
						floorStatus = "ok";
					}
					if (individualElevator[1].equals("door_closing")) {
						floorStatus = "ok";
					}
					if (individualElevator[1].equals("door_closed")) {
						floorStatus = "ok";
						setLampsSensors(individualElevator[2], individualElevator[0], false);
					}
					if (individualElevator[1].equals("door_opening")) {
						floorStatus = "ok";
					}
					if (individualElevator[1].equals("error")) {
						floorStatus = "ok";
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
				System.out.println(Timestamp.from(Instant.now()) + "  -  Floor received: " + print);
			}
			requests.remove(0);
		}
	}

	/**
	 * communicates with schedular
	 */
	@Override
	public void run() {}

	/**
	 * gets updates on elevators
	 */
	public void checking() {
		wait = "ok";
		String floorRequestData = wait.toString();
		byte[] toSend = floorRequestData.getBytes();
		try {
			this.sendPacket = new DatagramPacket(toSend, toSend.length, InetAddress.getLocalHost(), rpf.getFloorPort());
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
		if(floorRequestData != "ok") System.out.println(Timestamp.from(Instant.now()) + "  -  Floor Sent: " + floorRequestData);

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
			String[] individualElevator = splitResponse.split("-");	//gets status of elevators
			try {
				elevators[Integer.parseInt(individualElevator[0].substring(individualElevator[0].length() - 1))
						- 1] = splitResponse;
			} catch (UnknownError e) {
				elevators[i] = splitResponse;
			}
			if (individualElevator[1].equals("arrived")) {
				this.setLampsSensors(individualElevator[2], individualElevator[0], true);
				setFloorLampsOff(individualElevator[2]);
				floorStatus = "ok";
			}
			if (individualElevator[1].equals("moving")) {
				floorStatus = "ok";
			}
			if (individualElevator[1].equals("door_closing")) {
				floorStatus = "ok";
			}
			if (individualElevator[1].equals("door_closed")) {
				floorStatus = "ok";
				setLampsSensors(individualElevator[2], individualElevator[0], false);
			}
			if (individualElevator[1].equals("door_opening")) {
				floorStatus = "ok";
			}
			if (individualElevator[1].equals("error")) {
				floorStatus = "ok";
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

		System.out.println(Timestamp.from(Instant.now()) + "  -  Floor received: " + print);

		boolean elevatorWait = true;
		for (int i = 0; i < numOfElevators; i++) {
			String splitResponse = splitElevatorResponse[i];
			String[] individualElevator = splitResponse.split("-");
			if (individualElevator[1].equals("waiting")) {
				elevatorWait = false;
			}
		}

		while (elevatorWait) {	//as long as the elevator is waiting
			byte[] toSend2 = floorStatus.getBytes();
			try {
				this.sendPacket = new DatagramPacket(toSend2, toSend2.length, InetAddress.getLocalHost(), rpf.getFloorPort());
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
			if(floorStatus != "ok") System.out.println(Timestamp.from(Instant.now()) + "  -  Floor Sent: " + floorStatus);

			data = new byte[100];
			receivePacket = new DatagramPacket(data, data.length);
			try {
				sendReceiveSocket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			floorStatus = "";
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
					setFloorLampsOff(individualElevator[2]);
					floorStatus = "ok";
				}
				if (individualElevator[1].equals("moving")) {
					floorStatus = "ok";
				}
				if (individualElevator[1].equals("door_closing")) {
					floorStatus = "ok";
				}
				if (individualElevator[1].equals("door_closed")) {
					floorStatus = "ok";
					setLampsSensors(individualElevator[2], individualElevator[0], false);
				}
				if (individualElevator[1].equals("door_opening")) {
					floorStatus = "ok";
				}
				if (individualElevator[1].equals("error")) {
					floorStatus = "ok";
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
			System.out.println(Timestamp.from(Instant.now()) + "  -  Floor received: " + print);
		}
	}

	/**
	 * @return  ArrayList<FloorRequest> requests
	 */
	public ArrayList<FloorRequest> getRequests() {
		return this.requests;
	}

	/**
	 * @return Map<Integer, ArrayList<Boolean>> arrivalSensors
	 */
	public Map<Integer, ArrayList<Boolean>> getArrivalSensors() {
		return this.arrivalSensors;
	}

	public static void main(String[] args) {
		FloorSubsystem floor = new FloorSubsystem("requests.txt");
		floor.initialize();
		while (true) {
			floor.send();
		}
	}
}