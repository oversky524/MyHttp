package io.oversky524.myhttp;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by gaochao on 2016/4/20.
 */
public class NullOutputStream extends OutputStream {
    private long mWritten;

    @Override
    public void write(int b) throws IOException { ++mWritten; }

    @Override
    public void write(byte[] b) throws IOException { mWritten += b.length; }

    @Override
    public void write(byte[] b, int off, int len) throws IOException { mWritten += len; }

    public final long writtenSize(){ return mWritten; }
}
