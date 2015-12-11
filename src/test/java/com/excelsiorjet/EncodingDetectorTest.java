package com.excelsiorjet;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class EncodingDetectorTest {

    @Test
    public void testAscii() throws Exception {
        testFile(StandardCharsets.US_ASCII.name(), "/com/excelsiorjet/eula-ascii.txt");
    }

    @Test
    public void testUTF8() throws Exception {
        testFile(StandardCharsets.UTF_8.name(), "/com/excelsiorjet/eula-utf8.txt");
    }

    @Test
    public void testUTF16BE() throws Exception {
        testFile(StandardCharsets.UTF_16BE.name(), "/com/excelsiorjet/eula-utf16be.txt");
    }

    @Test
    public void testUTF16LE() throws Exception {
        testFile(StandardCharsets.UTF_16LE.name(), "/com/excelsiorjet/eula-utf16le.txt");
    }

    @Test
    public void testUTF32BE() throws Exception {
        testFile(Charset.forName("UTF-32BE").name(), "/com/excelsiorjet/eula-utf32be.txt");
    }

    @Test
    public void testUTF32LE() throws Exception {
        testFile(Charset.forName("UTF-32LE").name(), "/com/excelsiorjet/eula-utf32le.txt");
    }

    @Test
    public void testOneByte() throws Exception {
        testFile(StandardCharsets.US_ASCII.name(), "/com/excelsiorjet/eula-one-byte.txt");
    }

    private void testFile(String expectedEncoding, String filePath) throws URISyntaxException, IOException {
        File eula = Paths.get(getClass().getResource(filePath).toURI()).toFile();
        String encoding = EncodingDetector.detectEncoding(eula);
        assertEquals(expectedEncoding, encoding);
    }

}
