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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import org.flcit.springboot.commons.test.MockitoBaseTest;
import org.flcit.springboot.commons.test.util.LogTestUtils;
import org.flcit.springboot.streaming.multipart.resolver.StreamingMultipartResolver.StreamingMultipartHttpServletRequest;

class StreamingMultipartResolverTest implements MockitoBaseTest {

    @Mock
    private FileItemIterator iterator;

    @Mock
    private FileItemStream stream;

    @Mock
    private ServletFileUpload fileUpload;

    @Mock
    private MockStreamingMultipartResolver streamingMultipartResolver;

    @Test
    void tests() throws Exception {
        when(streamingMultipartResolver.prepareFileUpload(nullable(String.class))).thenReturn(fileUpload);
        when(streamingMultipartResolver.resolveMultipart(any())).thenCallRealMethod();
        when(fileUpload.getItemIterator(any(HttpServletRequest.class))).thenThrow(FileUploadException.class);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        assertThrows(MultipartException.class, () -> streamingMultipartResolver.resolveMultipart(request));
        when(fileUpload.getItemIterator(any(HttpServletRequest.class))).thenThrow(IOException.class);
        assertThrows(MultipartException.class, () -> streamingMultipartResolver.resolveMultipart(request));
    }

    @Test
    void initializeMultipartTest() throws Exception {
        prepareMocks();
        final StreamingMultipartHttpServletRequest request = ((StreamingMultipartHttpServletRequest) streamingMultipartResolver.resolveMultipart(new MockHttpServletRequest()));
        when(iterator.hasNext()).thenThrow(FileUploadException.class);
        assertThrows(MultipartException.class, () -> request.initializeMultipart());
        doThrow(IOException.class).when(iterator).hasNext();
        assertThrows(MultipartException.class, () -> request.initializeMultipart());
        doThrow(FileUploadBase.SizeLimitExceededException.class).when(iterator).hasNext();
        assertThrows(MaxUploadSizeExceededException.class, () -> request.initializeMultipart());
        doReturn(false).when(iterator).hasNext();
        assertDoesNotThrow(() -> request.initializeMultipart());
    }

    @Test
    void hasNextTest() throws Exception {
        prepareMocks();
        final StreamingMultipartHttpServletRequest request = ((StreamingMultipartHttpServletRequest) streamingMultipartResolver.resolveMultipart(new MockHttpServletRequest()));
        doReturn(true).when(iterator).hasNext();
        assertTrue(request.hasNext());
        doReturn(false).when(iterator).hasNext();
        assertFalse(request.hasNext());
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(stream);
        when(stream.getFieldName()).thenReturn("field");
        final Log logger = mock(Log.class);
        LogTestUtils.setLogger(streamingMultipartResolver, "logger", logger);
        request.initializeMultipart();
        assertTrue(request.hasNext());
    }

    @Test
    void consumeStreamsTest() throws Exception {
        prepareMocks();
        final StreamingMultipartHttpServletRequest request = ((StreamingMultipartHttpServletRequest) streamingMultipartResolver.resolveMultipart(new MockHttpServletRequest()));
        when(iterator.hasNext()).thenThrow(FileUploadException.class);
        assertThrows(MultipartException.class, () -> request.consumeStreams(s -> { }));
        doThrow(IOException.class).when(iterator).hasNext();
        assertThrows(MultipartException.class, () -> request.consumeStreams(s -> { }));
        doThrow(FileUploadBase.SizeLimitExceededException.class).when(iterator).hasNext();
        assertThrows(MaxUploadSizeExceededException.class, () -> request.consumeStreams(s -> { }));
        doThrow(FileUploadBase.FileSizeLimitExceededException.class).when(iterator).hasNext();
        assertThrows(MaxUploadSizeExceededException.class, () -> request.consumeStreams(s -> { }));
    }

    @Test
    void consumeFilesTest() throws Exception {
        prepareMocks();
        final StreamingMultipartHttpServletRequest request = ((StreamingMultipartHttpServletRequest) streamingMultipartResolver.resolveMultipart(new MockHttpServletRequest()));
        when(iterator.hasNext()).thenThrow(FileUploadException.class);
        assertThrows(MultipartException.class, () -> request.consumeFiles(s -> { }));
        doThrow(IOException.class).when(iterator).hasNext();
        assertThrows(MultipartException.class, () -> request.consumeFiles(s -> { }));
        doThrow(FileUploadBase.SizeLimitExceededException.class).when(iterator).hasNext();
        assertThrows(MaxUploadSizeExceededException.class, () -> request.consumeFiles(s -> { }));
        doThrow(FileUploadBase.FileSizeLimitExceededException.class).when(iterator).hasNext();
        assertThrows(MaxUploadSizeExceededException.class, () -> request.consumeFiles(s -> { }));
    }

    @SuppressWarnings("unchecked")
    @Test
    void consumeFilesFormFieldTest() throws Exception {
        prepareMocks();
        final StreamingMultipartHttpServletRequest request = ((StreamingMultipartHttpServletRequest) streamingMultipartResolver.resolveMultipart(new MockHttpServletRequest()));
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(stream);
        when(stream.isFormField()).thenReturn(true);
        Consumer<MultipartFile> consumer = mock(Consumer.class);
        assertDoesNotThrow(() -> request.consumeFiles(consumer));
        verify(consumer, never()).accept(any());
    }

    @Test
    void parseFormFieldTest() throws Exception {
        prepareMocks();
        final String field = "field";
        final String value = "{'id':5,'libelle':'lib'}";
        final StreamingMultipartHttpServletRequest request = ((StreamingMultipartHttpServletRequest) streamingMultipartResolver.resolveMultipart(new MockHttpServletRequest()));
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(stream);
        when(stream.isFormField()).thenReturn(true);
        when(stream.getFieldName()).thenReturn(field);
        when(stream.openStream()).thenReturn(new ByteArrayInputStream(value.getBytes()));
        when(stream.getContentType()).thenReturn("application/json;charset=UTF-8");
        request.initializeMultipart();
        assertEquals(value, request.getParameter(field));
    }

    @Test
    void parseFormFieldEncodingTest() throws Exception {
        prepareMocks();
        final String field = "field";
        final String value = "{'id':5,'libelle':'lib'}";
        when(streamingMultipartResolver.determineEncoding(any())).thenReturn("FAKE");
        final StreamingMultipartHttpServletRequest request = ((StreamingMultipartHttpServletRequest) streamingMultipartResolver.resolveMultipart(new MockHttpServletRequest()));
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(stream);
        when(stream.isFormField()).thenReturn(true);
        when(stream.getFieldName()).thenReturn(field);
        when(stream.openStream()).thenReturn(new ByteArrayInputStream(value.getBytes()));
        when(stream.getContentType()).thenReturn("application/json");
        final Log logger = mock(Log.class);
        LogTestUtils.setLogger(streamingMultipartResolver, "logger", logger);
        request.initializeMultipart();
        assertEquals(value, request.getParameter(field));
        when(logger.isWarnEnabled()).thenReturn(true);
        when(iterator.hasNext()).thenReturn(true, false);
        when(stream.getContentType()).thenReturn(null);
        request.initializeMultipart();
        verify(logger, times(1)).warn(anyString());
    }

    @Test
    void parseUploadFileTest() throws Exception {
        prepareMocks();
        final String field = "field";
        final StreamingMultipartHttpServletRequest request = ((StreamingMultipartHttpServletRequest) streamingMultipartResolver.resolveMultipart(new MockHttpServletRequest()));
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(stream);
        when(stream.getFieldName()).thenReturn(field);
        final Log logger = mock(Log.class);
        LogTestUtils.setLogger(streamingMultipartResolver, "logger", logger);
        when(logger.isDebugEnabled()).thenReturn(true);
        when(logger.isTraceEnabled()).thenReturn(true);
        request.initializeMultipart();
        verify(logger, times(1)).trace(anyString());
    }

    @Test
    void fileItemStreamReadingTest() throws Exception {
        prepareMocks();
        final StreamingMultipartHttpServletRequest request = ((StreamingMultipartHttpServletRequest) streamingMultipartResolver.resolveMultipart(new MockHttpServletRequest()));
        assertDoesNotThrow(() -> request.fileItemStreamReading(null));
        assertDoesNotThrow(() -> request.fileItemStreamReading(stream));
    }

    private void prepareMocks() throws FileUploadException, IOException {
        when(streamingMultipartResolver.prepareFileUpload(nullable(String.class))).thenReturn(fileUpload);
        when(fileUpload.getItemIterator(any(HttpServletRequest.class))).thenReturn(iterator);
        when(streamingMultipartResolver.resolveMultipart(any())).thenCallRealMethod();
    }

    private static class MockStreamingMultipartResolver extends StreamingMultipartResolver {

        @Override
        protected String determineEncoding(HttpServletRequest request) {
            return super.determineEncoding(request);
        }

        @Override
        protected FileUpload prepareFileUpload(String encoding) {
            return super.prepareFileUpload(encoding);
        }

    }

}
