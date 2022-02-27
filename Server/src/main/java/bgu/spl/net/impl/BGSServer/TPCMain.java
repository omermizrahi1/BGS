package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.srv.*;
import java.io.IOException;
import java.util.List;

public class TPCMain {
    public static void main(String[] args) throws IOException {
        ConnectionsImpl connections = ConnectionsImpl.getInstance();
        try (BaseServer<List<Object>> server = BaseServerImp.threadPerClient(
                Integer.parseInt(args[0]), //port
                () -> new BGSProtocol(connections),
                () -> new BidiMessageEncoderDecoder())) {
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
