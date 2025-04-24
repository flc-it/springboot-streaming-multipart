package org.flcit.springboot.streaming.multipart.functional;

import java.io.IOException;

import org.apache.commons.fileupload.FileUploadException;

/**
 * @param <T>
 * @since 
 * @author Florian Lestic
 */
@FunctionalInterface
public interface SupplierFileUploadException<T> {

    /**
     * @return
     * @throws FileUploadException
     * @throws IOException
     */
    T get() throws FileUploadException, IOException;

}
