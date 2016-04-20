package io.oversky524.myhttp;

import java.io.*;

/**
 * Created by gaochao on 2016/4/6.
 */
public class StreamUtils {
    private static final boolean DEBUG = false;
    private StreamUtils(){ throw new AssertionError("No instance"); }

    public static void copy(InputStream inputStream, boolean closeInput,
                            OutputStream outputStream, boolean closeOutput) throws IOException {
        copy(inputStream, closeInput, outputStream, closeOutput, Long.MAX_VALUE);
    }

    public static void copy(InputStream inputStream, boolean closeInput,
                            OutputStream outputStream, boolean closeOutput, long length) throws IOException {
        inputStream = new BufferedInputStream(inputStream);
        outputStream = new BufferedOutputStream(outputStream);
        final int size = 1024;
        byte[] buffer = new byte[size];
        int readSize;
        long leftSize = length;
        while ( leftSize > 0 && (readSize = inputStream.read(buffer, 0, (int)Math.min(size, leftSize))) != -1){
            leftSize -= readSize;
            outputStream.write(buffer, 0, readSize);
            if(DEBUG) System.out.println("length=" + length + ",readSize=" + readSize);
        }
        outputStream.flush();
        if(closeInput) inputStream.close();
        if(closeOutput) outputStream.close();
    }
}
