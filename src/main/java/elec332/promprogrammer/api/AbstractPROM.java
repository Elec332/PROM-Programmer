package elec332.promprogrammer.api;

/**
 * Created by Elec332 on 22-5-2018
 */
public abstract class AbstractPROM implements IPROMData {

    protected AbstractPROM(String name, int pins){
        this.name = name;
        this.pins = pins;
        PROMAPI.getPROMHandler().registerPROMType(this);
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
