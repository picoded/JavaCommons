package picoded.servlet.api.internal;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class ApiBuilderJS {

	public static String getJsLib() {
		Scanner scanner = null;
		try {
			File file = new File(ApiBuilderJS.class.getResource("ApiBuilderJS.js").getPath());
			StringBuilder fileContents = new StringBuilder((int) file.length());
			scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				fileContents.append(scanner.nextLine()
						+ System.getProperty("line.separator"));
			}
			return fileContents.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(scanner !=null){
				scanner.close();
			}
		}
		return null;
	}
}
