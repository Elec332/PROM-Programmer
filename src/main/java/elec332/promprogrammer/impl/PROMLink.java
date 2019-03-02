package elec332.promprogrammer.impl;

import elec332.promprogrammer.api.BitOrder;
import elec332.promprogrammer.api.IPROMData;
import elec332.promprogrammer.api.IPROMLink;
import gnu.io.SerialPort;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;

/**
 * Created by Elec332 on 30-5-2018.
 */
class PROMLink implements IPROMLink {

    PROMLink(IPROMData data, SerialPort port, BitOrder order){
        this.data = data;
        this.port = port;
        ioBits = data.getIOBits().length;
        addressBits = data.getAddressBits().length;
        addresses = (int) Math.pow(2, addressBits);
        if (ioBits != 8){
            throw new RuntimeException("Dunno what to do with this...");
        }
        this.order = order;
        init();
    }

    private final IPROMData data;
    private final int addresses;
    private final int ioBits, addressBits;
    private final BitOrder order;
    private SerialPort port;
    private BufferedReader reader;
    private BufferedWriter writer;
    private static final char end = '\n';
    private static final char split = ',';
    private static final Consumer<String> NULL = s -> {}, PRINT = System.out::println;
    private int delay = 0;

    private void init(){
        data.getIOBits();
        data.getAddressBits();
        try {
            reader = new BufferedReader(new InputStreamReader(port.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(port.getOutputStream()));
            sendFlushAndWait("init", 0, s -> {
                throw new RuntimeException(s);
            }, addresses, data.getIODelay(), order.ordinal());
            int total = 0;
            int poll = 25;
            for (int i = 0; i < poll; i++) {
                long l = System.currentTimeMillis();
                ping();
                total += (int) (System.currentTimeMillis() - l);
            }
            delay = data.getIODelay() - (total / (poll * 2)) + 1;
            System.out.println("Using IO delay of "+delay + "ms");
            Thread.sleep(250);
            readByte(0);
            Thread.sleep(50);
        } catch (Exception e){
            throw new RuntimeException("Failed to initialize PROMLink @ "+port.getName(), e);
        }
    }

    void disconnect(){
        checkConnection();
        try {
            writer.write("exit"+ end);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        port = null;
    }

    @Override
    public boolean isConnected() {
        return port != null;
    }

    @Override
    public IPROMData getPROMType() {
        return data;
    }

    @Override
    public BitOrder getBitOrder() {
        return order;
    }

    @Override
    public void writeByteWithReset(int address, int data) {
        if (data < 0 || data >= 256) {
            throw new IllegalArgumentException();
        }
        sendFlushAndWait("wbc", getIODelay() / 2, NULL, address, data);
    }

    @Override
    public void writeByteMultiple(int address, int data) {
        if (data < 0 || data >= 256) {
            throw new IllegalArgumentException();
        }
        sendFlushAndWait("wbm", getIODelay() / 3, NULL, address, data);
    }

    @Override
    public int readByteWithReset(int address) {
        checkConnection();
        try {
            writer.write("rbc," + address + end);
            writer.flush();
            while (true) {
                if (reader.ready()) {
                    return Integer.valueOf(reader.readLine());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int readByteMultiple(int address) {
        checkConnection();
        try {
            writer.write("rbm," + address + end);
            writer.flush();
            while (true) {
                if (reader.ready()) {
                    return Integer.valueOf(reader.readLine());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void printContents() {
        readContents(PRINT);
    }

    @Override
    public void printContents(int startAddress, int stopAddress, int wait) {
        readContents(PRINT, startAddress, stopAddress, wait);
    }

    @Override
    public void readContents(Consumer<String> handler) {
        sendFlushAndWait("ra", getIODelay(), handler);
    }

    @Override
    public void readContents(Consumer<String> handler, int startAddress, int stopAddress, int wait) {
        sendFlushAndWait("rp", getIODelay(), handler, startAddress, stopAddress, wait);
    }

    @Override
    public void write(int[] data, boolean checkIntegrity) {
        if (data.length != addresses){
            throw new IllegalArgumentException();
        }
        write(value -> data[value], checkIntegrity);
    }

    @Override
    public void write(IntUnaryOperator dataFetcher, boolean checkIntegrity) {
        List<String> check = new ArrayList<>();

        for (int i = 0; i < addresses; i += 16) {
            StringBuilder sb = new StringBuilder(String.format("%03x: ", i));
            for (int j = 0; j < 16; j++) {
                int toSend = dataFetcher.applyAsInt(i + j);
                sb.append(String.format(" %02x", toSend));
                if (j == 7) {
                    sb.append("  ");
                }
                writeByteMultiple(i + j, toSend);
                if (i + j == addresses - 1) {
                    clearIO();
                }
            }
            check.add(sb.toString());
        }
        int[] fuckingReference = {0};
        if (checkIntegrity){
            readContents(s -> {
                if (check.size() > fuckingReference[0] && !check.get(fuckingReference[0]).equals(s)){
                    System.out.println(s.substring(0, 4)+"   WEEEEEEEEEEEEEEEEEEEEEEEHHHHHHHHHH!");
                }
                fuckingReference[0] = fuckingReference[0] + 1;
            });
        }
    }

    @Override
    public void clearEEPROM() {
        sendFlushAndWait("clr", 0, PRINT);
    }

    @Override
    public void ping() {
        sendFlushAndWait("ping", 0, NULL);
    }

    @Override
    public int getIODelay() {
        return delay;
    }

    private void clearIO(){
        sendFlushAndWait("cio", getIODelay() * 2,null, NULL);
    }

    private void sendFlushAndWait(String command, int preWait, Consumer<String> receiver, Object... args) {
        checkConnection();
        try {
            Thread.sleep(preWait);
            StringBuilder commandBuilder = new StringBuilder(command);
            for (Object s : args) {
                commandBuilder.append(split).append(s.toString());
            }
            commandBuilder.append(end);
            writer.write(commandBuilder.toString());
            writer.flush();
            while (true) {
                if (reader.ready()) {
                    String s = reader.readLine();
                    if (!s.equals("end")) {
                        if (s.startsWith("thr")) {
                            throw new RuntimeException(s.split(",")[1]);
                        }
                        receiver.accept(s);
                    } else {
                        break;
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkConnection(){
        if (!isConnected()){
            throw new RuntimeException("Connection has been terminated.");
        }
    }

    private void checkAddress(int address){
        if(address < 0 || Integer.toUnsignedLong(address) >= Math.pow(2, addressBits)) {
            throw new IllegalArgumentException("Address outside range");
        }
    }

}
