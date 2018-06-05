package elec332.promprogrammer.impl;

import elec332.promprogrammer.api.IPROMData;
import elec332.promprogrammer.api.IPROMLink;
import gnu.io.SerialPort;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by Elec332 on 30-5-2018.
 */
class PROMLink implements IPROMLink {

    PROMLink(IPROMData data, SerialPort port){
        this.data = data;
        this.port = port;
        ioBits = data.getIOBits().length;
        addressBits = data.getAddressBits().length;
        if (addressBits != 8){
            throw new RuntimeException("Dunno what to do with this...");
        }
        init();
    }

    private final IPROMData data;
    private final int ioBits, addressBits;
    private SerialPort port;
    private BufferedReader reader;
    private BufferedWriter writer;

    private void init(){
        data.getIOBits();
        data.getAddressBits();
        try {
            reader = new BufferedReader(new InputStreamReader(port.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(port.getOutputStream()));
        } catch (Exception e){
            throw new RuntimeException("Failed to initialize PROMLink @ "+port.getName());
        }
    }

    void disconnect(){
        port = null;
    }

    @Override
    public boolean isConnected() {
        return port != null;
    }

    @Override
    public void writeByte(int address, byte data) {
        writer.write("wb|"+address+","+Byte.t);
    }

    @Override
    public int readByte(int address) {
        return 0;
    }

    private void checkAddress(int address){
        if(address < 0 || Integer.toUnsignedLong(address) >= Math.pow(2, addressBits)) {
            throw new IllegalArgumentException("Address outside range");
        }
    }

}
