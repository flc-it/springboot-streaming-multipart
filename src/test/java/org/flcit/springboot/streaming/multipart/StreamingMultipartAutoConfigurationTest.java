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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.flcit.commons.core.util.StringUtils;
import org.flcit.springboot.commons.test.util.MvcUtils;
import org.flcit.springboot.commons.test.util.PropertyTestUtils;
import org.flcit.springboot.streaming.multipart.TestResource.Request;
import org.flcit.springboot.streaming.multipart.TestResource.Response;
import org.flcit.springboot.streaming.multipart.TestResource.ResponseFile;
import org.flcit.springboot.streaming.multipart.resolver.StreamingMultipartResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.servlet.autoconfigure.MultipartAutoConfiguration;
import org.springframework.boot.servlet.autoconfigure.MultipartProperties;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.servlet.DispatcherServlet;

import tools.jackson.databind.json.JsonMapper;

class StreamingMultipartAutoConfigurationTest {

    private static final String PREFIX_PROPERTY = "spring.servlet.multipart";

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    JacksonAutoConfiguration.class,
                    WebMvcAutoConfiguration.class,
                    StreamingMultipartAutoConfiguration.class,
                    MultipartAutoConfiguration.class));

    @Test
    void streamingMultipartBeanOk() {
        this.contextRunner.run(context -> {
            assertThat(context).hasBean(DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME);
            assertThat(context).hasSingleBean(MultipartProperties.class);
            assertThat(context).hasSingleBean(StreamingMultipartResolver.class);
        });
    }

    @Test
    void streamingMultipartPropertiesDisabled() {
        this.contextRunner
        .withPropertyValues(
                PropertyTestUtils.getValue("spring.servlet.streaming.multipart", "enabled", Boolean.toString(false))
         )
        .run(context -> {
            assertThat(context).doesNotHaveBean(StreamingMultipartResolver.class);
            assertThat(context).hasSingleBean(MultipartProperties.class);
            assertThat(context).hasBean(DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME);
        });
    }

    @Test
    void streamingMultipartPropertiesLocation() {
        this.contextRunner
        .withPropertyValues(
                PropertyTestUtils.getValue(PREFIX_PROPERTY, "location", System.getProperty("java.io.tmpdir"))
         )
        .run(context -> {
            final MultipartProperties properties = context.getBean(MultipartProperties.class);
            assertEquals(System.getProperty("java.io.tmpdir"), properties.getLocation());
        });
        this.contextRunner
        .withPropertyValues(
                PropertyTestUtils.getValue(PREFIX_PROPERTY, "location", StringUtils.EMPTY)
         )
        .run(context -> {
            final MultipartProperties properties = context.getBean(MultipartProperties.class);
            assertEquals(StringUtils.EMPTY, properties.getLocation());
        });
    }

    @Test
    void streamingMultipartUploadTest() {
        this.contextRunner
        .withUserConfiguration(TestResource.class)
        .run(context -> {
            final Request request = new Request(1, "test");
            final List<ResponseFile> files = Arrays.asList(
                    new ResponseFile("fichier", "fichier.pdf", MediaType.APPLICATION_PDF_VALUE),
                    new ResponseFile("fichierDynamique", "fichier-dynamique.bytes", MediaType.APPLICATION_OCTET_STREAM_VALUE));
            final byte[] bytes = "blabla".getBytes();
            final Response expected1 = new Response(request, Arrays.asList(files.get(0)));
            final Response expected2 = new Response(request, files);
            MvcUtils.assertPostJsonResponse(context, TestResource.TEST_UPLOAD_URL_PATH, null,
                    new MockMultipartFile[] {
                            new MockMultipartFile("request", null, MediaType.APPLICATION_JSON_VALUE, new JsonMapper().writeValueAsBytes(request)),
                            new MockMultipartFile(files.get(0).getName(), files.get(0).getFilename(), files.get(0).getContentType(), bytes)
                    }
                    , expected1, false
            );
            MvcUtils.assertPostJsonResponse(context, TestResource.TEST_UPLOAD_FILES_URL_PATH, null,
                    new MockMultipartFile[] {
                            new MockMultipartFile("request", null, MediaType.APPLICATION_JSON_VALUE, new JsonMapper().writeValueAsBytes(request)),
                            new MockMultipartFile(files.get(0).getName(), files.get(0).getFilename(), files.get(0).getContentType(), bytes),
                            new MockMultipartFile(files.get(1).getName(), files.get(1).getFilename(), files.get(1).getContentType(), bytes)
                    }
                    , expected2, false
            );
            MvcUtils.assertPostJsonResponse(context, TestResource.TEST_UPLOAD_FILES_URL_2_PATH, null,
                    new MockMultipartFile[] {
                            new MockMultipartFile("request", null, MediaType.APPLICATION_JSON_VALUE, new JsonMapper().writeValueAsBytes(request)),
                            new MockMultipartFile(files.get(0).getName(), files.get(0).getFilename(), files.get(0).getContentType(), bytes),
                            new MockMultipartFile(files.get(1).getName(), files.get(1).getFilename(), files.get(1).getContentType(), bytes)
                    }
                    , expected2, false
            );
        });
    }

    @Test
    void streamingMultipartUploadSizeKoTest() throws Exception {
        final byte [] randomBytes = new byte[1 * 1000 * 100];
        final byte [] randomOverSizeBytes = new byte[2 * 1000 * 1000];
        new Random().nextBytes(randomBytes);
        new Random().nextBytes(randomOverSizeBytes);
        final Request request = new Request(1, "test");
        final List<ResponseFile> files = Arrays.asList(
                new ResponseFile("fichier", "fichier.pdf", MediaType.APPLICATION_PDF_VALUE)
        );
        final MockMultipartFile[] bodySmall = new MockMultipartFile[] {
                new MockMultipartFile("request", null, MediaType.APPLICATION_JSON_VALUE, new JsonMapper().writeValueAsBytes(request)),
                new MockMultipartFile(files.get(0).getName(), files.get(0).getFilename(), files.get(0).getContentType(), randomBytes)
        };
        final MockMultipartFile[] bodyOverSize = new MockMultipartFile[] {
                new MockMultipartFile("request", null, MediaType.APPLICATION_JSON_VALUE, new JsonMapper().writeValueAsBytes(request)),
                new MockMultipartFile(files.get(0).getName(), files.get(0).getFilename(), files.get(0).getContentType(), randomOverSizeBytes)
        };
        this.contextRunner
        .withUserConfiguration(TestResource.class)
        .run(context -> {
            MvcUtils.assertPostResponseStatus(context, TestResource.TEST_UPLOAD_URL_PATH, bodyOverSize, HttpStatus.CONTENT_TOO_LARGE);
            MvcUtils.assertPostResponseStatus(context, TestResource.TEST_UPLOAD_FILES_URL_PATH, bodyOverSize, HttpStatus.CONTENT_TOO_LARGE);
            MvcUtils.assertPostResponseStatus(context, TestResource.TEST_UPLOAD_FILES_URL_2_PATH, bodyOverSize, HttpStatus.CONTENT_TOO_LARGE);
        });
        this.contextRunner
        .withUserConfiguration(TestResource.class)
        .run(context -> {
            MvcUtils.assertPostResponse(context, TestResource.TEST_UPLOAD_URL_PATH, bodySmall);
            MvcUtils.assertPostResponse(context, TestResource.TEST_UPLOAD_FILES_URL_PATH, bodySmall);
            MvcUtils.assertPostResponse(context, TestResource.TEST_UPLOAD_FILES_URL_2_PATH, bodySmall);
        });
        this.contextRunner
        .withUserConfiguration(TestResource.class)
        .withPropertyValues(
                PropertyTestUtils.getValue(PREFIX_PROPERTY, "max-request-size", "100B")
        )
        .run(context -> {
            MvcUtils.assertPostResponseStatus(context, TestResource.TEST_UPLOAD_URL_PATH, bodySmall, HttpStatus.CONTENT_TOO_LARGE);
            MvcUtils.assertPostResponseStatus(context, TestResource.TEST_UPLOAD_FILES_URL_PATH, bodySmall, HttpStatus.CONTENT_TOO_LARGE);
            MvcUtils.assertPostResponseStatus(context, TestResource.TEST_UPLOAD_FILES_URL_2_PATH, bodySmall, HttpStatus.CONTENT_TOO_LARGE);
        });
        this.contextRunner
        .withUserConfiguration(TestResource.class)
        .withPropertyValues(
                PropertyTestUtils.getValue(PREFIX_PROPERTY, "max-file-size", "-1")
        )
        .run(context -> {
            MvcUtils.assertPostResponse(context, TestResource.TEST_UPLOAD_URL_PATH, bodyOverSize);
            MvcUtils.assertPostResponse(context, TestResource.TEST_UPLOAD_FILES_URL_PATH, bodyOverSize);
            MvcUtils.assertPostResponse(context, TestResource.TEST_UPLOAD_FILES_URL_2_PATH, bodyOverSize);
        });
    }

}
