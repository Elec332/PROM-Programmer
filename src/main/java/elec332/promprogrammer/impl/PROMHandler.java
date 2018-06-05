package elec332.promprogrammer.impl;

import elec332.promprogrammer.api.IPROMData;
import elec332.promprogrammer.api.IPROMHandler;
import elec332.promprogrammer.api.IPROMLink;
import elec332.promprogrammer.db.CAT28C16A;
import elec332.promprogrammer.impl.util.SerialHelper;
import gnu.io.SerialPort;

/**
 * Created by Elec332 on 22-5-2018
 */
public enum PROMHandler implements IPROMHandler {

    INSTANCE;

    PROMHandler(){
        try {
            this.port = SerialHelper.connect("??", 9600);
            link = null;
        } catch (Exception e){
            throw new ExceptionInInitializerError("Failed to initialize PROM-Manager");
        }
    }

    private final SerialPort port;
    private PROMLink link;

    @Override
    public void registerPROMType(IPROMData data) {

    }

    @Override
    public IPROMLink linkPROM(IPROMData data) {
        if (link != null){
            link.disconnect();
            link = null;
        }
        link = new PROMLink(data, port);
        return link;
    }

    static {
        INSTANCE.registerPROMType(new CAT28C16A());
    }

}
