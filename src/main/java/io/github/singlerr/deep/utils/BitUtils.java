package io.github.singlerr.deep.utils;

/***
 * An utility class for handling bit operations
 * @author Singlerr
 */
public final class BitUtils {
    /***
     * Create bit mask for given mask size
     * @param maskSize size of mask
     * @return bit mask
     */
    public static long createBitMask(int maskSize){
        StringBuilder binary = new StringBuilder();
        for(int i = 0; i<maskSize;i++)
            binary.append("1");
        return Long.parseLong(binary.toString(),2);
    }

}
