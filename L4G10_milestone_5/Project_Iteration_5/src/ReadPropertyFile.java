import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/*
 * Information from config file for settings on this program
 * */
public class ReadPropertyFile {
    Properties property;
    private static final long NANOSEC = 1000000000;

    /**
     * constructor, gets input config file
     */
    public ReadPropertyFile() {
        property = new Properties();

        try {
            FileInputStream ip = new FileInputStream("config.properties");
            property.load(ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the floor port from config file
     *
     * @return int floor port
     */
    public int getFloorPort() {
        return Integer.parseInt(this.property.getProperty("floor_port"));
    }

    /**
     * Gets the elevator port from config file
     *
     * @return int elevator port
     */
    public int getElevatorPort() {
        return Integer.parseInt(this.property.getProperty("elevator_port"));
    }

    /**
     * Gets the num of elevator from config file
     *
     * @return int num of elevator
     */
    public int getNumElevators() {
        return Integer.parseInt(this.property.getProperty("elevators"));
    }

    /**
     * Gets the num of floors from config file
     *
     * @return int num of floors
     */
    public int getNumFloors() {
        return Integer.parseInt(this.property.getProperty("floors"));
    }

    /**
     * Gets door timings from config file
     *
     * @return long door time
     */
    public long getDoorTimes() {
        return Long.parseLong(this.property.getProperty("time_open_close_doors_sec")) * NANOSEC;
    }

    /**
     * Gets the elevator timing from config file
     *
     * @return long time between floors
     */
    public long getFloorTravelTimes() {
        return Long.parseLong(this.property.getProperty("time_between_floors_sec")) * NANOSEC;
    }
}
