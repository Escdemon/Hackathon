package com.cgi.commons.rest.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;

/**
 * Temporary file to send into an output stream.
 */
public class TmpFile implements StreamingOutput {

    /** Path of the file to send. */
    private Path file;
    /** Input stream open onto the file. */
    private InputStream is;
    /** Indicates whether the file should be deleted. */
    private boolean deleteFile;

    /**
     * Creates a new temporary file to send into an output stream.
     * 
     * @param file
     *            File to send.
     */
    public TmpFile(File file) {
        this(file, null, true);
    }

    /**
     * Creates a new temporary file to send into an output stream.
     * 
     * @param file
     *            File to send.
     * @param inputStream
     *            Input stream to copy.
     */
    public TmpFile(File file, InputStream inputStream) {
        this(file, inputStream, true);
    }

    /**
     * Creates a new temporary file to send into an output stream.
     * 
     * @param file
     *            File to send.
     * @param inputStream
     *            Input stream to copy.
     * @param deleteFile
     *            Indicates whether the file should be deleted.
     */
    public TmpFile(File file, InputStream inputStream, boolean deleteFile) {
        this.file = file.toPath();
        this.is = inputStream;
        this.deleteFile = deleteFile;
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        try {
            if (is != null) {
                IOUtils.copy(is, output);
            } else {
                Files.copy(file, output);
            }
        } finally {
            IOUtils.closeQuietly(is);
            if (deleteFile) {
                Files.delete(file);
            }
        }
    }

}
