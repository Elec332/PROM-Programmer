package elec332.promprogrammer.api;

import java.util.List;

/**
 * Created by Elec332 on 22-5-2018
 */
public interface IPROMData {

    public String getName();

    public int getPins();

    public int[] getAddressBits();

    public int[] getIOBits();

    public int getGNDPin();

    public int getVCCPin();

    public void writeByte(int address, byte data, Loaded link);

    public int readByte(int address, Loaded link);

    public interface Loaded {

        public IPROMData getLinkedType();

        public void setAddress(int address);

        public void setData(byte data);

        public byte readByte(int address);

        public void setPin(int pin, boolean high);

    }

}
