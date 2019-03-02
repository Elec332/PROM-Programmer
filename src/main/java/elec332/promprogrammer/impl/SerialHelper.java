package elec332.promprogrammer.impl;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

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

    static List<String> getPackageContent(String p) throws Exception {
        final String packageName = p.replace('.', '/');
        List<String> list = new ArrayList<>();
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(packageName);
        while (urls.hasMoreElements()) {
            URI uri = urls.nextElement().toURI();
            Path ph;
            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                ph = fileSystem.getPath(packageName);
            } else {
                ph = Paths.get(uri);
            }
            Files.walk(ph, 1).forEach(path -> {
                if (!path.toString().equals(ph.toString())) {
                    list.add((packageName + "." + path.toString().substring(path.getParent().toString().length() + 1).replace(".class", "")).replace("/", "."));
                }
            });
        }
        return list;
    }

}
