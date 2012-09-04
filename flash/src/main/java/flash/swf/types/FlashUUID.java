////////////////////////////////////////////////////////////////////////////////
//
//  ADOBE SYSTEMS INCORPORATED
//  Copyright 2003-2006 Adobe Systems Incorporated
//  All Rights Reserved.
//
//  NOTICE: Adobe permits you to use, modify, and distribute this file
//  in accordance with the terms of the license agreement accompanying it.
//
////////////////////////////////////////////////////////////////////////////////

package flash.swf.types;

import java.util.Arrays;

/**
 * Represents a Flash UUID.
 */
public final class FlashUUID
{
    private static final int kUUIDSize = 16;

    public FlashUUID(byte[] bytes)
    {
        if (bytes == null) throw new NullPointerException();
        this.bytes = bytes;
    }

    public FlashUUID()
    {
        this.bytes = new byte[kUUIDSize];
    }

    public final byte[] bytes;

    public String toString()
    {
        return stringify(bytes);
    }

    private static String stringify(byte buf[])
    {
        StringBuilder sb = new StringBuilder(2 * buf.length);
        for (int i = 0; i < buf.length; i++)
        {
            int h = (buf[i] & 0xf0) >> 4;
            int l = (buf[i] & 0x0f);
            sb.append((char) ((h > 9) ? 'A' + h - 10 : '0' + h));
            sb.append((char) ((l > 9) ? 'A' + l - 10 : '0' + l));
        }
        return sb.toString();
    }
    
    public int hashCode()
    {
        int length = bytes.length;
        int code = length;
        for (int i=0; i < length; i++)
        {
            code = (code << 1) ^ bytes[i];
        }
        return code;
    }

    public boolean equals(Object object)
    {
        boolean isEqual = false;

        if (object instanceof FlashUUID)
        {
            FlashUUID flashUUID = (FlashUUID) object;
            if ( Arrays.equals(flashUUID.bytes, this.bytes) )
            {
                isEqual = true;
            }
        }

        return isEqual;
    }
}
