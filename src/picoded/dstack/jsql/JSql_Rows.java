package picoded.dstack.jsql;

import java.util.List;
import java.util.ArrayList;

public class JSql_Rows{
	public String[] uniqueColumns = null;
	public String[] insertColumns = null;
	public String[] defaultColumns = null;
	public String[] miscColumns = null;
	
	public List<Object[]> uniqueValuesList = null;
	public List<Object[]> insertValuesList = null;
	public List<Object[]> defaultValuesList = null;
	
	public JSql_Rows(){
		uniqueColumns = new String[]{};
		insertColumns = new String[]{};
		defaultColumns = new String[]{};
		miscColumns = new String[]{};
		
		uniqueValuesList = new ArrayList<Object[]>();
		insertValuesList = new ArrayList<Object[]>();
		defaultValuesList = new ArrayList<Object[]>();
	}
	
	public JSql_Rows(
		String[] inUniqueColumns, 
		List<Object[]> inUniqueValuesList,
		
		String[] inInsertColumns,
		List<Object[]> inInsertValuesList,
		
		String[] inDefaultColumns,			
		List<Object[]> inDefaultValuesList,
		
		String[] inMiscColumns
	){
		uniqueColumns = inUniqueColumns;
		insertColumns = inInsertColumns;
		defaultColumns = inDefaultColumns;
		miscColumns = inMiscColumns;
		
		uniqueValuesList = inUniqueValuesList;
		insertValuesList = inInsertValuesList;
		defaultValuesList = inDefaultValuesList;
	}
}