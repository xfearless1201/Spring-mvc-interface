package com.cn.tianxia.pay.sl.util;
import java.io.IOException;
import java.io.OutputStream;

interface Encoder
{

    abstract int encode(byte abyte0[], int i, int j, OutputStream outputstream)
        throws IOException;

    abstract int decode(byte abyte0[], int i, int j, OutputStream outputstream)
        throws IOException;

    abstract int decode(String s, OutputStream outputstream)
        throws IOException;
}