package elec332.promprogrammer.impl;

import elec332.promprogrammer.api.*;
import gnu.io.SerialPort;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by Elec332 on 22-5-2018
 */
enum PROMHandler implements IPROMHandler {

    INSTANCE;

    PROMHandler(){
        try {
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                disconnectLink();
                System.err.print("Exception in thread \"" + t.getName() + "\" ");
                e.printStackTrace(System.err);
                System.exit(1);
            });
            Runtime.getRuntime().addShutdownHook(new Thread(this::disconnectLink));
            this.link = null;
            this.registry = new HashMap<>();
            this.names = Collections.unmodifiableSet(this.registry.keySet());
            this.cRegistry = new HashMap<>();
        } catch (Exception e){
            throw new ExceptionInInitializerError(new RuntimeException("Failed to initialize PROM-Manager", e));
        }
    }

    private final Map<String, IPROMData> registry;
    private final Map<Class<? extends IPROMData>, IPROMData> cRegistry;
    private final Set<String> names;
    private SerialPort port;
    private PROMLink link;

    @Override
    public void registerPROMType(IPROMData data) {
        String name = Objects.requireNonNull(data).getName();
        if (!name.equals(name.toUpperCase())){
            throw new IllegalArgumentException("Letters in PROM name must be uppercase");
        }
        if (this.registry.containsKey(name)){
            throw new IllegalArgumentException("PROM type with name " + name + " already exists!");
        }
        this.registry.put(name, data);
        this.cRegistry.put(data.getClass(), data);
    }

    @Override
    public IPROMData getPROMData(String name) {
        return this.registry.get(Objects.requireNonNull(name).toUpperCase());
    }

    @Override
    @SuppressWarnings("all")
    public <P extends IPROMData> P getPROMData(Class<P> type) {
        return (P) cRegistry.get(type);
    }

    @Override
    public Set<String> getRegisteredPROMs() {
        return this.names;
    }

    @Override
    public IPROMLink linkPROM(IPROMData data, BitOrder order) {
        disconnectLink();
        if (!this.registry.containsKey(Objects.requireNonNull(data).getName())){
            throw new IllegalArgumentException("PROM " + data.getName() + " has not been registered!");
        }
        if (getPROMData(data.getName()) != data || getPROMData(data.getClass()) != data) { //Don't try to trick the system...
            throw new IllegalArgumentException("Please use the original registered PROM data!");
        }
        if (port == null){
            throw new IllegalStateException("COM port hasn't been set!");
        }
        this.link = new PROMLink(data, port, Objects.requireNonNull(order));
        return this.link;
    }

    @Override
    public IPROMHandler setCOMPort(String port) {
        try {
            this.disconnectLink();
            this.port = SerialHelper.connect(port, 57600);
            Thread.sleep(1500);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to connect to port: " + port, e);
        }
        return this;
    }

    void disconnectLink(){
        if (this.link != null){
            this.link.disconnect();
            this.link = null;
        }
    }

    static {
        try {
            register();
            for (String s : SerialHelper.getPackageContent("elec332.promprogrammer.db")){
                Class<?> c = Class.forName(s);
                Constructor<?> ctr = c.getDeclaredConstructor();
                ctr.setAccessible(true);
                ctr.newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError("Failed to register PROMHandler");
        }
    }

    private static void register() throws Exception {
        Field f = PROMAPI.class.getDeclaredField("handler");
        f.setAccessible(true);
        int i = f.getModifiers();
        Field modifier = f.getClass().getDeclaredField("modifiers");
        i &= -17;
        modifier.setAccessible(true);
        modifier.setInt(f, i);
        f.set(null, INSTANCE);
        f.setAccessible(false);
    }

}
