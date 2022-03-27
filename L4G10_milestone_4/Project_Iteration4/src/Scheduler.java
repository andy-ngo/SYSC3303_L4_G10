import java.io.IOException;
import java.net.*;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

public class Scheduler {

	private DatagramPacket receivePacket, sendPacket;
    private DatagramSocket sendReceiveSocketFloor, sendReceiveSocketElevators;
    private InetAddress addressFloor;
    
    public ElevatorMotor direction;
    private String isDataFromFloor, dataFromElevator, message;
    private int floorToVisit, portFloor, currentFloor;
    private boolean emptyFloor, emptyElevator;
    private SchedulerState currentState1, currentState2;
    
    private ArrayList<Elevator> elevators, stuck;
    private ArrayList<String> time;
    
    private int waiting = 0, elevatorBeingUsed = 0, maxElevator = 0, count = 0;
    private Random rand;

    private static ReadPropertyFile file = new ReadPropertyFile();

    /**
     * Initializes all the variables
     */
    public Scheduler() {
        currentFloor = 1;
        isDataFromFloor = "";
        dataFromElevator = "";
        floorToVisit = -1;
        emptyFloor = true;
        emptyElevator = true;
        rand = new Random();

        currentState1 = SchedulerState.STATE_1;
        currentState2 = SchedulerState.STATE_2;

        elevators = new ArrayList<>();
        stuck = new ArrayList<>();
        time = new ArrayList<>();
        

        try {
            sendReceiveSocketFloor = new DatagramSocket(file.getFloorPort());
            sendReceiveSocketElevators = new DatagramSocket(file.getElevatorPort());
        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }

    }
    
    public void updateTimeStamp(int elevatorNum){
        time.set(elevatorNum,getTime());
    }

    private String getTime() {
        Date date = new Date();
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
        formatter.setTimeZone(TimeZone.getTimeZone("EST"));
        String dateFormatted = formatter.format(date);

        return dateFormatted;
    }

    /**
     * Changes the number of elevator being used
     */
    private void elevatorChange() {
        if (elevatorBeingUsed >= maxElevator) {
            elevatorBeingUsed = 0;
        } else {
            elevatorBeingUsed++;
        }
    }

    /**
     * Calls the state machines and update the elevator changes
     */
    public void sendAndReceive() {
        byte[] dat = new byte[100];
        receivePacket = new DatagramPacket(dat, dat.length);
        while (true) {
            if (count == 2) {
                elevatorChange();
                count = 0;
            }
            this.receiveStateMachine(dat);
            this.sendStateMachine();
        }
    }

    /**
     * A send state machine
     *
     * @return
     */
    public void sendStateMachine() {
        switch (currentState1) {
            case STATE_1: 
                for (int i = 0; i <= maxElevator; i++) {
                    sendToElevator();
                }
                currentState1 = SchedulerState.STATE_2;
                break;
            case STATE_2:
                sendToFloor();
                currentState1 = SchedulerState.STATE_1;
                count++;
                break;
        }
        return;
    }

    /**
     * A receive state machine
     */
    public void receiveStateMachine(byte[] data) {
        switch (currentState2) {
            case STATE_1:
                for (int i = 0; i <= maxElevator; i++) {
                    boolean b = receiveFromElevator(data);
                    if (!b) {
                        i--;
                    }
                }
                currentState2 = SchedulerState.STATE_2;
                break;
            case STATE_2: 
                receiveFromFloor(data);
                currentState2 = SchedulerState.STATE_1;
                break;
        }
    }

    /**
     * Receives data from the floor
     *
     * @param data An ready to go message or nothing
     */
    public synchronized void receiveFromFloor(byte[] data) {
        try {
            sendReceiveSocketFloor.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        String id = new String(receivePacket.getData(), 0, this.receivePacket.getLength());
        String[] removed = id.split(" ");
        
        if (!removed[0].equals("go")) {
    		System.out.println(Timestamp.from(Instant.now()) + "  -  Request received from floor: "+id);
            if (!removed[0].equals("error")) {	
                checkPriority(Integer.parseInt(removed[1]), removed[2], Integer.parseInt(removed[3]));
            } else {
            	int hold;
                do { 
                	hold = rand.nextInt(elevators.size());
                }while(elevators.get(hold).getError() != 0);

            	elevators.get(hold).setError(Integer.parseInt(removed[1]));

            }
        }
    }

    /**
     * Receives data from the elevator
     *
     * @param data the info received from the elevator, either an arrival message or
     *             a button press
     */
    public synchronized boolean receiveFromElevator(byte[] data) {
        Elevator elev = new Elevator(); 
        try {
            sendReceiveSocketElevators.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        String id = new String(receivePacket.getData(), 0, this.receivePacket.getLength());
        String[] splitElevatorMsg = id.split("-");
        String[] test = message.split(" "); 
        
        for(Elevator e : elevators) {
        	if(splitElevatorMsg[0].equals(e.getID())) {
        		elev = e;
        	}
        }
        int elevatorNum = Integer.parseInt(String.valueOf(elev.getID().charAt(elev.getID().length()-1)));
        time.set(elevatorNum-1,getTime());

        for (String tt : test) {
            String[] test2 = tt.split("-");
            if (test2[0].equals(splitElevatorMsg[0])) {
                if (test2[1].equals("moving") && !splitElevatorMsg[1].equals("moving")) {
                    createMovingMessage(id, test, tt);
                } else {
                    if (test2[1].equals("moving") && splitElevatorMsg[1].equals("moving")) {
                        if (Integer.parseInt(test2[2]) != Integer.parseInt(splitElevatorMsg[2])) {
                            createMovingMessage(id, test, tt);
                        }
                    }
                }
                return false;
            }
        }

        if (!splitElevatorMsg[1].equals("arrived")) {
            if (splitElevatorMsg[1].equals("added")) {
                this.checkPriority(-1, null, Integer.parseInt(splitElevatorMsg[0]));
            } else {
                if (splitElevatorMsg[1].equals("moving")) {
                	elev.setStatus("moving");
                	elev.setCurrentFloor(Integer.parseInt(splitElevatorMsg[2]));
                	if (message.equals("")) {
                        message = message + splitElevatorMsg[0] + "-elevator moving-" + splitElevatorMsg[2];
                    } else {
                        message = message + " " + splitElevatorMsg[0] + "-elevator moving-" + splitElevatorMsg[2];
                        waiting--;
                    }
                } else if (splitElevatorMsg[1].equals("waiting")) { //If elevator waiting for new instruction
                	elev.setStatus("waiting");
                	waiting++;
                    if (message.equals("")) {
                        message = message + splitElevatorMsg[0] + "-waiting-";
                    } else {
                        message = message + " " + splitElevatorMsg[0] + "-waiting-";
                        waiting--;
                    }
                } else if (splitElevatorMsg[1].equals(" door is closing!")) { //If elevators door closing
                	elev.setStatus(" door is closing!");
                	if (message.equals("")) {
                        message = message + splitElevatorMsg[0] + "-door is closing!-";
                    } else { 
                        message = message + " " + splitElevatorMsg[0] + "-door is closing!-";
                        waiting--;
                    }

                } else if (splitElevatorMsg[1].equals("door is closed")) { 
                	elev.setStatus("door is closed");
                	if (message.equals("")) {
                        message = message + splitElevatorMsg[0] + "-door is closed-" + splitElevatorMsg[2];
                    } else {
                        message = message + " " + splitElevatorMsg[0] + "-door is closed-" + splitElevatorMsg[2];
                        waiting--;
                    }

                } else if (splitElevatorMsg[1].equals("door_opening")) {
                	elev.setStatus("door opening");
                	if (message.equals("")) {
                        message = message + splitElevatorMsg[0] + "-door is opening.-";
                    } else {
                        message = message + " " + splitElevatorMsg[0] + "-door is opening.-";
                        waiting--;
                    }
                } else if (splitElevatorMsg[1].equals("error")) {
                	if(elev.getError() == -1) {
                		elev.setStatus(" door is stuck");
                	}else if(elev.getError() == -2){
                		elev.setStatus("elvator stuck between floors!");
                		 stuck.add(elev);
                	}

                    if (message.equals("")) {
                        message = message + splitElevatorMsg[0] + "-error-" + splitElevatorMsg[2];
                    } else {
                        message = message + " " + splitElevatorMsg[0] + "-error-" + splitElevatorMsg[2];
                        waiting--;
                    }
                } else if (splitElevatorMsg[1].equals("doorReset")) {
                	elev.setStatus("doors reset");
                	elev.setError(0);
                    if (message.equals("")) {
                        message = message + splitElevatorMsg[0] + "-waiting-" + splitElevatorMsg[2];
                    } else {
                        message = message + " " + splitElevatorMsg[0] + "-waiting-" + splitElevatorMsg[2];
                        waiting--;
                    }
                }
                else if (splitElevatorMsg[1].equals("doorReseting")) {
                    elev.setStatus("doors reseting");
                    elev.setError(0);
                    if (message.equals("")) {
                        message = message + splitElevatorMsg[0] + "-waiting-" + splitElevatorMsg[2];
                    } else {
                        message = message + " " + splitElevatorMsg[0] + "-waiting-" + splitElevatorMsg[2];
                        waiting--;
                    }
                }
            }
        } else {
        	elev.setStatus("arrived");
            elev.setCurrentFloor(Integer.parseInt(splitElevatorMsg[2]));
            if (message.equals("")) {  
                message = message + id;
            } else { 
                message = message + " " + id;
                waiting--;
            }
        }
        elevatorChange();
        return true;
    }

    /**
     * Helper method that creates a message with an elevator that is moving
     *
     * @param id
     * @param test
     * @param tt
     */
    private void createMovingMessage(String id, String[] test, String tt) {
        message = "";
        for (String tt2 : test) {
            if (tt2.equals(tt)) {
                if (message.equals("")) {
                    message = message + id;
                } else {
                    message = message + " " + id;
                }
            } else {
                if (message.equals("")) {
                    message = message + tt2;
                } else {
                    message = message + " " + tt2;
                }
            }
        }
    }

    /**
     * Sends data to the floor
     *
     * @return the instructions to the floor, right now just an arriving message
     */
    public synchronized void sendToFloor() {
        byte[] toSend = new byte[100];
        if (waiting == elevators.size()) {
            String dataString = "waiting";
            toSend = dataString.getBytes();
            message = "";
        } else {
            String dataString = message;
            toSend = dataString.getBytes();
            message = "";
        }

        this.sendPacket = new DatagramPacket(toSend, toSend.length, addressFloor, portFloor);
        try {
            this.sendReceiveSocketFloor.send(this.sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * @return sends the floor requested to the elevator
     */
    public synchronized void sendToElevator() {
        Elevator temp = elevators.get(elevatorBeingUsed);
        byte[] toSend = new byte[100];

        if (temp.getError() != 0) { 
            String e = "error " + temp.getError();
            if(temp.getError() == -1){
                temp.setError(0);
            }
            toSend = e.getBytes();
        } else {
            int t = checkSend(temp); 
            if (t == -1) { 
                String wait = "waiting";
                toSend = wait.getBytes();
            } else { 
                String data = t + " " + temp.getDirection();
                temp.addDestination(t);
                toSend = data.getBytes();
            }
        }

        this.sendPacket = new DatagramPacket(toSend, toSend.length, temp.getAddress(), temp.getPort());

        try {
            this.sendReceiveSocketElevators.send(this.sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        elevatorChange();
    }

    /**
     * Uses the differences to figure out which elevator is going to be sent
     *
     * @param differenceMap stores all the differences between floors
     * @param floor         stores the origin floor
     * @param down          boolean that chooses whether the origin is up or down from the current elevator location
     * @param floor2        stores the destination
     * @param dir           stores the direction
     */
    private void getElevatorFromDifference(HashMap<String, Integer> differenceMap, int floor, boolean down, int floor2, String dir) {
        int minDifference = Collections.min(differenceMap.values());
        for (String currElevatorid : differenceMap.keySet()) {
            if (differenceMap.get(currElevatorid).equals(minDifference)) {
                for (Elevator e : elevators) {
                    if (e.getID().equals(currElevatorid)) {
                        if (down) {
                        	e.addToDown(floor);
                            if (dir.equals("UP")) {
                                e.addToUp(floor2);
                            } else {
                                e.addToDown(floor2);
                            }
                            break;
                        } else {
                            e.addToUp(floor);
                            if (dir.equals("UP")) {
                                e.addToUp(floor2);
                            } else {
                                e.addToDown(floor2);
                            }
                            break;
                        }
                    }
                }
                break;
            }
        }
    }

    /**
     * Adds the floor to the right queue
     *
     * @param origin               thats is the floor that need to be added to the queue
     * @param floorButtonDirection received from the floor
     * @param floor                is the destination
     */
    public synchronized void checkPriority(int origin, String floorButtonDirection, int floor) {

        HashMap<String, Integer> differenceUp = new HashMap<>();
        HashMap<String, Integer> differenceDown = new HashMap<>();
        HashMap<String, Integer> differenceStopped = new HashMap<>();

        for (Elevator currElevator : elevators) {
            if (!stuck.contains(currElevator) || stuck.isEmpty()) {
                int difference = currElevator.getCurrentFloor() - origin;
                if (currElevator.getDirection().equals(ElevatorMotor.Down)) {
                    if (difference > 0) {
                        differenceDown.put(currElevator.id, difference);
                    }
                } else if (currElevator.getDirection().equals(ElevatorMotor.Up)) {
                    if (difference < 0) {
                        differenceUp.put(currElevator.id, difference);
                    }
                } else if (currElevator.getDirection().equals(ElevatorMotor.Stop)) {
                    differenceStopped.put(currElevator.getID(), difference);
                }
            }

        }

        switch (floorButtonDirection) {
            case "DOWN": 
                if (!differenceDown.isEmpty()) {
                    getElevatorFromDifference(differenceDown, origin, true, floor, floorButtonDirection);
                } else if (!differenceStopped.isEmpty()) {
                    getElevatorFromDifference(differenceStopped, origin, true, floor, floorButtonDirection);

                } else {
                    Elevator minElevatorReq = elevators.get(elevatorBeingUsed);
                    int minSum = minElevatorReq.getUpQueue().size() + minElevatorReq.getDownQueue().size();
                    for (Elevator e : elevators) {
                        int sumOfSizeQueues = e.getDownQueue().size() + e.getUpQueue().size();
                        if (sumOfSizeQueues < minSum) {
                            minElevatorReq = e;
                            minSum = sumOfSizeQueues;
                        }
                    }

                    for (Elevator e : elevators) {
                        if (minElevatorReq.getID().equals(e.getID())) {
                            e.addToDown(origin);
                            e.addToDown(floor);
                            e.setElevatorLamps(true, floor);
                            break;
                        }
                    }

                }
                break;

            case "UP": 
                if (!differenceUp.isEmpty()) {
                    getElevatorFromDifference(differenceUp, origin, false, floor, floorButtonDirection);
                } else if (!differenceStopped.isEmpty()) {

                    getElevatorFromDifference(differenceStopped, origin, false, floor, floorButtonDirection);

                } else {
                    Elevator minElevatorReq = elevators.get(elevatorBeingUsed);
                    int minSum = minElevatorReq.getUpQueue().size() + minElevatorReq.getDownQueue().size();
                    for (Elevator e : elevators) {
                        int sumOfSizeQueues = e.getDownQueue().size() + e.getUpQueue().size();
                        if (sumOfSizeQueues < minSum) {
                            minElevatorReq = e;
                            minSum = sumOfSizeQueues;
                        }
                    }

                    for (Elevator e : elevators) {
                        if (minElevatorReq.getID().equals(e.getID())) {
                            e.addToUp(origin);
                            e.addToUp(floor);
                            e.setElevatorLamps(false, floor);
                            break;
                        }
                    }

                }
                break;

            default:
                break;
        }

    }

    /**
     * Check send basically organizes which QUEUE is going to the elevator first
     *
     * @return
     */
    public synchronized int checkSend(Elevator elevator) {
        int toVisit = -1;

        if (elevator.getUpQueue().isEmpty() && !elevator.getDownQueue().isEmpty()) {
            elevator.setDirection(ElevatorMotor.Down);
        }

        else if (!elevator.getUpQueue().isEmpty() && elevator.getDownQueue().isEmpty()) {
            elevator.setDirection(ElevatorMotor.Up);
        }

        if (!elevator.getUpQueue().isEmpty() || !elevator.getDownQueue().isEmpty()) {
            if (elevator.getDirection() == ElevatorMotor.Up) {
                toVisit = (Integer) elevator.getUpQueue().get(0);
                elevator.removeUp();
            }
            else if (elevator.getDirection() == ElevatorMotor.Down) {
                toVisit = (Integer) elevator.getDownQueue().get(0);
                elevator.removeDown();
            }
        }

        return toVisit;
    }


    /**
     * Initializes the packet sockets and the number of elevators
     *
     * @param numOfElevators
     */
    public void InitializePort(int numOfElevators) {
        maxElevator = numOfElevators;
        maxElevator--;
        byte[] data = new byte[100];
        receivePacket = new DatagramPacket(data, data.length);
        try {
            sendReceiveSocketFloor.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        portFloor = receivePacket.getPort();
        addressFloor = receivePacket.getAddress();

        String dataString = "" + numOfElevators;
        byte[] toSend = dataString.getBytes();
        message = "";
        this.sendPacket = new DatagramPacket(toSend, toSend.length, addressFloor, portFloor);
        try {
            this.sendReceiveSocketFloor.send(this.sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        while (elevators.size() != numOfElevators) {
            data = new byte[100];
            receivePacket = new DatagramPacket(data, data.length);
            try {
                sendReceiveSocketElevators.receive(receivePacket);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            String id = new String(receivePacket.getData(), 0, this.receivePacket.getLength());
            elevators.add(new Elevator(id, receivePacket.getPort(), receivePacket.getAddress(), 1));
            if (elevators.size() == numOfElevators) {
                break;
            }
        }

        for(int i = 0; i < elevators.size(); i++){
            time.add(getTime());
        }

        System.out.println(Timestamp.from(Instant.now()) + "  -  Floor port is: " + portFloor + " and address is: " + addressFloor);
        for (int z = 0; z < elevators.size(); z++) {
            Elevator temp = elevators.get(z);
            System.out
                    .println(temp.getID() + " port is: " + temp.getPort() + " and address is: " + temp.getAddress());
        }
    }

    /**
     * A to string method for the byte array
     * Converts byte array to string
     *
     * @return string
     */
    private static String toString(byte[] temp) {
        StringBuilder builder = new StringBuilder();
        for (byte b : temp) {
            builder.append(String.format("%02X ", b));
        }
        return builder.toString();
    }

    public void closeSockets() {
        this.sendReceiveSocketElevators.close();
        this.sendReceiveSocketFloor.close();
    }

    public String getDataFromElevator() {
        return this.dataFromElevator;
    }

    public boolean isEmptyFloor() {
        return this.emptyFloor;
    }

    public boolean isEmptyElevator() {
        return this.emptyElevator;
    }

    public SchedulerState getCurrentState1() {
        return this.currentState1;
    }

    public SchedulerState getCurrentState2() {
        return this.currentState2;
    }

    public ElevatorMotor getDirection() {
        return this.direction;
    }

    public int getFloorToVisit() {
        return this.floorToVisit;
    }

    public int getCurrentFloor() {
        return this.currentFloor;
    }

    public String getIsDataFromFloor() {
        return this.isDataFromFloor;
    }

    public DatagramPacket getReceivePacket() {
        return this.receivePacket;
    }

    public DatagramPacket getSendPacket() {
        return this.sendPacket;
    }

    public DatagramSocket getFloorSocket() {
        return this.sendReceiveSocketFloor;
    }

    public DatagramSocket getElevatorSocket() {
        return this.sendReceiveSocketElevators;
    }

    public ArrayList<Elevator> getElevators() {
        return this.elevators;
    }

    public void setElevators(ArrayList<Elevator> e) {
        this.elevators = e;
    }

    public ArrayList<String> gettime() {
        return time;
    }

    /**
     * Initializes and runs the thread
     *
     * @param args
     */
    public static void main(String[] args) {
        Scheduler scheduler = new Scheduler();
        
        scheduler.InitializePort(file.getNumElevators());
        scheduler.sendAndReceive();

    }
}
