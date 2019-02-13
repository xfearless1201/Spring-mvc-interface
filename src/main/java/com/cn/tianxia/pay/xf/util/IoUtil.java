package com.cn.tianxia.pay.xf.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class IoUtil
{

    public static String readInputStream(InputStream inputStream, String encoding)
    {
        if (inputStream == null)
        {
            return null;
        }

        String str = null;
        try
        {
            int length = 0;
            byte[] bytes = new byte[1024];
            StringBuffer buffer = new StringBuffer();
            while ((length = inputStream.read(bytes, 0, bytes.length)) != -1)
            {
                String read = new String(bytes, 0, length, "ISO_8859_1");
                buffer.append(read);
            }
            byte[] allBytes = buffer.toString().getBytes("ISO_8859_1");
            str = new String(allBytes, encoding);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                inputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return str;
    }

    public static InputStream getResourceAsStream(String name)
    {
        InputStream iis = IoUtil.class.getClassLoader().getResourceAsStream(name);
        return iis;
    }

    public static String getResource(String name)
    {
        URL url = IoUtil.class.getClassLoader().getResource(name);
        return url.getFile();
    }

}
