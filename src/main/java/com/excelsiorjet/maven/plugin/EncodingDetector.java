package com.excelsiorjet.maven.plugin;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.excelsiorjet.Txt.s;

public class EncodingDetector {

    public static final byte[] UTF_32BE_BOM = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF};
    public static final byte[] UTF_32LE_BOM = new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00};

    public static final byte[] UTF_8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    public static final byte[] UTF_16BE_BOM = new byte[]{(byte) 0xFE, (byte) 0xFF};
    public static final byte[] UTF_16LE_BOM = new byte[]{(byte) 0xFF, (byte) 0xFE};


    public static String detectEncoding(File eula) throws MojoFailureException {

        try (InputStream is = new FileInputStream(eula)) {
            byte[] bom = new byte[4];
            IOUtils.readFully(is, bom);
            return detectEncodingByBom(bom).name();
        } catch (IOException e) {
            throw new MojoFailureException(s("JetMojo.Package.Eula.UnableToDetectEncoding", eula.getAbsolutePath()), e);
        }
    }

    /**
     * Tries to recognize bom bytes. If UTF-8 bom bytes or no bom bytes recognized returns US-ASCII charset, otherwise
     * returns corresponding UTF-16/32 charset
     */
    private static Charset detectEncodingByBom(byte[] fileBomBytes) {
        // should be ordered descending by corresponding bom bytes length
        if (startsWith(fileBomBytes, UTF_32BE_BOM)) return Charset.forName("UTF-32BE");
        else if (startsWith(fileBomBytes, UTF_32LE_BOM)) return Charset.forName("UTF-32LE");
        else if (startsWith(fileBomBytes, UTF_8_BOM)) return StandardCharsets.UTF_8;
        else if (startsWith(fileBomBytes, UTF_16BE_BOM)) return StandardCharsets.UTF_16BE;
        else if (startsWith(fileBomBytes, UTF_16LE_BOM)) return StandardCharsets.UTF_16LE;
        else return StandardCharsets.US_ASCII;
    }

    private static boolean startsWith(byte[] array, byte[] prefix) {
        int len = Math.min(array.length, prefix.length);
        for (int i = 0; i < len; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

}
