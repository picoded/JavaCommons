package picoded.page.jsml;
import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

// Sub modules useds
import picoded.conv.*;
import picoded.enums.*;
import picoded.fileUtils.*;
import picoded.JStack.*;
import picoded.JStruct.*;
import picoded.struct.*;
import picoded.servlet.*;
import picoded.servletUtils.*;
import picoded.RESTBuilder.*;
import picoded.RESTBuilder.template.core.*;
import picoded.webUtils.*;
import picoded.webTemplateEngines.FormGenerator.*;
import picoded.webTemplateEngines.JSML.*;
import picoded.page.builder.*;

///
/// FormSet class that handles JSML, integration with the MetaTable inteface
/// 
public class FormSet {
	
	//-------------------------------------------------
	//
	//  Constructor setup
	//
	//-------------------------------------------------
	
	/// The base CommonsPage to refence to
	public CommonsPage base = null;
	
	/// Automated default setup using the commons page
	public FormSet(CommonsPage inPage) {
		base = inPage;
	}
	
	//-------------------------------------------------
	//
	//  Mapping config handling
	//
	//-------------------------------------------------
	
	/// Cached mapping
	GenericConvertMap<String,Object> _mapping = null;
	
	/// The config mapping
	GenericConvertMap<String,Object> fullFormMap() {
		if( _mapping != null ) {
			return _mapping;
		}
		_mapping = base.JConfig().getGenericConvertStringMap("site.FormSet.FormMap", "{}");
		return _mapping;
	}
	
	/// The sub mapping, for a keyname, if it exists
	GenericConvertMap<String,Object> formMap(String name) {
		return fullFormMap().getGenericConvertStringMap(name, null);
	}
	
	//-------------------------------------------------
	//
	//  Servlet page run integration
	//
	//-------------------------------------------------
	
	/// Process the full servlet request handling
	public void processServletPageRequest() {
		processServletPageRequest(base);
	}
	
	/// Process the full servlet request handling
	public void processServletPageRequest(BasePage page) {
		processServletPageRequest(page, page.requestWildcardUriArray());
	}
	
	/// Process the full servlet request handling
	public void processServletPageRequest(BasePage page, String[] requestWildcardUri) {
		try {
			//
			// Safety checks
			//
			if( page == null ) {
				page = base;
			}
			if( page == null || base == null ) {
				throw new RuntimeException("Invalid FormSet object, missing both page/base parameters");
			}
			
			//
			// Wildcard string handling
			//
			if (requestWildcardUri == null) {
				requestWildcardUri = new String[] {};
			}
			
			//
			// Check for minimum parameters : 404 if not valid
			//
			if( requestWildcardUri.length < 2 ) {
				page.getHttpServletResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			
			//
			// Time to fetch it =)
			//
			String formName = requestWildcardUri[0];
			String formType = requestWildcardUri[1];
			
			//
			// Get the form config
			//
			GenericConvertMap<String,Object> formConfig = formMap(formName);
			
			// The form config object
			if(formConfig == null) {
				throw new RuntimeException("Missing formset configuration for - "+formName);
			}
			
			//
			// MetaTable integration
			//
			Map<String,Object> data = new HashMap<String,Object>();
			String metaTableName = formConfig.getString("MetaTable",null);
			
			if( metaTableName != null ) {
				// Get the _oid
				String req_oid = null;
				if( requestWildcardUri.length >= 3 ) {
					req_oid = requestWildcardUri[2];
				}
				req_oid = page.requestParameters().getString("_oid", req_oid);
				
				// And the meta object (if valid)
				MetaObject dataMObj = base.JStack().getMetaTable(metaTableName).get(req_oid);
				if( dataMObj != null ) {
					data = dataMObj;
				}
			}
			
			//
			// The form object
			//
			String jsmlName = formConfig.getString("jsml", formName);
			JSMLForm jsmlFormObj = base.JSMLFormSet().get(jsmlName);
			
			// Missing form name
			if( jsmlFormObj == null ) {
				throw new RuntimeException("Invalid JSML name provided - "+jsmlName);
			}
			
			// Form type to check
			if( formType.equalsIgnoreCase("input") ) {
				String formResult = jsmlFormObj.generateHTML(data, false).toString();
				page.getWriter().println(base.JMTE().parseTemplate(formResult, data));
				return;
			} else if( formType.equalsIgnoreCase("display") ) {
				String formResult = jsmlFormObj.generateHTML(data, true).toString();
				page.getWriter().println(base.JMTE().parseTemplate(formResult, data));
				return;
			} else if( formType.equalsIgnoreCase("pdf") ) {
				byte[] pdfData = jsmlFormObj.generatePDF(base, data);
				HttpServletResponse response = page.getHttpServletResponse();
				
				response.setContentType("application/pdf");
				response.addHeader("Content-Disposition", "filename=" + formName + ".pdf");
				response.setContentLength(pdfData.length);
				page.getOutputStream().write(pdfData);
				return;
			} else {
				throw new RuntimeException("Invalid form type provided - "+formType);
			}
			
			//
			// All failed, file not found response =(
			//
			// page.getHttpServletResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
			// return;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
