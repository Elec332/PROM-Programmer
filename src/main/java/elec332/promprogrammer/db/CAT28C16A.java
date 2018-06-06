package elec332.promprogrammer.db;

import elec332.promprogrammer.api.AbstractPROM;

/**
 * Created by Elec332 on 22-5-2018
 */
public class CAT28C16A extends AbstractPROM {

    private CAT28C16A() {
        super("CAT28C16A", 24);
    }

    @Override
    public int[] getAddressBits() {
        return new int[11];
    }

    @Override
    public int[] getIOBits() {
        return new int[8];
    }

    @Override
    public int getGNDPin() {
        return 0;
    }

    @Override
    public int getVCCPin() {
        return 0;
    }

    @Override
    public void writeByte(int address, byte data, Loaded link) {

    }

    @Override
    public int readByte(int address, Loaded link) {
        return 0;
    }

    @Override
    public int getIODelay() {
        return 6;
    }

}
