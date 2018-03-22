package hackathon.geoloc;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.json.simple.JSONObject;

public class TwoWaySerialComm
{
    public TwoWaySerialComm()
    {
        super();
    }
    
    void connect ( String portName ) throws Exception
    {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort )
            {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(115200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();
                
                String serialMessage = "\r\n\r\n";
                OutputStream outstream = serialPort.getOutputStream();
                outstream.write(serialMessage.getBytes());  // envoyer un message 
                
                System.out.println("---------------------------------------");
                
                (new Thread(new SerialReader(in))).start(); //ouverture de l'input vers le device

                //String pos;
                //Thread t = new Thread();
                //t = (new Thread (new SerialWriter(out)));
                //t.start();

                (new Thread(new SerialWriter(out))).start(); // ouverture de l'output du device
                
                
            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }     
    }
    
    /** */
    public static class SerialReader implements Runnable 
    {
        InputStream in;

        PrintWriter writer;
        
        public SerialReader ( InputStream in )
        {
            this.in = in;
        }
        
        public void run () {
            byte[] buffer = new byte[1024];
            int len = -1;
            try {
                while ( ( len = this.in.read(buffer)) > -1 && buffer.length > 13) {
                	String coord = new String(buffer,0,len);
                    System.out.print(coord);
                    try {
	                    if (coord.length() > 0 && coord.substring(0,1).equals("x")) {
		                    Writer output;
		                    output = new BufferedWriter(new FileWriter("C:/tmp/position.json"));
		                    String[] t = coord.split(" ");
	                    	output.append(t[0].split(":")[1] + " " + t[1].split(":")[1]);
	                        output.close();
	                    }
                    } catch (ArrayIndexOutOfBoundsException e) {
                    	continue;
                    }
                }
            } catch ( IOException e ) {
                e.printStackTrace();
            } 
        }
    }

    /** */
    public static class SerialWriter implements Runnable {
        OutputStream out;
        
        public SerialWriter (OutputStream out) {
            this.out = out;
        }
        
        public void run () {
            try {                
                int c = 0;
                String result = "";
                while ( ( c = System.in.read()) > -1 ) {
                	
                	//System.out.println(c);
                    this.out.write(c);

                    
                }         
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }            
        }
        
        public byte[] intToByteArray(int value) {
            return new byte[] {
                    (byte)(value >>> 24),
                    (byte)(value >>> 16),
                    (byte)(value >>> 8),
                    (byte)value};
        }
    }
    
    public static void main ( String[] args ) {
        try {
            (new TwoWaySerialComm()).connect("COM3");
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}