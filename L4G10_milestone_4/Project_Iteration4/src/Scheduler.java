import java.io.IOException;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Scheduler {

    public ElevatorMotor direction;
    private String isDataFromFloor, dataFromElevator;
    private int floorToVisit, portFloor, currentFloor;
    private boolean emptyFloor, emptyElevator;
    private SchedulerStates currentState1, currentState2;
    private DatagramPacket receivePacket, sendPacket;
    private DatagramSocket sendReceiveSocketFloor, sendReceiveSocketElevators;
    private InetAddress addressFloor;
    private ArrayList<Elevator> elevators;
    private ArrayList<Elevator> elevatorsStuck;
    private ArrayList<String> timeStamps;
    private int waiting = 0;
    private int elevatorBeingUsed = 0;
    private int maxElevator = 0;
    private int count = 0;
    private String mess;
    private Random rand;

    private static ReadPropertyFile r = new ReadPropertyFile();

    /**
     * Enum for the states
     */
    public enum SchedulerStates {
        STATE_1,
        STATE_2;
    }

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

        currentState1 = SchedulerStates.STATE_1;
        currentState2 = SchedulerStates.STATE_2;

        elevators = new ArrayList<>();
        elevatorsStuck = new ArrayList<>();
        timeStamps = new ArrayList<>();

        try {
            sendReceiveSocketFloor = new DatagramSocket(r.getFloorPort());
            sendReceiveSocketElevators = new DatagramSocket(r.getElevatorPort());
        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }

    }
    
//    private void setView(ElevatorView e) {
//    	this.view = e;
//    }

    public void updateTimeStamp(int elevatorNum){
        timeStamps.set(elevatorNum,getTime());
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
        byte[] data = new byte[100];
        receivePacket = new DatagramPacket(data, data.length);
        while (true) {
            if (count == 2) {
                elevatorChange();
                count = 0;
            }
            this.receiveStateMachine(data);
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
            case STATE_1: //Send to Elevator
                for (int i = 0; i <= maxElevator; i++) {
                    sendToElevator();
                }
                currentState1 = SchedulerStates.STATE_2;
                break;
            case STATE_2://Send to floor
                sendToFloor();
                currentState1 = SchedulerStates.STATE_1;
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
            case STATE_1://Receive from elevator
                for (int i = 0; i <= maxElevator; i++) {
                    boolean b = receiveFromElevator(data);
                    if (!b) {
                        i--;
                    }
                }
                currentState2 = SchedulerStates.STATE_2;
                break;
            case STATE_2: //receive from floor
                receiveFromFloor(data);
                currentState2 = SchedulerStates.STATE_1;
                break;
        }
    }

    /**
     * Receives data from the floor
     *
     * @param data An ready to go message or nothing
     */
    public synchronized void receiveFromFloor(byte[] data) {
        //Try to receive packet from floor
        try {
            sendReceiveSocketFloor.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //turn packet to string and split it into an array of strings
        String name = new String(receivePacket.getData(), 0, this.receivePacket.getLength());
        String[] cut = name.split(" ");

        //if string is a floor request
        if (!cut[0].equals("go")) {
            if (!cut[0].equals("error")) {//if floor request is not an error
                //Sort the floor request to one of the elevators
                checkPriority(Integer.parseInt(cut[1]), cut[2], Integer.parseInt(cut[3]));
            } else {//If error than select one of the elevators to handle error
            	int temp;
                do { //looping until it gets an elevator that is not erroring out
                	temp = rand.nextInt(elevators.size());
                }while(elevators.get(temp).getError() != 0);

            	elevators.get(temp).setError(Integer.parseInt(cut[1]));//Sets type of error

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
        //Gets the elevator in use and tries to receive packet
        Elevator temp = new Elevator(); //elevators.get(elevatorBeingUsed);
        try {
            sendReceiveSocketElevators.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Converts the packet to string and splits it into an array of strings
        String name = new String(receivePacket.getData(), 0, this.receivePacket.getLength());
        String[] splitElevatorMsg = name.split("-");
        String[] test = mess.split(" "); //split message string into an array of strings
        
        // Selecting the correct elevator in use
        for(Elevator e : elevators) {
        	if(splitElevatorMsg[0].equals(e.getID())) {
        		temp = e;
        	}
        }
        int elevatorNum = Integer.parseInt(String.valueOf(temp.getID().charAt(temp.getID().length()-1)));
        timeStamps.set(elevatorNum-1,getTime());

        // If message was not blank and was split into an array
        // then append the packet string to the message depending
        // on the info contained within message
        for (String tt : test) {
            String[] test2 = tt.split("-");
            if (test2[0].equals(splitElevatorMsg[0])) {
                if (test2[1].equals("moving") && !splitElevatorMsg[1].equals("moving")) {
                    createMovingMessage(name, test, tt);
                } else {
                    if (test2[1].equals("moving") && splitElevatorMsg[1].equals("moving")) {
                        if (Integer.parseInt(test2[2]) != Integer.parseInt(splitElevatorMsg[2])) {
                            createMovingMessage(name, test, tt);
                        }
                    }
                }
                return false;
            }
        }


        // if elevator hasn't arrived
        if (!splitElevatorMsg[1].equals("arrived")) {
            if (splitElevatorMsg[1].equals("added")) {
                //button pressed, send new instruction to sort into Queues
                this.checkPriority(-1, null, Integer.parseInt(splitElevatorMsg[0]));
            } else {//Elevator in a state of in between  movements
                if (splitElevatorMsg[1].equals("moving")) {//If elevator moving between floor
                	temp.setStatus("moving");
                	temp.setCurrentFloor(Integer.parseInt(splitElevatorMsg[2]));
                	if (mess.equals("")) {
                        //if message in empty, set message as data received from elevator
                        mess = mess + splitElevatorMsg[0] + "-moving-" + splitElevatorMsg[2];
                    } else {
                        //if message not empty, append data received from elevator to message
                        mess = mess + " " + splitElevatorMsg[0] + "-moving-" + splitElevatorMsg[2];
                        waiting--;
                    }
                } else if (splitElevatorMsg[1].equals("waiting")) { //If elevator waiting for new instruction
                	temp.setStatus("waiting");
                	waiting++;
                    if (mess.equals("")) {
                        //if message in empty, set message as data received from elevator
                        mess = mess + splitElevatorMsg[0] + "-waiting";
                    } else {
                        //if message not empty, append data received from elevator to message
                        mess = mess + " " + splitElevatorMsg[0] + "-waiting";
                        waiting--;
                    }
                } else if (splitElevatorMsg[1].equals("door_closing")) { //If elevators door closing
                	temp.setStatus("door_closing");
                	if (mess.equals("")) {
                        //if message in empty, set message as data received from elevator
                        mess = mess + splitElevatorMsg[0] + "-door_closing";
                    } else { //if message not empty, append data received from elevator to message
                        mess = mess + " " + splitElevatorMsg[0] + "-door_closing";
                        waiting--;
                    }

                } else if (splitElevatorMsg[1].equals("door_closed")) { //If elevators door closed
                	temp.setStatus("door_closed");
                	if (mess.equals("")) {
                        //if message in empty, set message as data received from elevator
                        mess = mess + splitElevatorMsg[0] + "-door_closed-" + splitElevatorMsg[2];
                    } else {
                        //if message not empty, append data received from elevator to message
                        mess = mess + " " + splitElevatorMsg[0] + "-door_closed-" + splitElevatorMsg[2];
                        waiting--;
                    }

                } else if (splitElevatorMsg[1].equals("door_opening")) { //If elevator doors opening
                	temp.setStatus("door opening");
                	if (mess.equals("")) {
                        //if message in empty, set message as data received from elevator
                        mess = mess + splitElevatorMsg[0] + "-door_opening";
                    } else {
                        //if message not empty, append data received from elevator to message
                        mess = mess + " " + splitElevatorMsg[0] + "-door_opening";
                        waiting--;
                    }
                } else if (splitElevatorMsg[1].equals("error")) {
                	if(temp.getError() == -1) {
                		temp.setStatus("doors stuck");
                	}else if(temp.getError() == -2){
                		temp.setStatus("stuck between floors");
                		 elevatorsStuck.add(temp);
                	}

                    if (mess.equals("")) {
                        mess = mess + splitElevatorMsg[0] + "-error-" + splitElevatorMsg[2];
                    } else {
                        //if message not empty, append data received from elevator to message
                        mess = mess + " " + splitElevatorMsg[0] + "-error-" + splitElevatorMsg[2];
                        waiting--;
                    }
                } else if (splitElevatorMsg[1].equals("doorReset")) {
                	temp.setStatus("doors reset");
                	temp.setError(0);
                    if (mess.equals("")) {
                        mess = mess + splitElevatorMsg[0] + "-waiting-" + splitElevatorMsg[2];
                    } else {
                        //if message not empty, append data received from elevator to message
                        mess = mess + " " + splitElevatorMsg[0] + "-waiting-" + splitElevatorMsg[2];
                        waiting--;
                    }
                }
                else if (splitElevatorMsg[1].equals("doorReseting")) {
                    temp.setStatus("doors reseting");
                    temp.setError(0);
                    if (mess.equals("")) {
                        mess = mess + splitElevatorMsg[0] + "-waiting-" + splitElevatorMsg[2];
                    } else {
                        //if message not empty, append data received from elevator to message
                        mess = mess + " " + splitElevatorMsg[0] + "-waiting-" + splitElevatorMsg[2];
                        waiting--;
                    }
                }
            }
        } else { // if elevator arrived to floor
        	temp.setStatus("arrived");
            temp.setCurrentFloor(Integer.parseInt(splitElevatorMsg[2]));
            if (mess.equals("")) {  //if message in empty, set message as data received from elevator
                mess = mess + name;
            } else { //if message not empty, append data received from elevator to message
                mess = mess + " " + name;
                waiting--;
            }
        }
        //Changes the number of elevator being used
        elevatorChange();
        return true;
    }

    /**
     * Helper method that creates a message with an elevator that is moving
     *
     * @param name
     * @param test
     * @param tt
     */
    private void createMovingMessage(String name, String[] test, String tt) {
        mess = "";
        for (String tt2 : test) {
            if (tt2.equals(tt)) {
                if (mess.equals("")) {
                    mess = mess + name;
                } else {
                    mess = mess + " " + name;
                }
            } else {
                if (mess.equals("")) {
                    mess = mess + tt2;
                } else {
                    mess = mess + " " + tt2;
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
        //create a byte array
        byte[] toSend = new byte[100];
//        System.out.println("Sending");

        //If all elevator in use than wait
        if (waiting == elevators.size()) {
            String dataString = "waiting";
            toSend = dataString.getBytes();
            mess = "";
        } else {//if there is at least one elevator available than setup the data to send
            String dataString = mess;
            toSend = dataString.getBytes();
            mess = "";
        }

        //Create and try to send the packet to the floor subsystem
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
        // send the appropriate floor request based on the elevator

        if (temp.getError() != 0) { //if sending error to elevator
            String e = "error " + temp.getError();
            if(temp.getError() == -1){
                temp.setError(0);
            }
            //temp.setError(0);
            toSend = e.getBytes();
        } else {//if sending floor request to elevator
            int t = checkSend(temp); //get the floor to visit

            if (t == -1) { //if floor to visit is negative then, send wait message to elevator
                String wait = "waiting";
                toSend = wait.getBytes();
            } else { //if floor to visit is a valid floor than send direction and floor to the elevator
                String dat = t + " " + temp.getElevatorMotor();
                temp.addDestination(t);
                toSend = dat.getBytes();
            }
        }

        //Create and try to send the packet to the elevator subsystem
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
    private void getElevatorFromDifference(HashMap<String, Integer> differenceMap, int floor, boolean down, int floor2,
                                           String dir) {
        int minDifference = Collections.min(differenceMap.values());
        for (String currElevatorID : differenceMap.keySet()) {
            if (differenceMap.get(currElevatorID).equals(minDifference)) {
                for (Elevator e : elevators) {
                    if (e.getID().equals(currElevatorID)) {
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
     * @param floorButtonElevatorMotor received from the floor
     * @param floor                is the destination
     */
    public synchronized void checkPriority(int origin, String floorButtonElevatorMotor, int floor) {

        // Splits Elevator into different hashmaps depending on their direction
        HashMap<String, Integer> differenceUp = new HashMap<>();
        HashMap<String, Integer> differenceDown = new HashMap<>();
        HashMap<String, Integer> differenceStopped = new HashMap<>();

        //Calculates the differences and add to above maps
        for (Elevator currElevator : elevators) {
            // difference - used to calculate difference from the elevators current floor
            // and the new floor request
            if (!elevatorsStuck.contains(currElevator) || elevatorsStuck.isEmpty()) {
                int difference = currElevator.getCurrentFloor() - origin;
                if (currElevator.getElevatorMotor().equals(ElevatorMotor.DOWN)) {
                    if (difference > 0) {
                        differenceDown.put(currElevator.getID(), difference);
                    }
                } else if (currElevator.getElevatorMotor().equals(ElevatorMotor.UP)) {
                    if (difference < 0) {
                        differenceUp.put(currElevator.getID(), difference);
                    }
                } else if (currElevator.getElevatorMotor().equals(ElevatorMotor.STOP)) {
                    differenceStopped.put(currElevator.getID(), difference);
                } else {
                    // if direction is not correct

                }
            }

        }

        switch (floorButtonElevatorMotor) {
            case "DOWN": // Elevator is going down
                if (!differenceDown.isEmpty()) {
                    getElevatorFromDifference(differenceDown, origin, true, floor, floorButtonElevatorMotor);
                } else if (!differenceStopped.isEmpty()) {
                    getElevatorFromDifference(differenceStopped, origin, true, floor, floorButtonElevatorMotor);

                } else {
                    // Case where there are no elevators going down or stopped, so we assign the
                    // floor request to an elevator with the least amount of requests
                    //(ie. the size of their up and down queues)
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

            case "UP": // Elevator is going up
                if (!differenceUp.isEmpty()) {

                    getElevatorFromDifference(differenceUp, origin, false, floor, floorButtonElevatorMotor);
                } else if (!differenceStopped.isEmpty()) {

                    getElevatorFromDifference(differenceStopped, origin, false, floor, floorButtonElevatorMotor);

                } else {
                    // Case where there are no elevators going down or stopped, so we assign the
                    // floor request to an elevator with the least amount of requests
                    //(ie. the size of their up and down queues)
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

        //System.out.println("In check send");

        // If up queue is empty and down queue is not empty, set the elevator direction
        // to down
        if (elevator.getUpQueue().isEmpty() && !elevator.getDownQueue().isEmpty()) {
            elevator.setElevatorMotor(ElevatorMotor.DOWN);
        }
        // If up queue is not empty and down queue is empty, set the elevator direction
        // to up
        else if (!elevator.getUpQueue().isEmpty() && elevator.getDownQueue().isEmpty()) {
            elevator.setElevatorMotor(ElevatorMotor.UP);
        }

        // If up queue is not empty or down queue is not empty
        if (!elevator.getUpQueue().isEmpty() || !elevator.getDownQueue().isEmpty()) {
            // If the elevator direction is up, get floor to visit from up queue
            if (elevator.getElevatorMotor() == ElevatorMotor.UP) {
                toVisit = (Integer) elevator.getUpQueue().get(0);
                elevator.removeUp();
            }
            // If the elevator direction is up, get floor to visit from down queue
            else if (elevator.getElevatorMotor() == ElevatorMotor.DOWN) {
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
        mess = "";
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
            String name = new String(receivePacket.getData(), 0, this.receivePacket.getLength());
            elevators.add(new Elevator(name, receivePacket.getPort(), receivePacket.getAddress(), 1));
            if (elevators.size() == numOfElevators) {
                break;
            }
        }

        for(int i = 0; i < elevators.size(); i++){
            timeStamps.add(getTime());
        }

        //prints out the floor and elevator ports and address
        System.out.println("Floor port is: " + portFloor + " and address is: " + addressFloor);
        for (int z = 0; z < elevators.size(); z++) {
            Elevator temp = elevators.get(z);
            System.out
                    .println(temp.getID() + " port is: " + temp.getPort() + " and address is: " + temp.getAddress());
        }
    }

    /* For testing purposes */
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

    public SchedulerStates getCurrentState1() {
        return this.currentState1;
    }

    public SchedulerStates getCurrentState2() {
        return this.currentState2;
    }

    public ElevatorMotor getElevatorMotor() {
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

    public ArrayList<String> getTimeStamps() {
        return timeStamps;
    }

    /**
     * Initializes and runs the thread
     *
     * @param args
     */
    public static void main(String[] args) {
        Scheduler scheduler = new Scheduler();
        
//        ElevatorView e = new ElevatorView(scheduler);

//        scheduler.setView(e);
        scheduler.InitializePort(r.getNumElevators());
        scheduler.sendAndReceive();

    }
}
