/*
 * Author: Andy Ngo
 * Student ID: 101132278
 * Version: 1.0V
 * 
 * Description:
 * The purpose of this class is for the elevator motor movement and it is just a enumeration class that will be used by the elevator thread
 */

public enum Elevator_Motor{

	Up
	{
		@Override
		public String toString()
		{
			return "up";
		}
	},
	
	Stop
	{
		@Override
		public String toString()
		{
			return "stop";
		}
	},
	
	Down
	{
		@Override
		public String toString()
		{
			return "down";
		}
	};
	
}
