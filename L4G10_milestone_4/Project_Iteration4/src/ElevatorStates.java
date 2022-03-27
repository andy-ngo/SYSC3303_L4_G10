/*
 * Author: Andy Ngo
 * Student ID: 101132278
 * Version: 1.0V
 * 
 * Description:
 * The purpose of this class is for the elevator states and it is just a enumeration class that will be used by the elevator thread
 */

public enum ElevatorStates 
{
	INITIAL_STATE, 	// Elevator stopped with doors open
    START_STATE, 	// Close doors
    MOVE_STATE,		// Will transition into the moving state by closing the door
    MOVING_STATE, 	// Elevator moving
    FINAL_STATE,	// Reach destination
    ERROR_STATE;	// Handles errors
}
