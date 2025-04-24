package org.flcit.springboot.streaming.multipart.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.commons.fileupload.FileItemStream;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import org.flcit.commons.core.util.StringUtils;
import org.flcit.springboot.streaming.multipart.resolver.StreamingMultipartResolver.StreamingMultipartHttpServletRequest;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
public class FileItemStreamMultipartFile implements MultipartFile {

    private final FileItemStream fileItemStream;
    private final StreamingMultipartHttpServletRequest streamingMultipartHttpServletRequest;
    private boolean preserveFilename;

    /**
     * @param fileItemStream
     * @param streamingMultipartHttpServletRequest
     */
    public FileItemStreamMultipartFile(FileItemStream fileItemStream, StreamingMultipartHttpServletRequest streamingMultipartHttpServletRequest) {
        this.fileItemStream = fileItemStream;
        this.streamingMultipartHttpServletRequest = streamingMultipartHttpServletRequest;
    }

    /**
     * @param preserveFilename
     */
    public void setPreserveFilename(boolean preserveFilename) {
        this.preserveFilename = preserveFilename;
    }

    /**
     *
     */
    @Override
    public String getName() {
        return fileItemStream.getFieldName();
    }

    /**
     *
     */
    @Override
    public String getOriginalFilename() {
        final String filename = this.fileItemStream.getName();
        if (filename == null) {
            // Should never happen.
            return StringUtils.EMPTY;
        }
        if (this.preserveFilename) {
            // Do not try to strip off a path...
            return filename;
        }
        // Cut off at latest possible point (windows-style path and Unix-style path)
        final int pos = Math.max(filename.lastIndexOf('\\'), filename.lastIndexOf('/'));
        return pos != -1 ? filename.substring(pos + 1) : filename;
    }

    /**
     *
     */
    @Override
    public String getContentType() {
        return fileItemStream.getContentType();
    }

    /**
     *
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     *
     */
    @Override
    public long getSize() {
        return -1;
    }

    /**
     *
     */
    @Override
    public byte[] getBytes() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    @Override
    public InputStream getInputStream() throws IOException {
        streamingMultipartHttpServletRequest.fileItemStreamReading(this.fileItemStream);
        return fileItemStream.openStream();
    }

    /**
     *
     */
    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        FileCopyUtils.copy(getInputStream(), Files.newOutputStream(dest.toPath()));
    }

}
