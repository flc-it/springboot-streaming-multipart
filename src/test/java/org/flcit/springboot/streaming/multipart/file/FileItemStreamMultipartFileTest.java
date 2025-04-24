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

package org.flcit.springboot.streaming.multipart.file;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.fileupload.FileItemStream;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.util.FileCopyUtils;

import org.flcit.commons.core.util.StringUtils;
import org.flcit.springboot.commons.test.MockitoBaseTest;
import org.flcit.springboot.streaming.multipart.resolver.StreamingMultipartResolver.StreamingMultipartHttpServletRequest;

class FileItemStreamMultipartFileTest implements MockitoBaseTest {

    private static final String FILE_NAME = "test.json";

    @Mock
    private FileItemStream fileItemStream;

    @Mock
    private StreamingMultipartHttpServletRequest streamingMultipartHttpServletRequest;

    @Test
    void tests() {
        final FileItemStreamMultipartFile file = new FileItemStreamMultipartFile(fileItemStream, streamingMultipartHttpServletRequest);
        assertFalse(file.isEmpty());
        assertEquals(-1, file.getSize());
        assertThrows(UnsupportedOperationException.class, () -> file.getBytes());
        when(fileItemStream.getFieldName()).thenReturn("file");
        assertEquals("file", file.getName());
        when(fileItemStream.getContentType()).thenReturn("text/csv");
        assertEquals("text/csv", file.getContentType());
    }

    @Test
    void originalFilenameTest() {
        final FileItemStreamMultipartFile file = new FileItemStreamMultipartFile(fileItemStream, streamingMultipartHttpServletRequest);
        assertEquals(StringUtils.EMPTY, file.getOriginalFilename());
        when(fileItemStream.getName()).thenReturn(FILE_NAME);
        assertEquals(FILE_NAME, file.getOriginalFilename());
        when(fileItemStream.getName()).thenReturn("C:\\" + FILE_NAME);
        assertEquals(FILE_NAME, file.getOriginalFilename());
        when(fileItemStream.getName()).thenReturn("/usr/" + FILE_NAME);
        assertEquals(FILE_NAME, file.getOriginalFilename());
        file.setPreserveFilename(true);
        assertEquals("/usr/" + FILE_NAME, file.getOriginalFilename());
    }

    @Test
    void transferTest() {
        final FileItemStreamMultipartFile file = new FileItemStreamMultipartFile(fileItemStream, streamingMultipartHttpServletRequest);
        try (MockedStatic<Files> mock = mockStatic(Files.class)) {
            try (MockedStatic<FileCopyUtils> m2 = mockStatic(FileCopyUtils.class)) {
                assertDoesNotThrow(() -> file.transferTo(Paths.get("test").toFile()));
            }
        }
    }

}
