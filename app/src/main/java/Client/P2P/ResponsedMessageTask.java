package Client.P2P;

/**
 * Created by ouygu on 5/13/2017.
 */


import Client.Util.Config;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * Created by od on 27.04.2017.
 */
public class ResponsedMessageTask implements Callable<String> {

    private Peer p;
    private String msg;
    private Logger log = Client.log;

    public ResponsedMessageTask(Peer p, String msg) {
        this.p = p;
        this.msg = msg;
    }

    @Override
    public String call() throws Exception {

        int trials = 0;
        while(trials < Config.MESSAGE_MAX_TRIALS) {
            try {
                log.trace(p.getPeerServerPort());
                log.trace(p.getAddress());
                Socket messagedClient = new Socket(p.getAddress(), p.getPeerServerPort());
                messagedClient.setSoTimeout(Config.MESSAGE_TIMEOUT);
                ObjectOutputStream out = new ObjectOutputStream(messagedClient.getOutputStream());
                out.writeInt(Config.MESSAGE_OUTGOING_RESPONSE);
                out.writeUTF(msg);
                out.flush();

                ObjectInputStream in = new ObjectInputStream(new DataInputStream(messagedClient.getInputStream()));
                int ack = in.readInt();

                if (ack == Config.MESSAGE_ACK) {
                    String response = in.readUTF();
                    return response;
                } else {
                    log.trace("Non flag read");
                }
                messagedClient.close();

            } catch (IOException e) {
                log.warn("EXCEPTIOON\n\n\n");
                log.warn(e);
                trials++;
                continue;
            }
            trials++;
        }
        log.warn("Message cannot be sent after " + Config.MESSAGE_MAX_TRIALS + " trials");
        log.warn(msg);
        return null;
    }
}

