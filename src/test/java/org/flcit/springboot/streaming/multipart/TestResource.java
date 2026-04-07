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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.flcit.springboot.streaming.multipart.resolver.StreamingMultipartResolver.StreamingMultipartHttpServletRequest;

@RestController
@RequestMapping
class TestResource {

    static final Request REQUEST = new Request(5, "test");
    static final String TEST_UPLOAD_URL_PATH = "/test/upload";
    static final String TEST_UPLOAD_FILES_URL_PATH = "/test/upload/files";
    static final String TEST_UPLOAD_FILES_URL_2_PATH = "/test/upload/files2";

    @PostMapping(path = TEST_UPLOAD_URL_PATH, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response testUpload(@RequestPart("request") Request request, @RequestPart("fichier") MultipartFile fichier) {
        final Response response = new Response(request);
        response.addFile(new ResponseFile(fichier.getName(), fichier.getOriginalFilename(), fichier.getContentType()));
        return response;
    }

    @PostMapping(path = TEST_UPLOAD_FILES_URL_PATH, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response testUploadFiles(@RequestPart("request") Request request, StreamingMultipartHttpServletRequest streamingRequest) {
        final Response response = new Response(request);
        streamingRequest.consumeFiles(f -> response.addFile(new ResponseFile(f.getName(), f.getOriginalFilename(), f.getContentType())));
        return response;
    }

    @PostMapping(path = TEST_UPLOAD_FILES_URL_2_PATH, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response testUploadFiles2(@RequestPart("request") Request request, StreamingMultipartHttpServletRequest streamingRequest) {
        final Response response = new Response(request);
        streamingRequest.consumeStreams(f -> response.addFile(new ResponseFile(f.getFieldName(), f.getName(), f.getContentType())));
        return response;
    }

    public static final class Response {
        private Request request;
        private List<ResponseFile> files;
        public Response(Request request) {
            this.request = request;
        }
        public Response(Request request, List<ResponseFile> files) {
            this.request = request;
            this.files = files;
        }
        public Request getRequest() {
            return request;
        }
        public void setRequest(Request request) {
            this.request = request;
        }
        public List<ResponseFile> getFiles() {
            return files;
        }
        public void setFiles(List<ResponseFile> files) {
            this.files = files;
        }
        public void addFile(ResponseFile file) {
            if (this.files == null) {
                this.files = new ArrayList<>(1);
            }
            this.files.add(file);
        }
    }

    public static class ResponseFile {
        private String name;
        private String filename;
        private String contentType;
        public ResponseFile(String name, String filename, String contentType) {
            this.name = name;
            this.filename = filename;
            this.contentType = contentType;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getFilename() {
            return filename;
        }
        public void setFilename(String filename) {
            this.filename = filename;
        }
        public String getContentType() {
            return contentType;
        }
        public void setContentType(String contentType) {
            this.contentType = contentType;
        }
    }

    public static final class Request {
        private Integer id;
        private String libelle;
        public Request() { }
        public Request(Integer id, String libelle) {
            this.id = id;
            this.libelle = libelle;
        }
        public Integer getId() {
            return id;
        }
        public void setId(Integer id) {
            this.id = id;
        }
        public String getLibelle() {
            return libelle;
        }
        public void setLibelle(String libelle) {
            this.libelle = libelle;
        }
        @Override
        public int hashCode() {
            return Objects.hash(id, libelle);
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Request other = (Request) obj;
            return Objects.equals(id, other.id) && Objects.equals(libelle, other.libelle);
        }
    }

}
