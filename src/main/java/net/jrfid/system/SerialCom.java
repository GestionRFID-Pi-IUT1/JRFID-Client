package net.jrfid.system;


import com.fazecast.jSerialComm.SerialPort;
import net.jrfid.client.JRFIDClient;

import java.util.logging.Level;

public class SerialCom {

    public SerialPort serialPort;

    public String choosenSerialPort;
    public int baudRate;

    public SerialCom(String choosenSerialPort, int baudRate){
        this.choosenSerialPort = choosenSerialPort;
        this.baudRate = baudRate;
        if(openSerialPort()){
            JRFIDClient.client.log(Level.INFO, "Connexion sur le port "+choosenSerialPort+" réussie.");
        }
    }

    public boolean openSerialPort() {
        if (this.serialPort != null && this.serialPort.isOpen())
            this.serialPort.closePort();
        this.serialPort = SerialPort.getCommPort(choosenSerialPort);

        if (this.serialPort == null) {

            JRFIDClient.client.log(Level.INFO,"Can't find selected port!");
            return false;
        }

        this.serialPort.setBaudRate(baudRate);
        this.serialPort.setFlowControl(0);
        this.serialPort.setNumStopBits(1);
        this.serialPort.setParity(0);
        if (!this.serialPort.openPort()) {

            JRFIDClient.client.log(Level.INFO, "Can't open serial port!");
            this.serialPort = null;
            return false;
        }

        /** SerialPort DataListener **/

        return true;
    }


    public void closeSerialPort() {
        this.serialPort.closePort();
        this.serialPort = null;
    }

}
