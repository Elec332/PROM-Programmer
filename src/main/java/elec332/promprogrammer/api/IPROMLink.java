package elec332.promprogrammer.api;

/**
 * Created by Elec332 on 30-5-2018.
 */
public interface IPROMLink {

    public boolean isConnected();

    public void writeByte(int address, byte data);

    public int readByte(int address);

}
