package elec332.promprogrammer.impl.util;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

/**
 * Created by Elec332 on 30-5-2018.
 */
public class SerialHelper {

    public static SerialPort connect(String portName, int speed) throws Exception {
        CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier(portName);
        if (cpi.isCurrentlyOwned()) {
            throw new RuntimeException();
        } else {
            CommPort p = cpi.open("PROM-Programmer", 300);
            if (p instanceof SerialPort) {
                SerialPort sp = (SerialPort) p;
                sp.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                return sp;
            } else {
                throw new RuntimeException(p + " isn't instanceof SerialPort!");
            }
        }
    }

}
