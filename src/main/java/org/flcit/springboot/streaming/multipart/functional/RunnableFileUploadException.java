package org.flcit.springboot.streaming.multipart.functional;

import java.io.IOException;

import org.apache.commons.fileupload.FileUploadException;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
@FunctionalInterface
public interface RunnableFileUploadException {

    /**
     * @throws FileUploadException
     * @throws IOException
     */
    public abstract void run() throws FileUploadException, IOException;

}
