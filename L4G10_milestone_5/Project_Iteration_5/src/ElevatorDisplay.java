import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * Class creates GUI for program. In the format of a 5x22 table with elevators in 4 columns. Blue highlights indicate next destination.
 * Doors with yellow caution tape means floor stuck error. Doors half closed mean door stuck error. once a floor stuck error occurs on 
 * a elevator it will not take any more requests.
 */
public class ElevatorDisplay extends JFrame {
	private static final long serialVersionUID = 1L;
	private Scheduler model;
	private Container container;

	public static ReadPropertyFile rpf = new ReadPropertyFile();
	private int columns = rpf.getNumElevators() + 1;
	private int rows = rpf.getNumFloors();
	private int width = 550, height = 850;

	private int currFloor[];
	private JLabel grid[][];
	private Color backgroundColour;

	private String imageName = "";
	private String currImageName = "";
	private Image elevatorImage;
	final JFrame[] elevatorFrames = new JFrame[rpf.getNumElevators()];
	final JFrame[] floorStatuses = new JFrame[rpf.getNumFloors()];
	private HashMap<Integer, ArrayList<Boolean>> arrivalSensors = new HashMap<Integer, ArrayList<Boolean>>();
	private HashMap<Integer, Boolean[]> floorLamps = new HashMap<Integer, Boolean[]>();

	public ElevatorDisplay(Scheduler model) {
		super("Elevator");
		currFloor = new int[columns - 1];
		for (int i = 0; i < columns - 1; i++) {
			currFloor[i] = rows - 1;
		}
		
		this.model = model;
		backgroundColour = new Color(245,245,220);

		container = getContentPane();
		container.setLayout(new GridLayout(rows, columns));
		container.setBackground(backgroundColour);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(width, height);

		initializeMaps();
		initializeGrid();

		setVisible(true);
	}

	/**
	 * Initialize each grid
	 * 
	 */
	private void initializeGrid() {
		grid = new JLabel[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				grid[i][j] = new JLabel();
				grid[i][j].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
				grid[i][j].setVisible(true);
				container.add(grid[i][j]);
			}
			grid[i][0].setText("FLOOR " + String.valueOf(rows - i));
			grid[i][0].setFont(new Font("Courier New", Font.BOLD, 20));
		}

		elevatorImage = new ImageIcon(this.getClass().getResource("elevator_image.png")).getImage();
		Image elevator = elevatorImage.getScaledInstance(width / columns, height / rows, java.awt.Image.SCALE_SMOOTH);

		for (int i = 0; i < rpf.getNumElevators(); i++) {
			grid[rows - 1][i + 1].setIcon(new ImageIcon(elevator));
		}
	}

	private void initializeMaps() {
		floorLamps = new HashMap<Integer, Boolean[]>();
		for (int i = 0; i < rpf.getNumFloors(); i++) {
			Boolean[] b = { false, false };
			floorLamps.put(i + 1, b);
		}
		arrivalSensors = new HashMap<Integer, ArrayList<Boolean>>();
		for (int i = 0; i < rpf.getNumFloors(); i++) {
			ArrayList<Boolean> b = new ArrayList<>();
			for (int j = 0; j < rpf.getNumElevators(); j++) {
				b.add(false);
			}
			arrivalSensors.put(i + 1, b);
		}
	}

	/**
	 * place elevator images in the appropriate spot
	 */
	private void placeElevator(int floorNum, int elevatorNum, String imageName) {
		elevatorImage = new ImageIcon(this.getClass().getResource(imageName)).getImage();
		Image elevator = elevatorImage.getScaledInstance(width / columns, height / rows, java.awt.Image.SCALE_SMOOTH);

		grid[floorNum][elevatorNum].setIcon(new ImageIcon(elevator));
	}

	/**
	 * Refreshes view with latest updates from the model
	 * 
	 */
	public void refresh() {
		ArrayList<Elevator> elevators = model.getElevators();
		ArrayList<String> timestamps = model.getTimeStamps();

		for (Elevator e : elevators) {
			int num = Integer.parseInt(e.getId().split("Elevator")[1]);
			e.setTimestamp(timestamps.get(num - 1));

			if (e.getDestination() != -1) {//highlight elevators destination if there is one
				grid[rows - e.getDestination()][num].setBackground(Color.BLUE);
				grid[rows - e.getDestination()][num].setOpaque(true);
			}

			if (e.getStatus().contains("doors")) {
				imageName = "elevator_doors_stuck.png";
			} else if (e.getStatus().contains("stuck between floors")) {
				imageName = "elevator_error.png";
			} else if (e.getStatus().contains("open") || e.getStatus().contains("close")) {
				imageName = "elevator_door_open.png";
			} else {
				imageName = "elevator_image.png";
			}

			if (e.getStatus().contains("arr") || e.getStatus().contains("wait")) {
				grid[currFloor[num - 1]][num].setOpaque(false);
			}

			if (currFloor[num - 1] != rows - e.getCurrentFloor() || currImageName != imageName) {
				grid[currFloor[num - 1]][num].setIcon(null);
				placeElevator(rows - e.getCurrentFloor(), num, imageName);
				currFloor[num - 1] = rows - e.getCurrentFloor();
				currImageName = imageName;
			}
		}
	}
}
