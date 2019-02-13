package com.cn.tianxia.pay.sl.util;
import java.io.IOException;
import java.io.OutputStream;

class Base64Encoder
    implements Encoder
{

    protected void initialiseDecodingTable()
    {
        for(int i = 0; i < encodingTable.length; i++)
            decodingTable[encodingTable[i]] = (byte)i;

    }

    protected Base64Encoder()
    {
        padding = 61;
        initialiseDecodingTable();
    }

    public int encode(byte abyte0[], int i, int j, OutputStream outputstream)
        throws IOException
    {
        int k = j % 3;
        int l = j - k;
        for(int l1 = i; l1 < i + l; l1 += 3)
        {
            int i1 = abyte0[l1] & 0xff;
            int j1 = abyte0[l1 + 1] & 0xff;
            int k1 = abyte0[l1 + 2] & 0xff;
            outputstream.write(encodingTable[i1 >>> 2 & 0x3f]);
            outputstream.write(encodingTable[(i1 << 4 | j1 >>> 4) & 0x3f]);
            outputstream.write(encodingTable[(j1 << 2 | k1 >>> 6) & 0x3f]);
            outputstream.write(encodingTable[k1 & 0x3f]);
        }

        switch(k)
        {
        case 1: // '\001'
            int j3 = abyte0[i + l] & 0xff;
            int i2 = j3 >>> 2 & 0x3f;
            int k2 = j3 << 4 & 0x3f;
            outputstream.write(encodingTable[i2]);
            outputstream.write(encodingTable[k2]);
            outputstream.write(padding);
            outputstream.write(padding);
            break;

        case 2: // '\002'
            int k3 = abyte0[i + l] & 0xff;
            int l3 = abyte0[i + l + 1] & 0xff;
            int j2 = k3 >>> 2 & 0x3f;
            int l2 = (k3 << 4 | l3 >>> 4) & 0x3f;
            int i3 = l3 << 2 & 0x3f;
            outputstream.write(encodingTable[j2]);
            outputstream.write(encodingTable[l2]);
            outputstream.write(encodingTable[i3]);
            outputstream.write(padding);
            break;
        }
        return (l / 3) * 4 + (k != 0 ? 4 : 0);
    }

    private boolean ignore(char c)
    {
        return c == '\n' || c == '\r' || c == '\t' || c == ' ';
    }

    public int decode(byte abyte0[], int i, int j, OutputStream outputstream)
        throws IOException
    {
        int k = 0;
        int l;
        for(l = i + j; l > i && ignore((char)abyte0[l - 1]); l--);
        int i1 = i;
        int j1 = l - 4;
        for(i1 = nextI(abyte0, i1, j1); i1 < j1; i1 = nextI(abyte0, i1, j1))
        {
            byte byte0 = decodingTable[abyte0[i1++]];
            i1 = nextI(abyte0, i1, j1);
            byte byte1 = decodingTable[abyte0[i1++]];
            i1 = nextI(abyte0, i1, j1);
            byte byte2 = decodingTable[abyte0[i1++]];
            i1 = nextI(abyte0, i1, j1);
            byte byte3 = decodingTable[abyte0[i1++]];
            outputstream.write(byte0 << 2 | byte1 >> 4);
            outputstream.write(byte1 << 4 | byte2 >> 2);
            outputstream.write(byte2 << 6 | byte3);
            k += 3;
        }

        k += decodeLastBlock(outputstream, (char)abyte0[l - 4], (char)abyte0[l - 3], (char)abyte0[l - 2], (char)abyte0[l - 1]);
        return k;
    }

    private int nextI(byte abyte0[], int i, int j)
    {
        for(; i < j && ignore((char)abyte0[i]); i++);
        return i;
    }

    public int decode(String s, OutputStream outputstream)
        throws IOException
    {
        int i = 0;
        int j;
        for(j = s.length(); j > 0 && ignore(s.charAt(j - 1)); j--);
        int k = 0;
        int l = j - 4;
        for(k = nextI(s, k, l); k < l; k = nextI(s, k, l))
        {
            byte byte0 = decodingTable[s.charAt(k++)];
            k = nextI(s, k, l);
            byte byte1 = decodingTable[s.charAt(k++)];
            k = nextI(s, k, l);
            byte byte2 = decodingTable[s.charAt(k++)];
            k = nextI(s, k, l);
            byte byte3 = decodingTable[s.charAt(k++)];
            outputstream.write(byte0 << 2 | byte1 >> 4);
            outputstream.write(byte1 << 4 | byte2 >> 2);
            outputstream.write(byte2 << 6 | byte3);
            i += 3;
        }

        i += decodeLastBlock(outputstream, s.charAt(j - 4), s.charAt(j - 3), s.charAt(j - 2), s.charAt(j - 1));
        return i;
    }

    private int decodeLastBlock(OutputStream outputstream, char c, char c1, char c2, char c3)
        throws IOException
    {
        if(c2 == padding)
        {
            byte byte0 = decodingTable[c];
            byte byte3 = decodingTable[c1];
            outputstream.write(byte0 << 2 | byte3 >> 4);
            return 1;
        }
        if(c3 == padding)
        {
            byte byte1 = decodingTable[c];
            byte byte4 = decodingTable[c1];
            byte byte6 = decodingTable[c2];
            outputstream.write(byte1 << 2 | byte4 >> 4);
            outputstream.write(byte4 << 4 | byte6 >> 2);
            return 2;
        } else
        {
            byte byte2 = decodingTable[c];
            byte byte5 = decodingTable[c1];
            byte byte7 = decodingTable[c2];
            byte byte8 = decodingTable[c3];
            outputstream.write(byte2 << 2 | byte5 >> 4);
            outputstream.write(byte5 << 4 | byte7 >> 2);
            outputstream.write(byte7 << 6 | byte8);
            return 3;
        }
    }

    private int nextI(String s, int i, int j)
    {
        for(; i < j && ignore(s.charAt(i)); i++);
        return i;
    }

    protected final byte encodingTable[] = {
        65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 
        75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 
        85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 
        101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 
        111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 
        121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 
        56, 57, 43, 47
    };
    protected byte padding;
    protected final byte decodingTable[] = new byte[128];
}