import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFileChooser;

/**
 * 
 */

/**
 * @author Ali Fahd
 *
 */
public class FloorSubsytem implements Runnable {
	private static ArrayList<String> requests = new ArrayList<String>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFileChooser fileChooser = new JFileChooser(); 
		
		int response = fileChooser.showOpenDialog(null);
		
		if(response == JFileChooser.APPROVE_OPTION) {
			File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
			String fileContent = "";
			try {
				Scanner scan = new Scanner(file);
				while(scan.hasNextLine()) {
					requests.add(scan.nextLine());
				}
				System.out.println(requests);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
