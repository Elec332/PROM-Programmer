package elec332.promprogrammer.impl;

import elec332.promprogrammer.api.IPROMData;

/**
 * Created by Elec332 on 22-5-2018
 */
public abstract class AbstractPROM implements IPROMData {

    protected AbstractPROM(String name, int pins){
        this.name = name;
        this.pins = pins;
    }

    private final String name;
    private final int pins;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPins() {
        return pins;
    }

}
