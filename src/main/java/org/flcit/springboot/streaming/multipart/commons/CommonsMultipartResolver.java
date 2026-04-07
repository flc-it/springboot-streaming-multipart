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

package org.flcit.springboot.streaming.multipart.commons;

import java.nio.charset.Charset;

import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.HttpServletRequest;

/**s
 * 
 * @since 
 * @author Florian Lestic
 */
public abstract class CommonsMultipartResolver implements MultipartResolver {

    protected final Log logger = LogFactory.getLog(getClass());

    private long maxUploadSize;
    private long maxUploadSizePerFile;
    private String defaultEncoding;

    public void setMaxUploadSize(long maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }

    public void setMaxUploadSizePerFile(long maxUploadSizePerFile) {
        this.maxUploadSizePerFile = maxUploadSizePerFile;
    }

    public void setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    protected String getDefaultEncoding() {
        return defaultEncoding != null ? defaultEncoding : WebUtils.DEFAULT_CHARACTER_ENCODING;
    }

    @Override
    public boolean isMultipart(HttpServletRequest request) {
        return JakartaServletFileUpload.isMultipartContent(request);
    }

    protected String determineEncoding(HttpServletRequest request) {
        String encoding = request.getCharacterEncoding();
        if (encoding == null) {
            encoding = getDefaultEncoding();
        }
        return encoding;
    }

    @SuppressWarnings("rawtypes")
    protected JakartaServletFileUpload prepareFileUpload(String encoding) {
        final JakartaServletFileUpload actualFileUpload = new JakartaServletFileUpload<>();
        actualFileUpload.setMaxSize(this.maxUploadSize);
        actualFileUpload.setMaxFileSize(this.maxUploadSizePerFile);
        actualFileUpload.setHeaderCharset(Charset.forName(encoding != null ? encoding : defaultEncoding));
        return actualFileUpload;
    }

    @Override
    public void cleanupMultipart(MultipartHttpServletRequest request) {
        // NOTHING
    }

}
