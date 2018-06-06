package elec332.promprogrammer.api;

import java.util.function.Consumer;
import java.util.function.IntFunction;

/**
 * Created by Elec332 on 30-5-2018.
 */
public interface IPROMLink {

    public boolean isConnected();

    default public void writeByte(int address, int data){
        writeByteWithReset(address, data);
    }

    public void writeByteMultiple(int address, int data);

    public void writeByteWithReset(int address, int data);

    default public int readByte(int address){
        return readByteWithReset(address);
    }

    public int readByteMultiple(int address);

    public int readByteWithReset(int address);

    public void printContents();

    public void printContents(int startAddress, int stopAddress, int wait);

    public void readContents(Consumer<String> handler);

    public void readContents(Consumer<String> handler, int startAddress, int stopAddress, int wait);

    public void write(int[] data, boolean checkIntegrity); //This will murder your RAM, revive it, and kill it again...

    public void write(IntFunction<Integer> dataFetcher, boolean checkIntegrity);

    public void clearEEPROM();

    public void ping();

    public int getIODelay();

}
