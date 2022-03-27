

import java.io.IOException;
import java.net.*;
import java.sql.Timestamp;
import java.util.*;

public class ElevatorSubsystem implements Runnable {

    private int location = 1; // current location of the elevator
    private ElevatorStates currentState; // current state of the elevator
    private Direction motorState; // The motor state is whether the elevator is moving
    private boolean doorOpen; // Whether the door is open
    private FloorRequest data; // The current request
    private int[] elevatorButtons; // array of buttons
    private boolean[] elevatorLamps; // array of lamps
    private Direction directionLamp; // Directional lamp
    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket sendReceiveSocket;
    private String[] packetString = new String[2];
    private String name;
    private static int countWaiting = 0;
    private long time = 0;
    private long time2 = 0;
    private int moving = 1;
    private int currFloor, destFloor;
    private int errorSelect = 0;

    //Variables to read config file
    private static ReadPropertyFile r = new ReadPropertyFile();
    private static long time_open_close_doors = r.getTimeToOpenCloseDoors();
    private static long time_between_floors = r.getTimeBetweenFloors();

    /**
     * Instantiates the variables
     *
     * @param name Name of the thread
     */
    public ElevatorSubsystem(String name) {
        // this.scheduler = scheduler;
        System.out.println(time_open_close_doors);
        this.name = name;
        currentState = ElevatorStates.INITIAL_STATE;
        motorState = Direction.STOPPED;
        data = new FloorRequest();
        doorOpen = true;
        elevatorLamps = new boolean[r.getNumFloors()];
        currFloor = 1;
        destFloor = 0;


        try {
            sendReceiveSocket = new DatagramSocket();
        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }

        // Initializing the lamps
        for (int i = 0; i < elevatorLamps.length; ++i) {
            elevatorLamps[i] = false;
        }

        // Initializing the buttons
        elevatorButtons = new int[r.getNumFloors()];
        for (int i = 0; i < this.elevatorButtons.length; ++i) {
            elevatorButtons[i] = i + 1;
        }

        // Initializing the directionLamp
        directionLamp = Direction.STOPPED;
    }

    /**
     * Init packet and socket for elevator
     */
    public void Initialize() {
        byte[] toSend = new byte[100];

        toSend = this.name.getBytes();
        try {
            this.sendPacket = new DatagramPacket(toSend, toSend.length, InetAddress.getLocalHost(), r.getElevatorPort());
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
    }

    /**
     * Depending on the current state, sets the direction in which the elevator
     * needs to move or if stationary, then turns on lamps
     *
     * @param type Used to identify whether the elevator needs directions or need
     *             the floor lamps.
     */
    private void parseData(String direction, String type) {
        //Sets the motor state of the elevator
        if (type.equals("Direction")) {
            if (this.currFloor < this.destFloor) {
                this.motorState = Direction.UP;
            } else if (this.currFloor > this.destFloor) {
                this.motorState = Direction.DOWN;
            } else {
                this.motorState = Direction.STOPPED;
            }
        }
        //If type is floor number, then turn on the direction lamps
        if (type.equals("Floor Number")) {
            this.elevatorLamps[Integer.parseInt(direction) - 1] = true;
            this.destFloor = Integer.parseInt(direction);
        }

    }

    /**
     * State Machine that will complete ElevatorSubsystem operations
     */
    public void stateMachine() {

        switch (currentState) {
            case INITIAL_STATE: // Elevator stopped with doors open

                //create a byte array and try to receive packet
                byte[] data = new byte[100];
                receivePacket = new DatagramPacket(data, data.length);
                try {
                    sendReceiveSocket.receive(receivePacket);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }

                //Convert packet to string
                String receivePacketData = new String(receivePacket.getData(), 0, this.receivePacket.getLength());

                //print the info received
                if(receivePacketData != "waiting") {
                    System.out.println(this.name + " Received: " + receivePacketData);                	
                }

                //Process the received info
                if (receivePacketData.equals("waiting")) {  //if received waiting then, put elevator into initial state
                    currentState = ElevatorStates.INITIAL_STATE;

                    //Sending the name of the elevator and "waiting" message back so that scheduler know which elevator is available
                    String elevatorWithRequest = name + "-" + receivePacketData;
                    sendElevatorMessage(elevatorWithRequest);

                } else {
                    String[] temp = receivePacketData.split(" ");
                    if (temp[0].equals("error")) { //if received error
                        errorSelect = Integer.parseInt(temp[1]);
                        //go to state 5
                        currentState = ElevatorStates.STATE_5;
                        time = System.nanoTime();
                        break;
                    }
                    //change state
                    currentState = ElevatorStates.STATE_1;
                    countWaiting = 0;
                }
                //once state is changed, set time to get the initial time of movement start
                time = System.nanoTime();
                break;
            case STATE_1: // doors close
                //Set the timing of the door to close
                if (System.nanoTime() >= (time_open_close_doors + time)) {
                    doorOpen = false;

                    System.out.println(name + " door closed");

                    //send to scheduler that door is closed and then change state
                    this.packetString = (new String(receivePacket.getData(), 0, this.receivePacket.getLength())).split(" ");
                    parseData(this.packetString[1], "Direction");
                    String msg1 = name + "-door_closed-" + currFloor;
                    sendElevatorMessage(msg1);
                    currentState = ElevatorStates.STATE_2;
                    time = System.nanoTime();
                } else { //if door is still in the process of closing
                    System.out.println(name + " door closing");

                    //Send to scheduler that door is closing
                    String msg1 = name + "-door_closing";
                    sendElevatorMessage(msg1);
                    //Keep recursively calling the same state if the elevator is still in the process of closing doors
                    currentState = ElevatorStates.STATE_1;
                }
                break;
            case STATE_2: // Transition to moving
                // Turn on lamps
                directionLamp = motorState;
                parseData(this.packetString[0], "Floor Number");
                parseData(this.packetString[1], "Direction");
                // TODO:Listen to request implementation
                //Elevator Lamps and Buttons are pushed within elevator to add a new request
                //Set to state 3 to start the process of moving between floor timers
                currentState = ElevatorStates.STATE_3;
                time = System.nanoTime();
                time2 = System.nanoTime();
                break;
            case STATE_3: // Elevator moving
                //Calculate the total time taken to from current to destination floor
                long x = time_between_floors * Math.abs(this.destFloor - this.currFloor);
                //Compares time to check if elevator has reached the floor
                if (System.nanoTime() <= (x + time)) {
                    long currTime = System.nanoTime();
                    //compares time between floors to iterate one floor after another
                    if (currTime - time2 >= time_between_floors) {
                        if (this.currFloor < this.destFloor) { // if going up
                            location++;
                            time2 = System.nanoTime();
                        } else if (this.currFloor > this.destFloor) {//if going down
                            location--;
                            time2 = System.nanoTime();
                        }
                    }

                    //Send the message of moving to the scheduler
                    String msg = name + "-moving-" + (location + 1);
                    sendElevatorMessage(msg);
                    //recursively call the same state till reached the correct floor
                    currentState = ElevatorStates.STATE_3;
                } else {//switch states when reached destination floor
                    time = System.nanoTime(); // reset time before switching states
                    currentState = ElevatorStates.STATE_4;
                }
                break;
            case STATE_4: // reach destination
                //Timing for the doors to open
                if (System.nanoTime() >= (time_open_close_doors + time)) { //if doors are fully opened
                    //set doors open to true and set the motor sate to stopped
                    doorOpen = true;
                    motorState = Direction.STOPPED;
                    directionLamp = motorState;

                    //Send to scheduler with arrive info
                    this.currFloor = Integer.parseInt(this.packetString[0]);
                    String msg = name + "-arrived-" + this.packetString[0];
                    sendElevatorMessage(msg);

                    System.out.println(this.name + " Sent: " + msg);
                    //go back to the initial state
                    currentState = ElevatorStates.INITIAL_STATE;
                } else {//Keep sending doors opening to scheduler till the doors are fully open
                    System.out.println(name + " door opening");
                    String msg1 = name + "-door_opening";
                    sendElevatorMessage(msg1);
                    //Recursively set the same state till doors are completely open
                    currentState = ElevatorStates.STATE_4;
                }

                break;
            case STATE_5: //Error Handling
                if (errorSelect == -1) { // If error is door is stuck
                    
                	String msg2;
                	
                	//Simulate door stuck and reset 
                	System.out.println(name + " door closing");
                    System.out.println(name + " Door is stuck");
                                     
                	//Timing for the error
                    if (System.nanoTime() < (time_open_close_doors + time)) { //if doors fully reset
                        System.out.println(name + " reseting Door");
                        msg2 =  name + "-doorReseting-" + this.currFloor;
                        sendElevatorMessage(msg2);
                        //Recursively set the same state till doors are completely open
                        currentState = ElevatorStates.STATE_5;

                    } else {//Keep sending doors opening to scheduler till the doors are fully open
                        msg2 =  name + "-doorReset-" + this.currFloor;
                        errorSelect = 0;
                        currentState = ElevatorStates.INITIAL_STATE;
                        sendElevatorMessage(msg2);
                    }

                } else if (errorSelect == -2) {//if fatal error: stuck between floors
                    //Send the data to the scheduler about which floor is elevator stuck on
                    System.out.println(name + " Stuck at floor " + this.currFloor);
                    System.out.println(name + " Shutting down and re allocating queues");
                    String msg1 = name + "-error-" + this.currFloor;
                    sendElevatorMessage(msg1);
                    errorSelect = -3;
                } else if (errorSelect == -3) {
                    String msg1 = name + "-error-" + this.currFloor;
                    sendElevatorMessage(msg1);
                }
        }
    }

    private void sendElevatorMessage(String elevatorWithRequest) {
//    	String msg = elevatorWithRequest + "-" + String.valueOf(new Timestamp(System.currentTimeMillis()));
//        byte[] toSend = msg.getBytes();
    	byte[] toSend = elevatorWithRequest.getBytes();
        try {
            this.sendPacket = new DatagramPacket(toSend, toSend.length, InetAddress.getLocalHost(), r.getElevatorPort());
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
    }

    /**
     * Calls the state machine continuously while thread is active
     */
    @Override
    public void run() {
        System.out.println(name + " started!");
        Initialize();
        while (true) {
            this.stateMachine();

            try {
                Thread.sleep(1500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Getters for Unit Testing
     */
    public Direction getMotorState() {
        return this.motorState;
    }

    public boolean isDoorOpen() {
        return this.doorOpen;
    }

    public Direction getDirectionLamp() {
        return this.directionLamp;
    }

    public FloorRequest getData() {
        return this.data;
    }

    public ElevatorStates getCurrentState() {
        return this.currentState;
    }

    /**
     * Enum containing all the states
     */
    public static enum ElevatorStates {
        INITIAL_STATE, // Elevator stopped with doors open
        STATE_1, // Close doors
        STATE_2, // Elevator moving
        STATE_3, // Reach destination
        STATE_4,
        STATE_5;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        Thread elevatorThreads[] = new Thread[r.getNumElevators()];

        for (int i = 0; i < r.getNumElevators(); i++) {
            elevatorThreads[i] = new Thread(new ElevatorSubsystem("Elevator" + (i + 1)), "Elevator" + (i + 1));
            elevatorThreads[i].start();
        }
    }
}
