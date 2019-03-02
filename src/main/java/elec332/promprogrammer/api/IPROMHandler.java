package elec332.promprogrammer.api;

import java.util.Set;

/**
 * Created by Elec332 on 22-5-2018
 */
public interface IPROMHandler {

    public void registerPROMType(IPROMData data);

    public Set<String> getRegisteredPROMs();

    public <P extends IPROMData> P getPROMData(Class<P> type);

    public IPROMData getPROMData(String name);

    default public IPROMLink linkPROM(Class<? extends IPROMData> type, BitOrder order){
        return linkPROM(getPROMData(type), order);
    }

    default public IPROMLink linkPROM(String name, BitOrder order){
        return linkPROM(getPROMData(name), order);
    }

    public IPROMLink linkPROM(IPROMData data, BitOrder order);

    public IPROMHandler setCOMPort(String port);

    public void disconnect();

}
