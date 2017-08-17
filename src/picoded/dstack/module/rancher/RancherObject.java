package picoded.dstack.module.rancher;

import picoded.struct.GenericConvertMap;
import picoded.conv.*;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import java.util.HashMap;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;

/**
* The setup and methods implemented in this class is specifically for Uilicious's Rancher
* it can be extended and overwritten to cater for any other rancher setup.
*/
public class RancherObject{

  protected String accessKey = "";
  protected String secretKey = "";
  protected String baseURL = "";
  protected String projectID = "";
  protected String catalogTemplate = "";
  protected String templateVersion = "";
  protected String basicAuth = "";
  protected String deployment_server_url = "";
  protected String externalID = "";
  protected String selenium_external_link = "";
  protected String beta_version = "";
  protected String catalog_version = "";

  /**
  * Constructor for RancherObject
  */
  public RancherObject(){
  }

  /**
  * Configure the Rancher set up to be used
  * @param take in the rancher configuration to be set
  */
  public void configureRancher(GenericConvertMap<String, Object> rancherConfig){
    if (rancherConfig.size() <= 0){
      throw new RuntimeException("rancherConfig is empty");
    }
    // Configuration set up for rancher
    accessKey = rancherConfig.getString("accessKey", "");
    secretKey = rancherConfig.getString("secretKey", "");
    baseURL = rancherConfig.getString("baseURL", "");
    projectID = rancherConfig.getString("projectID", "");
    catalogTemplate = rancherConfig.getString("catalogTemplate", "");
    templateVersion = rancherConfig.getString("templateVersion", "");
    deployment_server_url = rancherConfig.getString("deployment_server_url", "");
    selenium_external_link = rancherConfig.getString("selenium_external_link", "");
    beta_version = rancherConfig.getString("beta_version", "2");
    catalog_version = rancherConfig.getString("catalog_version", "1");
    externalID = rancherConfig.getString("externalID", "");
    if (accessKey.isEmpty() || secretKey.isEmpty() || baseURL.isEmpty() || projectID.isEmpty() || catalogTemplate.isEmpty() || templateVersion.isEmpty() || deployment_server_url.isEmpty() || externalID.isEmpty() || selenium_external_link.isEmpty())
      throw new RuntimeException("rancherConfig file is not set up properly. Ensure that there are \n"+
                                  "accessKey, secretKey, baseURL, projectID, catalogTemplate, templateVersion, externalID, selenium_external_link and deployment_server_url are set.");
    String userpass = accessKey+":"+secretKey;
    // the username and password in Base64 for the subsequent requests
    basicAuth = "Basic " + new String(Base64.getEncoder().encodeToString(userpass.getBytes()));
  }

  /**
  * Initialize a URL connection with the URL
  * @param the path to the url to be connected
  */
  public URLConnection initializeConnection(String urlPath){
    URLConnection uc = null;
    try{
      URL url = new URL(urlPath);
      uc = url.openConnection();
      if (uc == null){
        throw new RuntimeException("no url connection formed.");
      }
    }catch(MalformedURLException malformed){
      System.out.println("The URL is malformed.");
    }catch(IOException io){
      System.out.println("Error in the inputstream");
    }
    return uc;
  }

  /**
  * Retrieves the catalog template as specified in the rancherConfig.json file
  */
  public Map<String, Object> retrieveCatalog(){
    URLConnection uc = initializeConnection(baseURL+"/v" + catalog_version + "-catalog/templates/"+catalogTemplate+":"+templateVersion);
    uc.setRequestProperty("Authorization", basicAuth);
    String catalog = "";
    Map<String, Object> catalogMap = new HashMap<>();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"))) {
      for (String line; (line = reader.readLine()) != null;) {
          catalog += line;
      }
      catalogMap = ConvertJSON.toMap(catalog);
    }catch(Exception e){
      System.out.println(e);
      catalogMap.put("status", "404");
      catalogMap.put("code", e); // put in just in case need more information
    }
    return catalogMap;
  }

  /**
  * Override this class
  * Generate the data to be transmitted to the Rancher API stack endpoint call
  * @param take in a map that contains user's information
  */
  public GenericConvertMap<String, Object> generateStackPostData(GenericConvertMap<String, Object> userInfo){
    // returns null unless overwritten
    return null;
  }

  /**
  * Create a new stack for the user specified
  * @param take in the stack configuration in form of Map Object
  */
  public GenericConvertMap<String, Object> createStack(GenericConvertMap<String, Object> stackConfig){
    HttpURLConnection uc = (HttpURLConnection) initializeConnection(baseURL+"/v" + beta_version + "-beta/projects/"+projectID+"/stack");
    StringBuffer response = new StringBuffer();
    Map<String, Object> result = new HashMap<>();
    try{
      // Setting POST request
      uc.setRequestMethod("POST");
      uc.setDoOutput(true);
      uc.setDoInput(true);
      uc.setRequestProperty("Authorization", basicAuth);
      uc.setRequestProperty("Content-Type", "application/json");
      uc.setRequestProperty("Accept", "application/json");
      // Writing data to the URL
      byte[] dataBytes = stackConfig.toString().getBytes();
      OutputStream os = uc.getOutputStream();
      os.write(dataBytes);
      os.close();
      uc.connect();
      // Getting response back
      int responseCode = uc.getResponseCode();
      InputStream is = null;
      // If stack is created
      if( responseCode == 201 ){
        is = uc.getInputStream();
      } else {
        // Get the error
        is = uc.getErrorStream();
      }
      // Outputing the response
      BufferedReader in = new BufferedReader(new InputStreamReader(is));
  		String inputLine;
  		while ((inputLine = in.readLine()) != null) {
  			response.append(inputLine);
  		}
  		in.close();
      // Convert response into Map
      result = ConvertJSON.toMap(response.toString());
    } catch(Exception e) {
      System.out.println("Connection error while passing data to rancher.");
      // Additional errors captured and placed to result
      result.put("status", "422");
      result.put("code", "Connection error while passing data to rancher. Review RancherObject.");
    }
    // return as a GenericConvertMap
    return GenericConvert.toGenericConvertStringMap(result);
  }
}
