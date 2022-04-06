/**
 * @author Andy Ngo, Karim Mahrous
 * 
 * Verion: 4.0V
 * 
 * Description:
 * The purpose of this class is for the elevator thread and it will check the floor request array and make sure
 * if there are any requests. If there are requests the elevator will check to go up and down or to stop when it arrives.
 * A state machine is implemented to organize some parts of the code.
 * This class will be synchronizing with the scheduler class.
 */

import java.io.IOException;
import java.net.*;

public class ElevatorSubsystem implements Runnable 
{
	
	//ReadPropertyFile variables
    private static ReadPropertyFile rpf = new ReadPropertyFile();
    private static long doorTimes = rpf.getDoorTimes();
    private static long floorTimes = rpf.getFloorTravelTimes();

    //Initialize variables
    private int floor = 1; 					// The floor that the elevator is on
    private ElevatorStates currentState; 	// The current state of the elevator
    private ElevatorMotor motorState; 		// The motor state is whether the elevator is moving
    private int[] buttons; 					// Array of elevator buttons
    private boolean[] lampStatus; 			// Array of lamps
    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket sendReceiveSocket;
    private String[] packetString = new String[2];
    private String id;						// Will be the id of the elevator
    private String msg,msg1,msg2;			// These message variables will be used to send messages
    private long time_x = 0;				// These time variables will store the times of the elevators
    private long time_y = 0;
    private long travelTime,currentTime;	// Will be used to store the travel time of the elevators
    private int currFloor, destFloor;
    private int errorSelect = 0;

    /**
     * Initialize the variables and constructor
     *
     * @param id the id of the elevator thread
     */
    public ElevatorSubsystem(String id) 
    {
        this.id = id;
        currentState = ElevatorStates.INITIAL_STATE;
        motorState = ElevatorMotor.STOP;
        lampStatus = new boolean[rpf.getNumFloors()];
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

        // Initializing the lamps, buttons, direction lamp
        for (int i = 0; i < lampStatus.length; ++i) 
        {
            lampStatus[i] = false;
        }

        buttons = new int[rpf.getNumFloors()];
        for (int i = 0; i < this.buttons.length; ++i) 
        {
            buttons[i] = i + 1;
        }
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
     * This will be used in the states where the elevator motor needs to have the
     * direction state changed. As well as changing the lamp status
     * 
     * @param motorState, will be used to change the elevator motor state
     * @param type, will be used to identify if the elevator needs direction or the lamp status
     */
    private void parse(String motorState, String type) 
    {
        if (type.equals("ElevatorMotor")) 
        {
            if (this.currFloor < this.destFloor) 
            {
                this.motorState = ElevatorMotor.UP;
            } 
            else if (this.currFloor > this.destFloor) 
            {
                this.motorState = ElevatorMotor.DOWN;
            } 
            else 
            {
                this.motorState = ElevatorMotor.STOP;
            }
        }
        if (type.equals("Floor Number")) 
        {
            this.lampStatus[Integer.parseInt(motorState) - 1] = true;
            this.destFloor = Integer.parseInt(motorState);
        }

    }
    
    /**
     * This method will be used to send any messages from the elevator to the Scheduler
     * 
     * @param elevatorWithRequest the message that will be sent to Scheduler
     */
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
     * This state machine will be used to go through all the elevator operations
     * Each state will have brief descriptions in the ElevatorState class
     */
    public void stateMachine() 
    {
        switch (currentState) 
        {
            case INITIAL_STATE:
                byte[] dateByte = new byte[100];
                receivePacket = new DatagramPacket(dateByte, dateByte.length);
                try 
                {
                    sendReceiveSocket.receive(receivePacket);
                } catch (IOException e) 
                {
                    e.printStackTrace();
                    System.exit(1);
                }
                
                // Will convert the packet to string and print out what was received
                String receivePacketData = new String(receivePacket.getData(), 0, this.receivePacket.getLength());
                if(receivePacketData != "waiting") 
                {
                	System.out.println("\n================  " + this.id + "  ================");
                    System.out.println("       ==      Received: " + new String(receivePacket.getData(), 0, this.receivePacket.getLength()) + "      ==");                	
                }
                
                //The received packet will then be processed
                if (receivePacketData.equals("waiting")) 
                {
                    currentState = ElevatorStates.INITIAL_STATE;
                    String elevatorWithRequest = id + "-" + receivePacketData;
                    sendElevatorMessage(elevatorWithRequest);

                } 
                else 
                {
                    String[] temp = receivePacketData.split(" ");
                    if (temp[0].equals("error")) 
                    {
                        errorSelect = Integer.parseInt(temp[1]);
                        currentState = ElevatorStates.ERROR_STATE;
                        time_x = System.nanoTime();
                        break;
                    }
                    currentState = ElevatorStates.START_STATE;
                }
                time_x = System.nanoTime(); // Upon state change, store the time to get initial time of movement
                break;
                
            case START_STATE:
            	// This will go through the door closing process, once the doors are closed it will notify Scheduler to change states
                if (System.nanoTime() >= (doorTimes + time_x)) 
                {
                    System.out.println("\n================  " + id + "  ================");
                    System.out.println("    ==\t\tDoor Closed \t\t==");
                    this.packetString = (new String(receivePacket.getData(), 0, this.receivePacket.getLength())).split(" ");
                    parse(this.packetString[1], "ElevatorMotor");
                    msg1 = id + "-door_closed-" + currFloor;
                    sendElevatorMessage(msg1);
                    currentState = ElevatorStates.MOVE_STATE;
                    time_x = System.nanoTime();
                } 
                // Door is still in the closing process, will keep calling start state until doors are closed
                else 
                { 
                	System.out.println("\n================  " + id + "  ================");
                	System.out.println("    ==\t\tDoor Closing... \t==");
                    msg1 = id + "-door_closing";
                    sendElevatorMessage(msg1);
                    currentState = ElevatorStates.START_STATE;
                }
                break;
                
            case MOVE_STATE:
                // Elevator lamps will turn on and any buttons pushed will add new requests
                parse(this.packetString[0], "Floor Number");
                parse(this.packetString[1], "ElevatorMotor");
                currentState = ElevatorStates.MOVING_STATE;
                time_x = System.nanoTime();
                time_y = System.nanoTime();
                break;
                
            case MOVING_STATE:
                // This will calculate the travel time between floors
                travelTime = floorTimes * Math.abs(this.destFloor - this.currFloor);
                // Will then be used and compared to see if the elevator has reach its destination
                if (System.nanoTime() <= (travelTime + time_x)) 
                {
                    currentTime = System.nanoTime();
                    
                    // Will be used to compare the times between each floor and will iterate each floor that is travelled to
                    if (currentTime - time_y >= floorTimes) 
                    {
                        if (this.currFloor < this.destFloor) 
                        {
                            floor++;
                            time_y = System.nanoTime();
                            System.out.println("\n================  " + id + "  ================");
                            System.out.println("    ==\t\tGoing up...      \t==");
                        }
                        else if (this.currFloor > this.destFloor) 
                        {
                            floor--;
                            time_y = System.nanoTime();
                            System.out.println("\n================  " + id + "  ================");
                            System.out.println("    ==\t\tGoing Down...     \t==");
                        }
                    }
                    
                    // A message of the current state will be sent to the Scheduler, this will continue till floor is reached
                    msg = id + "-moving-" + (floor + 1);
                    sendElevatorMessage(msg);
                    currentState = ElevatorStates.MOVING_STATE;
                } 
                // Upon reaching the destination floor
                else 
                {
                    time_x = System.nanoTime();
                    currentState = ElevatorStates.FINAL_STATE;
                }
                break;
                
            case FINAL_STATE:
                if (System.nanoTime() >= (doorTimes + time_x)) 
                {
                    motorState = ElevatorMotor.STOP;
                    
                    // Send a message to the Scheduler about the arrival
                    this.currFloor = Integer.parseInt(this.packetString[0]);
                    msg = id + "-arrived-" + this.packetString[0];
                    sendElevatorMessage(msg);
                    System.out.println("\n================  " + this.id + "  ================");
                    System.out.println("    ==    Sent: " + msg + "\t==");
                    currentState = ElevatorStates.INITIAL_STATE;
                } 
                // Door opening still in process, will continue until doors are fully open and notify Scheduler
                else
                {
                	System.out.println("\n================  " + id + "  ================");
                	System.out.println("  ==\t\tDoor Opening... \t==");
                    msg1 = id + "-door_opening";
                    sendElevatorMessage(msg1);
                    currentState = ElevatorStates.FINAL_STATE;
                }

                break;
                
            case ERROR_STATE:
                if (errorSelect == -1)
                {
                	// This will simulate the elevator doors getting stuck
                	System.out.println("\n================  " + id + "  ================");
                	System.out.println("    ==\t\tDoor Closing... \t==");
                    System.out.println("    ==\t\tDoor is Stuck... \t==");
                                     
                	// Begin timer for error time
                    if (System.nanoTime() < (doorTimes + time_x)) 
                    {
                    	System.out.println("\n================  " + id + "  ================");
                    	System.out.println("    ==\t\t" + " Resetting Door... \t==");
                        msg2 =  id + "-doorReseting-" + this.currFloor;
                        sendElevatorMessage(msg2);
                        currentState = ElevatorStates.ERROR_STATE;

                    }
                    // Will keep sending door reset message to Scheduler until it is fixed
                    else 
                    {
                        msg2 =  id + "-doorReset-" + this.currFloor;
                        errorSelect = 0;
                        currentState = ElevatorStates.INITIAL_STATE;
                        sendElevatorMessage(msg2);
                    }

                } 
                else if (errorSelect == -2) 
                {
                    // Will send out the floor that the elevator is stuck on to the Scheduler
                	System.out.println("\n================  " + id + "  ================");
                    System.out.println("    ==      Stuck at floor " + this.currFloor + "\t==");
                    System.out.println(" == Shutting down and reallocating queues ==");
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

    /**
     * Will call the state machine while the threads are active
     */
    @Override
    public void run() 
    {
    	System.out.println("\n================  " + id + "  ==================");
        System.out.println("       ==\t Started! \t==");
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
     * The main function that will run the elevator subsystem
     * @param args
     */
    public static void main(String[] args) 
    {
        Thread elevator[] = new Thread[rpf.getNumElevators()];

        for (int i = 0; i < rpf.getNumElevators(); i++) 
        {
            elevator[i] = new Thread(new ElevatorSubsystem("Elevator" + (i + 1)), "Elevator" + (i + 1));
            elevator[i].start();
        }
    }
}
