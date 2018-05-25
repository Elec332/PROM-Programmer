package elec332.promprogrammer.db;

import elec332.promprogrammer.impl.AbstractPROM;

/**
 * Created by Elec332 on 22-5-2018
 */
public class CAT28C16A extends AbstractPROM {

    public CAT28C16A() {
        super("CAT28C16A", 24);
    }

    @Override
    public int getAddressBits() {
        return 11;
    }

    @Override
    public int getIOBits() {
        return 8;
    }

}
