package io.oversky524.myhttp;

import java.io.*;

/**
 * Created by gaochao on 2016/4/6.
 */
public class StreamUtils {
    private StreamUtils(){ throw new AssertionError("No instance"); }

    public static void copy(InputStream inputStream, boolean closeInput,
                            OutputStream outputStream, boolean closeOutput, long length) throws IOException {
        inputStream = new BufferedInputStream(inputStream);
        outputStream = new BufferedOutputStream(outputStream);
        byte[] buffer = new byte[1024];
        int readSize;
        long leftSize = length;
        while ( leftSize > 0 && (readSize = inputStream.read(buffer)) != -1){
            leftSize -= readSize;
            outputStream.write(buffer, 0, readSize);
        }
        outputStream.flush();
        if(closeInput) inputStream.close();
        if(closeOutput) outputStream.close();
    }
}
