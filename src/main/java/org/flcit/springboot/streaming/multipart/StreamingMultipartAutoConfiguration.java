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

package org.flcit.springboot.streaming.multipart;

import org.flcit.springboot.streaming.multipart.resolver.StreamingMultipartResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.Servlet;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
@AutoConfiguration(before = MultipartAutoConfiguration.class)
@ConditionalOnBooleanProperty(prefix = "spring.servlet.streaming.multipart", name = "enabled", matchIfMissing = true)
@ConditionalOnClass(Servlet.class)
@ConditionalOnWebApplication(type = Type.SERVLET)
@EnableConfigurationProperties(MultipartProperties.class)
@PropertySource("classpath:streaming-multipart-lib.properties")
public class StreamingMultipartAutoConfiguration {

    /**
     * @param multipartProperties
     * @return
     */
    @Bean(DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
    public StreamingMultipartResolver multipartResolver(MultipartProperties multipartProperties) {
        final StreamingMultipartResolver multipartResolver = new StreamingMultipartResolver();
        multipartResolver.setMaxUploadSize(multipartProperties.getMaxRequestSize().toBytes());
        multipartResolver.setMaxUploadSizePerFile(multipartProperties.getMaxFileSize().toBytes());
        return multipartResolver;
    }

}
