package picoded.webTemplateEngines.FormGenerator;


public class FormInputTemplates_test {
	
	/// Prototype lenientStringLookup
	public int lenientStringLookup(String source, String lookup, int offset) {
		
		int lowestOffset = -1;
		int tmpOffset;
		
		String[] lookupArray = lookup.split(" ");
		
		for(int a=0; a<lookupArray.length; ++a) {
			tmpOffset = source.indexOf(lookupArray[a], offset);
			
			if(lowestOffset <= -1 || tmpOffset < lowestOffset) {
				lowestOffset = tmpOffset;
			}
		}
		
		return lowestOffset;
	}
	
}
