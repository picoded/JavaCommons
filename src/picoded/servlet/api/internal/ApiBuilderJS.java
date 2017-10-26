package picoded.servlet.api.internal;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import picoded.servlet.api.ApiBuilder;

public class ApiBuilderJS {

	public static String getJsLib() {
		Scanner scanner = null;
		StringBuilder fileContents = new StringBuilder();
		try {
			File file = new File("ApiBuilderJS.js");
			if (ApiBuilderJS.class.getResource("ApiBuilderJS.js") != null) {
				file = new File(ApiBuilderJS.class.getResource("ApiBuilderJS.js").getPath());
			}
			InputStream is = ApiBuilderJS.class.getResourceAsStream("ApiBuilderJS.js");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				fileContents.append(line + "\n");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return (fileContents == null) ? "" : fileContents.toString();
	}

	/**
	 * Method to generate the entire Javascript API file contents
	 */
	public static String generateApiJs(ApiBuilder builder, String hostpath, String apiNamespace) {
		String versionStr = builder.versionStr();

		String generateJSScript = "var api = (function() {\n";
		generateJSScript += getJsLib();
		generateJSScript += "\tapicore.baseURL(\""+("/" + apiNamespace + "/" + versionStr + "/").replaceAll("//","/")+"\");\n"
			+ "\tapicore.setEndpointMap({\n";

		// Generating endpoints
		for (String endpoint : builder.keySet()) {
			endpoint = endpoint.replaceAll("/", "\\.");
			generateJSScript += "\t\t\"" + endpoint + "\" : [],\n";
		}
		// Add an empty endpoint for closing sake @TODO: fix this plox
		generateJSScript += "\t\t\"\" : []\n";
		// System.out.println(generateJSScript+"\n\n it is completed here "+index);
		// generateJSScript = generateJSScript.substring(0, index) + "\n";
		generateJSScript += "\t});\n" + "\treturn api;\n" + "})();\n";
		return generateJSScript;
	}
}
