/**
 * @author Andy Ngo, Karim Mahrous
 * Version: 4.0V
 * 
 * Description:
 * The purpose of this class is for the elevator thread and it will check the floor request array and make sure
 * if there are any requests. If there are requests the elevator will check to go up and down or to stop when it arrives.
 * A state machine is implemented to organize some parts of the code.
 * This class will be synchronizing with the scheduler class.
 */

import java.io.IOException;
import java.net.*;
import java.sql.Timestamp;
import java.util.*;

public class ElevatorSubsystem implements Runnable 
{

	//Variables that will read the configure file
    private static ReadPropertyFile rpf = new ReadPropertyFile();
    private static long door_times = rpf.getOperateDoorTimes();
    private static long floor_times = rpf.getFloorTravelTimes();
    
    private int floor = 1; 					// The floor that the elevator is on
    private ElevatorStates currentState; 	// The current state of the elevator
    private ElevatorMotor motorState; 		// The motor state is whether the elevator is moving
    private boolean doorOpen; 				// Door boolean state
    private FloorRequest data; 				// The current request
    private int[] buttons; 					// Array of elevator buttons
    private boolean[] lampStatus; 		// Array of lamps
    private ElevatorMotor directionLamp; 	// The direction that the elevator is going that will be show on the lamp
    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket sendReceiveSocket;
    private String[] packetString = new String[2];
    private String id;
    private String msg;
    private String msg1;
    private String msg2;
    private static int countWaiting = 0;
    private long time = 0;
    private long time2 = 0;
    private long x;
    private long currTime;
    private int moving = 1;
    private int currFloor, destFloor;
    private int errorSelect = 0;

    /**
     * Instantiates the variables and constructor
     *
     * @param id the id of the elevator thread
     */
    public ElevatorSubsystem(String id) 
    {
        System.out.println(door_times);
        this.id = id;
        currentState = ElevatorStates.INITIAL_STATE;
        motorState = ElevatorMotor.Stop;
        data = new FloorRequest();
        doorOpen = true;
        lampStatus= new boolean[rpf.getNumFloors()];
        currFloor = 1;
        destFloor = 0;

        try 
        {
            sendReceiveSocket = new DatagramSocket();
        } catch (SocketException se) 
        {
            se.printStackTrace();
            System.exit(1);
        }

        //Initializing the lamps, buttons, direction lamp
        for (int i = 0; i < lampStatus.length; ++i) 
        {
            lampStatus[i] = false;
        }

        buttons = new int[rpf.getNumFloors()];
        for (int i = 0; i < this.buttons.length; ++i) 
        {
            buttons[i] = i + 1;
        }

        directionLamp = ElevatorMotor.Stop;
    }

    /**
     * Initialize the packets and sockets for elevator
     */
    public void init() 
    {
        byte[] sendData = new byte[100];

        sendData = this.id.getBytes();
        try 
        {
            this.sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), rpf.getElevatorPort());
        } catch (UnknownHostException e) 
        {
            e.printStackTrace();
            System.exit(1);
        }

        try 
        {
            this.sendReceiveSocket.send(this.sendPacket);
        } catch (IOException e) 
        {
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
    private void parse(String direction, String type) 
    {
        //Sets the motor state of the elevator
        if (type.equals("Direction")) 
        {
            if (this.currFloor < this.destFloor) 
            {
                this.motorState = ElevatorMotor.Up;
            } 
            else if (this.currFloor > this.destFloor) 
            {
                this.motorState = ElevatorMotor.Down;
            } 
            else 
            {
                this.motorState = ElevatorMotor.Stop;
            }
        }
        //If type is floor number, then turn on the direction lamps
        if (type.equals("Floor Number")) 
        {
            this.lampStatus[Integer.parseInt(direction) - 1] = true;
            this.destFloor = Integer.parseInt(direction);
        }

    }

    /**
     * State Machine that will complete ElevatorSubsystem operations
     */
    public void stateMachine() 
    {

        switch (currentState) 
        {
            case INITIAL_STATE: // Elevator stopped with doors open

                //create a byte array and try to receive packet
                byte[] data = new byte[100];
                receivePacket = new DatagramPacket(data, data.length);
                try 
                {
                    sendReceiveSocket.receive(receivePacket);
                } catch (IOException e) 
                {
                    e.printStackTrace();
                    System.exit(1);
                }

                //Convert packet to string
                String receivePacketData = new String(receivePacket.getData(), 0, this.receivePacket.getLength());

                //print the info received
                System.out.println(this.id + " Received: " + new String(receivePacket.getData(), 0, this.receivePacket.getLength()));

                //Process the received info
                if (receivePacketData.equals("waiting")) //if received waiting then, put elevator into initial state
                {  
                    currentState = ElevatorStates.INITIAL_STATE;

                    //Sending the id of the elevator and "waiting" message back so that scheduler know which elevator is available
                    String elevatorWithRequest = id + "-" + receivePacketData;
                    sendElevatorMessage(elevatorWithRequest);

                } 
                else 
                {
                    String[] temp = receivePacketData.split(" ");
                    if (temp[0].equals("error")) //if received error
                    { 
                        errorSelect = Integer.parseInt(temp[1]);
                        //go to state 5
                        currentState = ElevatorStates.ERROR_STATE;
                        time = System.nanoTime();
                        break;
                    }
                    //change state
                    currentState = ElevatorStates.START_STATE;
                    countWaiting = 0;
                }
                //once state is changed, set time to get the initial time of movement start
                time = System.nanoTime();
                break;
            case START_STATE: // doors close
                //Set the timing of the door to close
                if (System.nanoTime() >= (door_times + time)) 
                {
                    doorOpen = false;

                    System.out.println(id + " Door Closed...");

                    //send to scheduler that door is closed and then change state
                    this.packetString = (new String(receivePacket.getData(), 0, this.receivePacket.getLength())).split(" ");
                    parse(this.packetString[1], "Direction");
                    msg1 = id + "-door_closed-" + currFloor;
                    sendElevatorMessage(msg1);
                    currentState = ElevatorStates.MOVE_STATE;
                    time = System.nanoTime();
                } 
                //if door is still in the process of closing
                else 
                { 
                    System.out.println("==== " + id + " Door closing ====");

                    //Send to scheduler that door is closing
                    msg1 = id + "-door_closing";
                    sendElevatorMessage(msg1);
                    //Keep recursively calling the same state if the elevator is still in the process of closing doors
                    currentState = ElevatorStates.START_STATE;
                }
                break;
            case MOVE_STATE: // Transition to moving
                // Turn on lamps
                directionLamp = motorState;
                parse(this.packetString[0], "Floor Number");
                parse(this.packetString[1], "Direction");
                // TODO:Listen to request implementation
                //Elevator Lamps and Buttons are pushed within elevator to add a new request
                //Set to state 3 to start the process of moving between floor timers
                currentState = ElevatorStates.MOVING_STATE;
                time = System.nanoTime();
                time2 = System.nanoTime();
                break;
            case MOVING_STATE: // Elevator moving
                //Calculate the total time taken to from current to destination floor
                x = floor_times * Math.abs(this.destFloor - this.currFloor);
                //Compares time to check if elevator has reached the floor
                if (System.nanoTime() <= (x + time)) 
                {
                    currTime = System.nanoTime();
                    //compares time between floors to iterate one floor after another
                    if (currTime - time2 >= floor_times) 
                    {
                        if (this.currFloor < this.destFloor) // if going up
                        { 
                            floor++;
                            time2 = System.nanoTime();
                        } 
                        else if (this.currFloor > this.destFloor) //if going down
                        {
                            floor--;
                            time2 = System.nanoTime();
                        }
                    }

                    //Send the message of moving to the scheduler
                    msg = id + "-moving-" + (floor + 1);
                    sendElevatorMessage(msg);
                    //recursively call the same state till reached the correct floor
                    currentState = ElevatorStates.MOVING_STATE;
                } 
                //switch states when reached destination floor
                else 
                {
                    time = System.nanoTime(); // reset time before switching states
                    currentState = ElevatorStates.FINAL_STATE;
                }
                break;
            case FINAL_STATE: // reach destination
                //Timing for the doors to open
                if (System.nanoTime() >= (door_times + time)) //if doors are fully opened
                { 
                    //set doors open to true and set the motor sate to stopped
                    doorOpen = true;
                    motorState = ElevatorMotor.Stop;
                    directionLamp = motorState;

                    //Send to scheduler with arrive info
                    this.currFloor = Integer.parseInt(this.packetString[0]);
                    msg = id + "-arrived-" + this.packetString[0];
                    sendElevatorMessage(msg);

                    System.out.println(this.id + " Sent: " + msg);
                    //go back to the initial state
                    currentState = ElevatorStates.INITIAL_STATE;
                } 
                //Keep sending doors opening to scheduler till the doors are fully open
                else 
                {
                    System.out.println(id + " Door Opening...");
                    msg1 = id + "-door_opening";
                    sendElevatorMessage(msg1);
                    //Recursively set the same state till doors are completely open
                    currentState = ElevatorStates.FINAL_STATE;
                }

                break;
            case ERROR_STATE: //Error Handling
                if (errorSelect == -1)  // If error is door is stuck
                {
                	//Simulate door stuck and reset 
                	System.out.println(id + " Door closing...");
                    System.out.println(id + " Door is stuck...");
                                     
                	//Timing for the error
                    if (System.nanoTime() < (door_times + time)) //if doors fully reset
                    { 
                        System.out.println(id + " Reseting Door...");
                        msg2 =  id + "-doorReseting-" + this.currFloor;
                        sendElevatorMessage(msg2);
                        //Recursively set the same state till doors are completely open
                        currentState = ElevatorStates.ERROR_STATE;

                    } 
                    //Keep sending doors opening to scheduler till the doors are fully open
                    else 
                    {
                        msg2 =  id + "-doorReset-" + this.currFloor;
                        errorSelect = 0;
                        currentState = ElevatorStates.INITIAL_STATE;
                        sendElevatorMessage(msg2);
                    }

                } 
                else if (errorSelect == -2) //if fatal error: stuck between floors
                {
                    //Send the data to the scheduler about which floor is elevator stuck on
                    System.out.println("==== " + id + " Stuck at floor " + this.currFloor + " ====");
                    System.out.println("==== " + id + " Shutting down and reallocating queues ====");
                    msg1 = id + "-error-" + this.currFloor;
                    sendElevatorMessage(msg1);
                    errorSelect = -3;
                }
                else if (errorSelect == -3) 
                {
                    msg1 = id + "-error-" + this.currFloor;
                    sendElevatorMessage(msg1);
                }
        }
    }

    private void sendElevatorMessage(String elevatorWithRequest) 
    {
    	byte[] sendData = elevatorWithRequest.getBytes();
        try 
        {
            this.sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), rpf.getElevatorPort());
        } catch (UnknownHostException e) 
        {
            e.printStackTrace();
            System.exit(1);
        }

        try 
        {
            this.sendReceiveSocket.send(this.sendPacket);
        } catch (IOException e) 
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Will call the state machine while the threads are active
     */
    @Override
    public void run() 
    {
        System.out.println(id + " Started!");
        init();
        while (true) 
        {
            this.stateMachine();

            try 
            {
                Thread.sleep(1500L);
            } catch (InterruptedException e) 
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Making getter methods to user for Unit Testing
     */
    public ElevatorMotor getMotorState() 
    {
        return this.motorState;
    }

    public boolean isDoorOpen() 
    {
        return this.doorOpen;
    }

    public ElevatorMotor getDirectionLamp() 
    {
        return this.directionLamp;
    }

    public FloorRequest getData() 
    {
        return this.data;
    }

    public ElevatorStates getCurrentState() 
    {
        return this.currentState;
    }

    /**
     * Main for the elevator subsystem
     * @param args
     */
    public static void main(String[] args) 
    {

        Thread elevator[] = new Thread[rpf.getNumElevators()];

        for (int i = 0; i < rpf.getNumElevators(); i++) 
        {
            elevator[i] = new Thread(new ElevatorSubsystem("Elevator" + (i + 1)), "Elevator " + (i + 1));
            elevator[i].start();
        }
    }
}
