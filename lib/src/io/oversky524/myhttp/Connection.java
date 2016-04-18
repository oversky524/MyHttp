package io.oversky524.myhttp;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by gaochao on 2016/4/11.
 */
public class Connection {
    private static final boolean DEBUG = true;
    private Map<Request, Response> requests = new LinkedHashMap<>();
    private Socket mSocket;
    private long mIdleAt = Long.MAX_VALUE;

    public long idleAt(){ return mIdleAt; }

    public void setIdleAt(long idleAt){ mIdleAt = idleAt; }

    Connection(String host, int port){
        try {
            mSocket = new Socket(host, port);
            mSocket.setSoTimeout(5 * 1000);
            if(DEBUG){
                System.out.println(mSocket + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean shouldClose(long aliveDuration){
        if(mIdleAt == Long.MAX_VALUE) return false;
        if(mIdleAt + aliveDuration <= System.nanoTime() || readTest()) return true;
        return false;
    }

    public Response put(Request request){
        Response response = new Response();
        requests.put(request, response);
        mIdleAt = Long.MAX_VALUE;
        return response;
    }

    public void release(Request request){
        requests.remove(request);
        if(requests.isEmpty()) mIdleAt = System.nanoTime();
    }

    public Socket getSocket(){ return mSocket; }

    public boolean isIdle(){
        boolean empty = requests.isEmpty();
        /*if(empty){
            mIdleAt =
        }*/
        return empty;
    }

    public void closeSafely(){
        try {
            if(mSocket != null) mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(DEBUG) System.out.println(mSocket + " is closed safely");
    }

    private boolean readTest(){
        boolean result = false;
        try {
            int readTimeout = mSocket.getSoTimeout();
            try {
                mSocket.setSoTimeout(1);
                if(mSocket.getInputStream().read() == -1) result = true;
            } catch (IOException e) {
                e.printStackTrace();
                result = !(e instanceof SocketTimeoutException);
            } finally {
                mSocket.setSoTimeout(readTimeout);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return result;
    }
}
