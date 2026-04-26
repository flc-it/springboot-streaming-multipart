package org.flcit.springboot.streaming.multipart.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.commons.fileupload2.core.FileItemInput;
import org.flcit.commons.core.util.StringUtils;
import org.flcit.springboot.streaming.multipart.resolver.StreamingMultipartResolver.StreamingMultipartHttpServletRequest;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
public class FileItemInputMultipartFile implements MultipartFile {

    private final FileItemInput fileItemInput;
    private final StreamingMultipartHttpServletRequest streamingMultipartHttpServletRequest;
    private boolean preserveFilename;

    /**
     * @param fileItemInput
     * @param streamingMultipartHttpServletRequest
     */
    public FileItemInputMultipartFile(FileItemInput fileItemInput, StreamingMultipartHttpServletRequest streamingMultipartHttpServletRequest) {
        this.fileItemInput = fileItemInput;
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
        return fileItemInput.getFieldName();
    }

    /**
     *
     */
    @Override
    public String getOriginalFilename() {
        final String filename = this.fileItemInput.getName();
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
        return fileItemInput.getContentType();
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
        streamingMultipartHttpServletRequest.fileItemInputReading(this.fileItemInput);
        return fileItemInput.getInputStream();
    }

    /**
     *
     */
    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        FileCopyUtils.copy(getInputStream(), Files.newOutputStream(dest.toPath()));
    }

}
