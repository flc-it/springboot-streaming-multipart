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

import java.util.Map;

import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
public class MultipartParsingResult {

    private final MultiValueMap<String, MultipartFile> multipartFiles;

    private final Map<String, String[]> multipartParameters;

    private final Map<String, String> multipartParameterContentTypes;

    public MultipartParsingResult(MultiValueMap<String, MultipartFile> mpFiles,
            Map<String, String[]> mpParams, Map<String, String> mpParamContentTypes) {

        this.multipartFiles = mpFiles;
        this.multipartParameters = mpParams;
        this.multipartParameterContentTypes = mpParamContentTypes;
    }

    public MultiValueMap<String, MultipartFile> getMultipartFiles() {
        return this.multipartFiles;
    }

    public Map<String, String[]> getMultipartParameters() {
        return this.multipartParameters;
    }

    public Map<String, String> getMultipartParameterContentTypes() {
        return this.multipartParameterContentTypes;
    }
}
