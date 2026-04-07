/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flcit.springboot.streaming.multipart.resolver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.core.FileUploadSizeException;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.commons.io.function.IORunnable;
import org.apache.commons.io.function.IOSupplier;
import org.flcit.springboot.streaming.multipart.commons.CommonsMultipartResolver;
import org.flcit.springboot.streaming.multipart.commons.MultipartParsingResult;
import org.flcit.springboot.streaming.multipart.file.FileItemInputMultipartFile;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
public class StreamingMultipartResolver extends CommonsMultipartResolver {

    private static final String EXCEPTION_MESSAGE = "Failed to parse multipart servlet request";

    @Override
    public MultipartHttpServletRequest resolveMultipart(final HttpServletRequest request) throws MultipartException {
        Assert.notNull(request, "Request must not be null");
        return new StreamingMultipartHttpServletRequest(request);
    }

    /**
     * 
     * @since 
     * @author Florian Lestic
     */
    public class StreamingMultipartHttpServletRequest extends DefaultMultipartHttpServletRequest {

        private final FileItemInputIterator it;
        private final String encodingRequest;
        private final JakartaServletFileUpload<?,?> fileUpload;
        private FileItemInput currentFileItemInput;

        public StreamingMultipartHttpServletRequest(HttpServletRequest request) {
            super(request);
            this.encodingRequest = StreamingMultipartResolver.this.determineEncoding(request);
            this.fileUpload = StreamingMultipartResolver.this.prepareFileUpload(encodingRequest);
            this.it = run(() -> this.fileUpload.getItemIterator(request));
        }

        @Override
        protected void initializeMultipart() {
            run(() -> {
                final MultipartParsingResult result = parseFileItems();
                setMultipartFiles(result.getMultipartFiles());
                setMultipartParameterContentTypes(result.getMultipartParameterContentTypes());
                setMultipartParameters(result.getMultipartParameters());
            });
        }

        @Override
        public MultipartFile getFile(String name) {
            MultipartFile res = getMultipartFiles().getFirst(name);
            if (res != null) {
                return res;
            }
            initializeMultipart();
            return getMultipartFiles().getFirst(name);
        }

        public boolean hasNext() {
            return run(() -> currentFileItemInput != null || it.hasNext());
        }

        public void consumeStreams(Consumer<FileItemInput> consumer) {
            if (currentFileItemInput != null) {
                consumer.accept(currentFileItemInput);
                currentFileItemInput = null;
            }
            run(() -> {
                while (it.hasNext()) {
                    consumer.accept(it.next());
                }
            });
        }

        private void run(IORunnable runnable) {
            run(() -> {
                runnable.run();
                return null;
            });
        }

        private <T> T run(IOSupplier<T> supplier) {
            try {
                return supplier.get();
            }
            catch (FileUploadSizeException ex) {
                throw new MaxUploadSizeExceededException(ex.getPermitted(), ex);
            }
            catch (IOException ex) {
                throw new MultipartException(EXCEPTION_MESSAGE, ex);
            }
        }

        public void consumeFiles(Consumer<MultipartFile> consumer) {
            if (currentFileItemInput != null) {
                consumer.accept(parseUploadFile(currentFileItemInput));
                currentFileItemInput = null;
            }
            try {
                while (it.hasNext()) {
                    FileItemInput fileItemInput = it.next();
                    if (!fileItemInput.isFormField()) {
                        consumer.accept(parseUploadFile(fileItemInput));
                    }
                }
            }
            catch (FileUploadSizeException ex) {
                throw new MaxUploadSizeExceededException(fileUpload.getMaxFileSize(), ex);
            }
            catch (IOException ex) {
                throw new MultipartException(EXCEPTION_MESSAGE, ex);
            }
        }

        private MultipartParsingResult parseFileItems() throws IOException {

            MultiValueMap<String, MultipartFile> multipartFiles = new LinkedMultiValueMap<>();
            Map<String, String[]> multipartParameters = new HashMap<>();
            Map<String, String> multipartParameterContentTypes = new HashMap<>();

            if (isResolved()) {
                multipartFiles.addAll(getMultipartFiles());
                multipartParameters.putAll(getMultipartParameters());
                multipartParameterContentTypes.putAll(getMultipartParameterContentTypes());
            }

            // Extract only begining multipart parameters and multipart files are stream after
            if (currentFileItemInput == null) {
                while (this.it.hasNext()) {
                    FileItemInput fileItemInput = it.next();
                    if (fileItemInput.isFormField() || !StringUtils.hasLength(fileItemInput.getName())) {
                        parseFormField(fileItemInput, multipartParameters, multipartParameterContentTypes);
                    }
                    else {
                        //1st file must be preload
                        multipartFiles.add(fileItemInput.getFieldName(), parseUploadFile(fileItemInput));
                        this.currentFileItemInput = fileItemInput;
                        //Files must be send streaming after pre process
                        return new MultipartParsingResult(multipartFiles, multipartParameters, multipartParameterContentTypes);
                    }
                }
            }
            return new MultipartParsingResult(multipartFiles, multipartParameters, multipartParameterContentTypes);
        }

        private void parseFormField(FileItemInput fileItem, Map<String, String[]> multipartParameters, Map<String, String> multipartParameterContentTypes) throws IOException {
            final String value = getValue(fileItem);
            String[] curParam = multipartParameters.get(fileItem.getFieldName());
            // simple form field or array of simple form fields
            curParam = curParam == null ? new String[] { value } : StringUtils.addStringToArray(curParam, value);
            multipartParameters.put(fileItem.getFieldName(), curParam);
            multipartParameterContentTypes.put(fileItem.getFieldName(), fileItem.getContentType());
        }

        private final String getValue(final FileItemInput fileItem) throws IOException {
            final String partEncoding = determineEncoding(fileItem.getContentType(), encodingRequest);
            final byte[] bytes = FileCopyUtils.copyToByteArray(fileItem.getInputStream());
            try {
                return new String(bytes, partEncoding);
            }
            catch (UnsupportedEncodingException ex) {
                if (StreamingMultipartResolver.this.logger.isWarnEnabled()) {
                    StreamingMultipartResolver.this.logger.warn("Could not decode multipart item '" + fileItem.getFieldName() +
                            "' with encoding '" + partEncoding + "': using platform default");
                }
                return new String(bytes);
            }
        }

        private MultipartFile parseUploadFile(FileItemInput fileItem) {
            // multipart file field
            MultipartFile file = createMultipartFile(fileItem);
            LogFormatUtils.traceDebug(StreamingMultipartResolver.this.logger, traceOn ->
                "Part '" + file.getName() + "', size " + file.getSize() +
                " bytes, filename='" + file.getOriginalFilename() + "'" +
                (Boolean.TRUE.equals(traceOn) ? ", storage=streaming" : org.flcit.commons.core.util.StringUtils.EMPTY)
            );
            return file;
        }

        private String determineEncoding(String contentTypeHeader, String defaultEncoding) {
            if (!StringUtils.hasText(contentTypeHeader)) {
                return defaultEncoding;
            }
            MediaType contentType = MediaType.parseMediaType(contentTypeHeader);
            Charset charset = contentType.getCharset();
            return (charset != null ? charset.name() : defaultEncoding);
        }

        private MultipartFile createMultipartFile(FileItemInput fileItemInput) {
            return new FileItemInputMultipartFile(fileItemInput, this);
        }

        public void fileItemInputReading(FileItemInput fileItemInput) {
            if (fileItemInput == this.currentFileItemInput) {
                this.currentFileItemInput = null;
            }
        }

    }

}
