package picoded.webTemplateEngines.FormGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// Isolated FormWrapperTemplates, focused entirely on table wrapper implmentation
///
/// This is mainly cause the table implmentation alone is complicated sadly =(
/// so the class files are seperated for easier maintenance
///
public class TableWrapperTemplates {
	
	///
	/// FunctionalInterface for inserting of table data, inside the shared table builder
	///
	/// This is used both in injecting header data, and for row data. Note that the index count
	/// for both are counted differently.
	///
	/// Additionally the function should return a NULL for terminating the end of an iteration
	/// (end of row, or collumn, etc). As the table builder relies on this to "finish"
	///
	@FunctionalInterface
	public interface TableDataInjector {
		public abstract StringBuilder apply(int rowPos, int colPos);
	}
	
	/// If your printing more then a 100 K rows, you seriously have problems
	/// This upper limit to iteration is used to prevent infinite loops
	public static final int tableBuilderSanityLimit = 100000;
	
	/// TableBuilder out of bound standard error
	public static final String tableBuilderLimitError = "WT# - tableBuilder Injector out of bound for: ";
	
	///
	/// Utility function used to build the HTML table structure. With the respective data injectors
	///
	public static StringBuilder tableBuilder_ViaInjectors( //
		FormNode node, //
		TableDataInjector topHeaderInjector, TableDataInjector leftHeaderInjector, TableDataInjector dataInjector //
	) { //
		 //
		 // Reuse vars
		 //
		StringBuilder ret = new StringBuilder(); //return StringBuilder
		try {
			
			int row = 0; //Row iterator
			int col = 0; //Collumn iterator
			StringBuilder injectorData = null;
			
			//
			// Child wrapper: <table class="pfc_table"> ... </table> tags
			//
			StringBuilder[] tableTag = node.defaultHtmlChildWrapper(HtmlTag.TABLE, node.prefix_childWrapper() + "table",
				null);
			ret.append(tableTag[0]);
			
			//
			// Top header handling: <thead> ... </thead>
			//
			ret.append("<thead>");
			if (topHeaderInjector != null) {
				// Loop top header rows
				row = 0;
				while (row >= 0) {
					injectorData = topHeaderInjector.apply(row, 0);
					// Terminates the row if it has no 0 index items
					if (injectorData == null) {
						break;
					}
					// Creates the row
					ret.append("<tr class='" + node.prefix_childWrapper() + "row_" + row + "'>");
					
					// Insert first 0 index item for the row
					ret.append("<th class='" + node.prefix_childWrapper() + "col_0'>");
					ret.append(injectorData);
					ret.append("</th>");
					
					// Next collumn onwards
					col = 1;
					while (col >= 1) {
						// 1 index onwards
						injectorData = topHeaderInjector.apply(row, col);
						// Moves on to next row when no more data
						if (injectorData == null) {
							break;
						}
						
						// Insert first 1 index item onwards for the row
						ret.append("<th class='" + node.prefix_childWrapper() + "col_" + col + "'>");
						ret.append(injectorData);
						ret.append("</th>");
						
						// Increment to next column within sanity limits
						if (col++ > tableBuilderSanityLimit) {
							throw new RuntimeException(tableBuilderLimitError + "Top Header Columns");
						}
					}
					// Terminates the row
					ret.append("</tr>");
					
					// Increment to next row within sanity limits
					if (row++ > tableBuilderSanityLimit) {
						throw new RuntimeException(tableBuilderLimitError + "Top Header Rows");
					}
				}
			}
			// End top header rows loop
			ret.append("</thead>");
			
			//
			// Left header, and actual content handling: <thead> ... </thead>
			//
			ret.append("<tbody>");
			// Loop body rows
			row = 0;
			while (row >= 0) {
				StringBuilder firstRowHeader = (leftHeaderInjector != null) ? leftHeaderInjector.apply(row, 0) : null;
				StringBuilder firstRowData = (dataInjector != null) ? dataInjector.apply(row, 0) : null;
				
				// Terminates the row if it has no 0 index items, for both headers and data
				if (firstRowHeader == null && firstRowData == null) {
					break;
				}
				
				ret.append("<tr class='" + node.prefix_childWrapper() + "row_" + row + "'>");
				//
				// Row Header handling
				//
				if (firstRowHeader != null) {
					// Insert first 0 index header item for the row
					ret.append("<th class='" + node.prefix_childWrapper() + "col_0'>");
					
					ret.append(firstRowHeader);
					ret.append("</th>");
				}
				
				// Next collumn onwards
				col = 1;
				while (col >= 1) {
					// 1 index onwards
					
					injectorData = (leftHeaderInjector != null) ? leftHeaderInjector.apply(row, col) : null;
					// Moves on to next row when no more data
					if (injectorData == null) {
						break;
					}
					
					// Insert first 1 index item onwards for the row
					ret.append("<th class='" + node.prefix_childWrapper() + "col_" + col + "'>");
					ret.append(injectorData);
					ret.append("</th>");
					
					// Increment to next column within sanity limits
					if (col++ > tableBuilderSanityLimit) {
						throw new RuntimeException(tableBuilderLimitError + "Left Header Columns");
					}
				}
				
				//
				// Row Data handling
				//
				if (firstRowData != null) {
					// Insert first 0 index header item for the row
					ret.append("<td class='" + node.prefix_childWrapper() + "col_0'>");
					ret.append(firstRowData);
					ret.append("</td>");
				}
				
				// Next collumn onwards
				col = 1;
				while (col >= 1) {
					// 1 index onwards
					
					injectorData = (dataInjector != null) ? dataInjector.apply(row, col) : null;
					// Moves on to next row when no more data
					if (injectorData == null) {
						break;
					}
					// Insert first 1 index item onwards for the row
					ret.append("<td class='" + node.prefix_childWrapper() + "col_" + col + "'>");
					ret.append(injectorData);
					ret.append("</td>");
					// Increment to next column within sanity limits
					if (col++ > tableBuilderSanityLimit) {
						throw new RuntimeException(tableBuilderLimitError + "Left Header Columns");
					}
				}
				
				ret.append("</tr>");
				
				// Increment to next row within sanity limits
				if (row++ > tableBuilderSanityLimit) {
					throw new RuntimeException(tableBuilderLimitError + "Body Rows");
				}
			}
			// End body rows loop
			ret.append("</tbody>");
			
			// Closes table tag
			ret.append(tableTag[1]);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return ret;
	}
	
	///
	/// Utility function used to build the HTML table structure. With the respective data injectors. And a standard wrapper
	///
	public static StringBuilder tableBuilderWithWrapper_ViaInjectors( //
		FormNode node, String wrapperClass, //
		TableDataInjector topHeaderInjector, TableDataInjector leftHeaderInjector, TableDataInjector dataInjector //
	) { //
		 //return
		StringBuilder ret = new StringBuilder();
		
		//wrapper
		StringBuilder[] wrapperArr = node.defaultHtmlWrapper(HtmlTag.DIV, wrapperClass, null);
		ret.append(wrapperArr[0]);
		
		//label
		String label = node.label();
		if (label != null && label.length() > 0) {
			StringBuilder[] labelArr = node.defaultHtmlLabel(HtmlTag.DIV, node.prefix_standard() + "label", null);
			
			ret.append(labelArr[0]);
			ret.append(label);
			ret.append(labelArr[1]);
		}
		
		//table
		ret.append(tableBuilder_ViaInjectors(node, topHeaderInjector, leftHeaderInjector, dataInjector));
		
		//closing up
		ret.append(wrapperArr[1]);
		return ret;
	}
	
	///
	/// Utility function used to build the header injector, based on its definition
	///
	@SuppressWarnings("unchecked")
	public static TableDataInjector topHeaderDefineToInjector(FormNode node, Object topHeaderDefine,
		boolean isDisplayMode) {
		if (topHeaderDefine != null) {
			if (topHeaderDefine instanceof List || topHeaderDefine instanceof Map) {
				//
				// Setup the header list
				//
				List<Object> mainHeaderList = new ArrayList<Object>();
				if (topHeaderDefine instanceof List) {
					mainHeaderList.addAll((List<Object>) topHeaderDefine);
				} else { //use map as a single row
					mainHeaderList.add(topHeaderDefine);
				}
				//
				// Empty headers list, will not created the lamda function
				//
				if (mainHeaderList.size() > 0) {
					if (mainHeaderList.get(0) instanceof List) {
						//
						// Header is 2 tiers !!!
						//
						return (row, col) -> {
							//
							// Terminate row iteration, once beyond the list scope
							//
							if (row >= mainHeaderList.size()) {
								return null;
							}
							//
							// Gets the sub list
							//
							Object subTierObj = mainHeaderList.get(row);
							if (subTierObj instanceof List) {
								List<Object> subTierList = (List<Object>) subTierObj;
								//
								// Terminate column iteration, once beyond the column limit
								//
								if (col >= subTierList.size()) {
									return null; //terminate row
								}
								//
								// Gets the tier2 return object
								//
								Object ret = subTierList.get(col);
								
								//
								// Form node in headers D=
								//
								if (ret instanceof Map) {
									return (new FormNode(node._formGenerator, (Map<String, Object>) ret, node.getValueMap()))
										.fullHtml(isDisplayMode);
								}
								
								return new StringBuilder((ret != null) ? ret.toString() : "");
							} else {
								//
								// Fallsback to single collumn if no sublist
								//
								if (col == 0) {
									return new StringBuilder((subTierObj != null) ? subTierObj.toString() : "");
								}
							}
							// Terminate row / col (all else failed)
							return null;
						};
					} else {
						//
						// Assume is 1 tier, single row only
						//
						return (row, col) -> {
							// Terminate row iteration, after first row
							if (row > 0) {
								return null;
							}
							// Terminate column iteration, once beyond the column limit
							if (col >= mainHeaderList.size()) {
								return null;
							}
							Object ret = mainHeaderList.get(col);
							
							//
							// Form node in headers D=
							//
							if (ret instanceof Map) {
								return (new FormNode(node._formGenerator, (Map<String, Object>) ret, node.getValueMap()))
									.fullHtml(isDisplayMode);
							}
							
							return new StringBuilder((ret != null) ? ret.toString() : "");
						};
					}
				}
			} else {
				//
				// Fallback: output object string ONCE
				//
				return (row, col) -> {
					if (row == 0 && col == 0) {
						return new StringBuilder(topHeaderDefine.toString());
					}
					return null;
				};
			}
		}
		return null;
	}
	
	///
	/// Varient of [tableBuilderWithWrapper_ViaInjectors], which does the processing of objects to header injectors
	///
	/// This handles the conversion of table header formats to the lamda functions
	///
	public static StringBuilder tableBuilderWithWrapper_ViaDefinesAndInjector(FormNode node, String wrapperClass,
		boolean isDisplayMode, //
		Object topHeaderDefine, Object leftHeaderDefine, TableDataInjector dataInjector //
	) { //
	
		//
		// Top header handling, if object definition given
		//
		TableDataInjector topHeaderInjector = topHeaderDefineToInjector(node, topHeaderDefine, isDisplayMode);
		
		//
		// Left header handling, if object definition given
		//
		TableDataInjector leftHeaderInjector = null;
		TableDataInjector leftHeaderInjector_unflipped = topHeaderDefineToInjector(node, leftHeaderDefine, isDisplayMode);
		if (leftHeaderInjector_unflipped != null) {
			leftHeaderInjector = (row, col) -> {
				return leftHeaderInjector_unflipped.apply(col, row);
			};
		}
		
		// Run with all the applicable injectors
		return tableBuilderWithWrapper_ViaInjectors(node, wrapperClass, topHeaderInjector, leftHeaderInjector,
			dataInjector);
	}
	
	///
	/// Utility function used to build the data injector, based on its field
	///
	@SuppressWarnings("unchecked")
	public static TableDataInjector horizontalDataInjectorFromNode(FormNode node, boolean isDisplayMode) {
		
		Object fieldValue = node.getRawFieldValue();
		List<Map<String, Object>> childrenDefinition = node.childrenDefinition();
		List<Object> valueRows = (fieldValue instanceof List) ? (List<Object>) fieldValue : null;
		
		//
		// Minimum rows iteration check
		//
		int minIteration = node.getInt("min-iteration", 0);
		int maxIteration = node.getInt("max-iteration", 32767); //short int max
		
		// Fixed iteration overwrite, min/max
		int fixedIteration = node.getInt("iteration", -1);
		if (fixedIteration >= 0) {
			minIteration = fixedIteration;
			maxIteration = fixedIteration;
		}
		
		//
		// Named iteration mode ?
		//
		String[] namedIteration = node.getStringArray("name-iteration", null);
		if (namedIteration != null) {
			minIteration = namedIteration.length;
			maxIteration = namedIteration.length;
		}
		
		//
		// Table iteration row values setup
		//
		if (minIteration > 0 || maxIteration > 0) {
			List<Object> tmpRows = new ArrayList<Object>();
			if (valueRows != null) {
				tmpRows.addAll(valueRows);
			}
			while (tmpRows.size() < minIteration) {
				tmpRows.add(new HashMap<String, Object>());
			}
			
			//
			// Maximum iteration check
			//
			while (tmpRows.size() > maxIteration) {
				tmpRows.remove(tmpRows.size() - 1);
			}
			
			valueRows = tmpRows;
		}
		
		//
		// Return the value rows lamda if not null
		//
		if (valueRows != null) {
			final List<Object> dataRows = valueRows;
			final List<String> namedIteration_final = (namedIteration == null) ? null : Arrays.asList(namedIteration);
			
			return (row, col) -> {
				//
				// Terminate the rows, when last row is done
				//
				if (row >= dataRows.size()) {
					return null;
				}
				//
				// Terminate the cols, when last col is done
				//
				if (col >= childrenDefinition.size()) {
					return null;
				}
				//
				// Prepare
				//
				Object rowObj = dataRows.get(row);
				Map<String, Object> rowMap = (rowObj instanceof Map) ? (Map<String, Object>) rowObj : null;
				Map<String, Object> child = childrenDefinition.get(col);
				
				//sam single tier "fix"
				int tierNumber = row;
				
				FormNode childNode = new FormNode(node._formGenerator, child, rowMap);
				String currentNodeName = node.getFieldName();
				
				if (currentNodeName != null && !currentNodeName.isEmpty()) {
					if (namedIteration_final != null) {
						childNode.namePrefix = currentNodeName + "." + namedIteration_final.get(tierNumber) + ".";
					} else {
						childNode.namePrefix = currentNodeName + "[" + tierNumber + "].";
					}
				}
				//and sam "fix"
				
				//
				// Build if possible
				//
				StringBuilder ret = null;
				if (child != null && rowMap != null) {
					ret = (childNode).fullHtml(isDisplayMode);
				}
				return (ret != null) ? ret : new StringBuilder(""); //blank fallback
			};
		}
		
		return null;
	}
	
	///
	/// Horizontal table wrapper implmentation
	///
	/// Children field values are added as rows
	///
	public static StringBuilder tableWrapper_horizontal(FormNode node, boolean isDisplayMode) {
		//exclude from display;
		if (isDisplayMode && node.getBoolean("excludeFromDisplay", false)) {
			return new StringBuilder("");
		}
		
		return tableBuilderWithWrapper_ViaDefinesAndInjector(
		// Base stuff
			node, node.prefix_wrapper() + "table " + node.prefix_wrapper() + "horizontalTable", isDisplayMode, //
			// Top header injector (base header is default also)
			(node.get("topHeaders") != null) ? node.get("topHeaders") : node.get("headers"), //
			// Left header define
			node.get("leftHeaders"), //
			// Data injector
			horizontalDataInjectorFromNode(node, isDisplayMode) //
		);
	}
	
	///
	/// Vertical table wrapper implmentation
	///
	/// Children field values are added as cols
	///
	public static StringBuilder tableWrapper_vertical(FormNode node, boolean isDisplayMode) {
		//exclude from display;
		if (isDisplayMode && node.getBoolean("excludeFromDisplay", false)) {
			return new StringBuilder("");
		}
		
		TableDataInjector unflippedDataInjector = horizontalDataInjectorFromNode(node, isDisplayMode);
		TableDataInjector dataInjector = null;
		// The same function as horizontal is used, with axis flipped
		if (unflippedDataInjector != null) {
			dataInjector = (row, col) -> {
				return unflippedDataInjector.apply(col, row);
			};
		}
		// Build it
		return tableBuilderWithWrapper_ViaDefinesAndInjector(
		// Base stuff
			node, node.prefix_wrapper() + "table " + node.prefix_wrapper() + "verticalTable", isDisplayMode, //
			// Top header injector
			node.get("topHeaders"), //
			// Left header define (base header is default also)
			(node.get("leftHeaders") != null) ? node.get("leftHeaders") : node.get("headers"), //
			// Data injector
			dataInjector//
		);
	}
	
}
