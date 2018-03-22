package com.cgi.commons.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.cgi.commons.db.FileDbManager;
import com.cgi.commons.ref.entity.FileContainer;

/**
 * Manager for temporary files.
 */
public class TmpFileManager {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(FileDbManager.class);
    /** Path to the temporary directory. */
    private static final String TMP_DIRECTORY = System.getProperty("java.io.tmpdir");
    /** A character to flag our files. */
    private static final byte SUN = "¤".getBytes(StandardCharsets.US_ASCII)[0];// Specific character to add or detect the file name into a stream.
    /** Default name is no name is provided. */
    private static final String DEFAULT_NAME = "untitled";

    /** Input stream. */
    private InputStream is;
    /** File name. */
    private String name;
    /** Number of bytes to subtract to get the real file length. */
    private int delta;

    /**
     * Creates a new manager.
     * 
     * @param inputStream
     *            Input stream.
     */
    public TmpFileManager(InputStream inputStream) {
        this(inputStream, null);
    }

    /**
     * Creates a new manager.
     * 
     * @param inputStream
     *            Input stream.
     * @param name
     *            File Name. If this argument is {@code null}, the file name is {@value #DEFAULT_NAME}.
     */
    public TmpFileManager(InputStream inputStream, String name) {
        this.is = inputStream;
        this.name = (name != null) ? name.trim() : DEFAULT_NAME;
    }

    /**
     * Creates a temporary file.
     * 
     * @param extractName
     *            Indicates whether this method attempts to read the name from the given input stream with the following
     *            algorithm :
     *            <ol>
     *            <li>The {@code 2} first bytes are read and tests again the character '{@value #SUN}'.</li>
     *            <li>If these tests succeed, the file name's length is read until the character '{@value #SUN}' is
     *            read.</li>
     *            <li>Then the file name is read; otherwise the name is set to the {@code name} specified into the
     *            constructor of this manager or {@value #DEFAULT_NAME}.</li>
     *            </ol>
     * @param appendName
     *            Indicates whether the file name should be added to the content (see the algorithm above).
     * @return The created temporary file or {@code null} if the given input stream is {@code null}.
     * @throws TechnicalException
     *             If an error occurs while reading the input stream or writing the temporary file.
     */
    public FileContainer createFile(boolean extractName, boolean appendName) throws TechnicalException {
        if (is == null) {
            return null;
        }

        try {
            if (extractName) {
                extractName();
            }
            String uuid = UUID.randomUUID().toString();
            Path tmpFile = Paths.get(TMP_DIRECTORY, uuid + ".tmp");

            if (appendName) {
                appendName();
            }
            Files.copy(is, tmpFile);
            FileContainer container = new FileContainer();
            container.setName(name);
            container.setUuid(uuid);
            return container;

        } catch (IOException e) {
            String msg = "Error while creating file " + name;
            LOGGER.error(msg, e);
            throw new TechnicalException(msg, e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Extract the file name from the current input stream.
     * 
     * @return The extracted name or {@value #DEFAULT_NAME}.
     * @throws IOException
     *             If an I/O error occurs.
     * @see #createFile(boolean, boolean)
     */
    public String extractName() throws IOException {
        int count = 2;// Two firsts '¤'
        byte[] twoFirstBytes = new byte[2];
        twoFirstBytes[0] = (byte) is.read();
        twoFirstBytes[1] = (byte) is.read();

        if (twoFirstBytes[0] == SUN && twoFirstBytes[1] == SUN) {
            // The input stream starts with '¤¤'
            int len = 0;
            int nextByte;
            // Let's read the length until another sun is found.
            while (((nextByte = is.read())) != SUN) {
                len += nextByte;
                count++;// add next byte
            }
            count++; // the third '¤'
            byte[] b = new byte[len];

            if (is.read(b) == len) {
            	// Base64 is used to preserve Latin characters.
                name = new String(Base64.decodeBase64(b), StandardCharsets.UTF_8);
                count += len;// file name length
                delta = count;
            }

        } else if (is.markSupported()) {
            name = DEFAULT_NAME;
            // The file name is not stored into the input stream, the stream is reset as it supports it.
            is.reset();

        } else {
            name = DEFAULT_NAME;
            // Returns an inputStream with the 2 first bytes if the given input stream does not support reset.
            SequenceInputStream sis = new SequenceInputStream(new ByteArrayInputStream(twoFirstBytes), is);
            is = sis;
        }
        return name;
    }

    /**
     * @return The number of bytes read into the file to get the file name. The real file length is
     *         {@code file.length() - getFileLengthDelta}. It returns 0 if the method
     *         {@link TmpFileManager#extractName()} was not called previously or the file name is not stored into the
     *         file.
     */
    public int getFileLengthDelta() {
        return delta;
    }

    /**
     * @param uuid
     *            file's identifier.
     * @return A file {@code uuid.tmp} located into the temporary directory. It may not exists.
     */
    public static File getTemporaryFile(String uuid) {
        return new File(TMP_DIRECTORY, uuid + ".tmp");
    }

    /**
     * Append the file name to the beginning of the stream.
     * 
     * @throws IOException
     *             If an I/O error occurs.
     * @see #createFile(boolean, boolean)
     */
    private void appendName() throws IOException {
        if (name.isEmpty() || DEFAULT_NAME.equals(name)) {
            return;
        }
        byte max = Byte.MAX_VALUE;
        // Base64 is used to preserve Latin characters.
        byte[] nameBytes = Base64.encodeBase64(name.getBytes(StandardCharsets.UTF_8));
        int len = nameBytes.length / max;
        int remainder = nameBytes.length % max;
        byte[] bytes = new byte[2 + len + 1 + 1 + nameBytes.length];// [¤,¤,128,128,[0->127],¤,filename]
        int i = 0;
        int j;
        bytes[i++] = SUN;// 63
        bytes[i++] = SUN;// 63

        for (j = 0; j < len; j++) {
            bytes[i + j] = max;// 127
        }
        i += len;
        bytes[i++] = (byte) remainder;// It may be 0.
        bytes[i++] = (byte) SUN;// 63

        for (j = 0; j < nameBytes.length; j++) {
            bytes[i + j] = nameBytes[j];
        }
        SequenceInputStream sis = new SequenceInputStream(new ByteArrayInputStream(bytes), is);
        is = sis;
    }

}
