package hackathon.geoloc;

import gnu.io.*;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;

public class App {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		TwoWaySerialComm o = new TwoWaySerialComm();
		try {
			listPorts(); // trouver le nom des devices connectées 
			o.connect("COM3"); // se connecter au device 
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	 static void listPorts()
	    {
	        java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
	        while ( portEnum.hasMoreElements() ) 
	        {
	            CommPortIdentifier portIdentifier = portEnum.nextElement();
	            System.out.println(portIdentifier.getName()  +  " - " +  getPortTypeName(portIdentifier.getPortType()) );
	        }        
	    }
	    
	    static String getPortTypeName ( int portType )
	    {
	        switch ( portType )
	        {
	            case CommPortIdentifier.PORT_I2C:
	                return "I2C";
	            case CommPortIdentifier.PORT_PARALLEL:
	                return "Parallel";
	            case CommPortIdentifier.PORT_RAW:
	                return "Raw";
	            case CommPortIdentifier.PORT_RS485:
	                return "RS485";
	            case CommPortIdentifier.PORT_SERIAL:
	                return "Serial";
	            default:
	                return "unknown type";
	        }
	    }
}


	


