package elec332.promprogrammer.api;

/**
 * Created by Elec332 on 22-5-2018
 */
public interface IPROMHandler {

    public void registerPROMType(IPROMData data);

    public IPROMLink linkPROM(IPROMData data);

}
