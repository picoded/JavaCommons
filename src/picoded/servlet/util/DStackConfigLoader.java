package picoded.servlet.util;

import picoded.dstack.DStack;
import picoded.dstack.jsql.JSqlStack;
import picoded.dstack.jsql.connector.JSql;
import picoded.dstack.CommonStack;

import picoded.struct.GenericConvertMap;
import picoded.conv.ConvertJSON;

public class DStackConfigLoader {
  public DStackConfigLoader(){

  }

  public static CommonStack configStringToCommonStack ( String config ) {
    GenericConvertMap<String, Object> configMap = ConvertJSON.toCustomClass(config, GenericConvertMap.class);
    if ( configMap == null )
      return null;
    String type = configMap.getString("type");

    if ( type.equalsIgnoreCase("Jsql") ) {
			String engine = configMap.getString("engine", "");
			String path = configMap.getString("path", "");
			String username = configMap.getString("username", "");
			String password = configMap.getString("password", "");
			String database = configMap.getString("database", "");
      if ( path.isEmpty() )
        throw new RuntimeException("DStack path is not set.");
      if ( engine.equalsIgnoreCase("sqlite") ) {
        return  new JSqlStack(JSql.sqlite(path));
      } else if ( egine.equalsIgnoreCase("mysql") ) {
        return new JSqlStack(Jsql.mysql(path, database, username, password));
      }
      return null;
    }
  }

  public static DStack generateDStack ( List<Object> stackOptions ) {
    DStack dstack = new DStack();
    for ( Object stackOption : stackOptions ) {
      CommonStack stack = configStringToCommonStack(ConvertJSON.fromObject(stackOption));

    }
  }
}
