

package barcode.scanner;

/**
 * a program to emulate bar code scanner api
 * @auther kamar baraka
 * @version 1.0*/

import java.awt.AWTException;
import java.awt.Robot;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TooManyListenersException;
import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;



public class ScanHandler
        implements Runnable, SerialPortEventListener {

    private static CommPortIdentifier myCommPortIdentifier;
    private static Enumeration portList;
    private static String TimeStamp, driverClass, connectionString, comPort;
    private Connection myConnection;
    private InputStream myInputStream;
    private Robot myRobot;
    private SerialPort mySerialPort;
    private Thread myThread;
    public ScanHandler() {

        // open serial port
        try {
            TimeStamp = new java.util.Date().toString();
            mySerialPort = (SerialPort) myCommPortIdentifier.open("ComControl", 2000);
            //System.out.println(TimeStamp + ": " + myCommPortIdentifier.getName() + " opened for scanner input");
        } catch (PortInUseException e) {
            e.printStackTrace();
        }

        // get serial input stream
        try {
            myInputStream = mySerialPort.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // add an event listener on the port
        try {
            mySerialPort.addEventListener(this);
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
        mySerialPort.notifyOnDataAvailable(true);

        // set up the serial port properties
        try {
            mySerialPort.setSerialPortParams(9600,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
            mySerialPort.setDTR(false);
            mySerialPort.setRTS(false);

        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
        }

        // make a robot to pass keyboard data
        try {
            myRobot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        // create the thread
        myThread = new Thread(this);
        myThread.start();
    }

    public void run() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignore) {}
    }

    // on scan
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {

            StringBuilder myStringBuilder = new StringBuilder();
            int c;
            try {

                // append the scanned data onto a string builder
                while ((c = myInputStream.read()) != 10){
                   if (c != 13)  myStringBuilder.append((char) c);
                }

                // send to keyboard buffer if the barcode doesn't start with '5'
                if (myStringBuilder.charAt(0) != '5') {

                    for (int i = 0; i < myStringBuilder.length(); i++) {
                        myRobot.keyPress((int) myStringBuilder.charAt(i));
                        myRobot.keyRelease((int) myStringBuilder.charAt(i));
                    }

                // here's the scanned barcode as a variable!
                } else {
                    TimeStamp = new java.util.Date().toString();
                    System.out.println(TimeStamp + ": scanned input received:" + myStringBuilder.toString());
                }

                // close the input stream
                myInputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        // read ScanHandler properties
        Properties myProperties = new Properties();
        try {
            myProperties.load(new FileInputStream("config.properties"));
            comPort             = myProperties.getProperty("ScanHandler.comPort");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            // get our pre-defined COM port
            myCommPortIdentifier = CommPortIdentifier.getPortIdentifier(comPort);
            ScanHandler reader = new ScanHandler();

        } catch (Exception e) {
            TimeStamp = new java.util.Date().toString();
            System.out.println(TimeStamp + ": " + comPort + " " + myCommPortIdentifier);
            System.out.println(TimeStamp + ": msg1 - " + e);
        }
    }
}