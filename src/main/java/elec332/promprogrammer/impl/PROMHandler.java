package elec332.promprogrammer.impl;

import elec332.promprogrammer.api.IPROMData;
import elec332.promprogrammer.api.IPROMHandler;
import elec332.promprogrammer.db.CAT28C16A;

/**
 * Created by Elec332 on 22-5-2018
 */
public enum PROMHandler implements IPROMHandler {

    INSTANCE;

    @Override
    public void registerPROMType(IPROMData data) {

    }

    static {
        INSTANCE.registerPROMType(new CAT28C16A());
    }

}
