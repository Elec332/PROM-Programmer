package elec332.promprogrammer.api;

import java.util.Set;

/**
 * Created by Elec332 on 6-6-2018
 */
public class PROMAPI {

    public static IPROMHandler getPROMHandler(){
        return handler;
    }

    private static final IPROMHandler handler;

    static {
        handler = new IPROMHandler() {

            @Override
            public void registerPROMType(IPROMData data) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <P extends IPROMData> P getPROMData(Class<P> type) {
                throw new UnsupportedOperationException();
            }

            @Override
            public IPROMData getPROMData(String name) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<String> getRegisteredPROMs() {
                throw new UnsupportedOperationException();
            }

            @Override
            public IPROMLink linkPROM(IPROMData data, BitOrder order) {
                throw new UnsupportedOperationException();
            }

            @Override
            public IPROMHandler setCOMPort(String port) {
                throw new UnsupportedOperationException();
            }

        };

        try {
            Class.forName("elec332.promprogrammer.impl.PROMHandler");
        } catch (Exception e){
            //Ignore, something else may be handling it
        }
    }

}
