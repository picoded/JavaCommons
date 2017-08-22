package picoded.servlet.api.internal;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.junit.*;

import picoded.servlet.util.EmbeddedServlet;
import picoded.TestConfig;
import picoded.core.conv.*;
import picoded.set.*;
import picoded.web.*;

public class ApiBuilderJS_test {

	//
	// Asset file read
	//
	@Test
	public void jsAssetFileRead() {
		assertNotNull(ApiBuilderJS.getJsLib());
	}
}