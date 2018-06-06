package elec332.promprogrammer.impl;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 30-5-2018.
 */
class SerialHelper {

    @SuppressWarnings("all")
    public static SerialPort connect(String portName, int speed) throws Exception {
        CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier(portName);
        if (cpi.isCurrentlyOwned()) {
            throw new RuntimeException("Comm port already owned!");
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

    static List<String> getPackageContent(String p) throws IOException {
        final String packageName = p.replace('.', '/');
        List<String> list = new ArrayList<>();
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(packageName);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            File dir = new File(url.getFile());
            String base = dir.getPath();
            list.addAll(Arrays.stream(Objects.requireNonNull(dir.listFiles())).map(file -> p.replace('/', '.') + "." + file.getPath().replace(base, "").substring(1).replace(".class", "")).collect(Collectors.toList()));
        }
        return list;
    }

}
