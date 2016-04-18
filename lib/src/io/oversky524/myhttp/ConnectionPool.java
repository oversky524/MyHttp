package io.oversky524.myhttp;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by gaochao on 2016/4/11.
 */
public class ConnectionPool {
//    private static final long DEFAULT_ALIVE_DURATION_MS = 5 * 60 * 1000;//5 min
    private static final long DEFAULT_ALIVE_DURATION_MS = 20 * 1000;//5 min
    private static final int DEFAULT_MAX_CONNECTIONS = 5;
    private static int sMaxConnections;
    private static long sAliveDuration;
    static {
        boolean keepAlive = Boolean.valueOf(System.getProperty("http.keepAlive", "true"));
        sAliveDuration = Long.valueOf(System.getProperty("http.keepAliveDuration", String.valueOf(DEFAULT_ALIVE_DURATION_MS)));
        sMaxConnections = keepAlive ?
                Integer.valueOf(System.getProperty("http.maxConnections",String.valueOf(DEFAULT_MAX_CONNECTIONS))) :
                0;
    }

    private long mAliveDuration;//unit: ms
    private int mMaxConnections;
    private Map<String, Connection> mPool = new LinkedHashMap<>();
    private Executor mCleanupExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("MyHttp Connection Cleanup");
            thread.setDaemon(true);
            return thread;
        }
    });
    private Runnable mCleanupRunnable = new Runnable() {
        @Override
        public void run() {
            while (true){
                long leftTime = cleanupOnce(System.nanoTime());
                if(leftTime == -1) break;

                if(leftTime > 0){
                    final long unit = 1000L * 1000L;
                    long waitMills = leftTime / unit;
                    long waitNanos = leftTime - waitMills * unit;
                    synchronized (this){
                        try {
                            ConnectionPool.this.wait(waitMills, (int)waitNanos);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }
        }
    };

    /**
     * @return left time to start a new cleanup; -1 indicates that this round is over
     * */
    private long cleanupOnce(long now) {
        long maxIdleTime = Long.MIN_VALUE;
        int inUseCount = 0, idleCount = 0;
        Connection connection = null;
        String key = null;

        for(Map.Entry<String, Connection> entry : mPool.entrySet()){
            Connection c = entry.getValue();
            if(!c.isIdle()){
                ++inUseCount;
                continue;
            }

            ++idleCount;
            long idleTime = now - c.idleAt();
            if(idleTime > maxIdleTime){
                maxIdleTime = idleTime;
                connection = c;
                key = entry.getKey();
            }
        }

        if(idleCount > mMaxConnections || maxIdleTime > mAliveDuration){
            mPool.remove(key);
            connection.closeSafely();
            return 0;

        }else if(idleCount > 0){
            return mAliveDuration - maxIdleTime;

        }else if(inUseCount > 0){
            return mAliveDuration;

        }else{
            return -1;
        }
    }

    public ConnectionPool(){ this(sMaxConnections, sAliveDuration); }

    public ConnectionPool(int maxConnections, long aliveDuration){
        mMaxConnections = maxConnections;
        mAliveDuration = TimeUnit.MILLISECONDS.toNanos(aliveDuration);
    }

    public synchronized Connection getConnection(String host, int port){
        String key = key(host, port);
        boolean empty = mPool.isEmpty();
        Connection connection = mPool.get(key);
        if(connection != null && connection.shouldClose(mAliveDuration)){
            mPool.remove(key);
            connection.closeSafely();
            connection = null;
        }
        if(connection == null){
            connection = new Connection(host, port);
            mPool.put(key, connection);
        }
        if(empty) mCleanupExecutor.execute(mCleanupRunnable);
        return connection;
    }

    void removeConnection(String host, int port){
        Connection connection = mPool.remove(key(host, port));
        connection.closeSafely();
    }

    private static String key(String host, int port){ return host + ":" + port; }
}
