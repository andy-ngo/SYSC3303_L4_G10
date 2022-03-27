import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ReadPropertyFile {
    Properties property;
    private static final long NANOSEC = 1000000000;

    /**
     * Initializes the variables and gets the input configuration file
     */
    public ReadPropertyFile() {
        property = new Properties();

        // Reading from file
        try {
            FileInputStream ip = new FileInputStream("config.properties");
            property.load(ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the floor port from the config file
     *
     * @return floor port
     */
    public int getFloorPort() {
        return Integer.parseInt(this.property.getProperty("floor_port"));
    }

    /**
     * Gets the elevator port from the config file
     *
     * @return elevator port
     */
    public int getElevatorPort() {
        return Integer.parseInt(this.property.getProperty("elevator_port"));
    }

    /**
     * Gets the num of elevator from the config file
     *
     * @return num of elevator
     */
    public int getNumElevators() {
        return Integer.parseInt(this.property.getProperty("elevators"));
    }

    /**
     * Gets the num of floors from the config file
     *
     * @return num of floors
     */
    public int getNumFloors() {
        return Integer.parseInt(this.property.getProperty("floors"));
    }

    /**
     * Gets the time of opening and closing door from the config file
     *
     * @return the open/close door time
     */
    public long getOperateDoorTimes() {
        return Long.parseLong(this.property.getProperty("time_open_close_doors_sec")) * NANOSEC;
    }

    /**
     * Gets the time an elevator takes from one floor to another from the config file
     *
     * @return time between floors taken by the elevator
     */
    public long getFloorTravelTimes() {
        return Long.parseLong(this.property.getProperty("time_between_floors_sec")) * NANOSEC;
    }
    
    /**
     * Gets the path of the floor data json file from the config file
     *
     * @return path of json file where serialized floor data is stored
     */
    public String getJsonPath() {
        return this.property.getProperty("json_path");
    }
}
