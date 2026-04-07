# springboot-streaming-multipart

## Présentation
Le projet *springboot-streaming-multipart* est la librairie pour passer le multipart en streaming dans une API Spring Boot.

## Frameworks
- [Spring boot](https://spring.io/projects/spring-boot) [@3.5.14](https://docs.spring.io/spring-boot/3.5/index.html)

## Dépendances
- [Apache commons fileUpload](https://commons.apache.org/proper/commons-fileupload/commons-fileupload2-jakarta-servlet5/index.html)

## Packages
**org.flcit.springboot.streaming.resolver.StreamingMultipartResolver** => classe qui traite les requests multipart en streaming

## Exemples d'implémentation

Avec un objet de données JSON en début de flux et un nombre déterminé de fichiers consommé sous forme de [MultipartFile](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/multipart/MultipartFile.html) :
```java
@PostMapping(path = TEST_UPLOAD_URL_PATH, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public Response testUpload(@RequestPart Request request, @RequestPart MultipartFile fichier) {
    final Response response = new Response(request);
    response.addFile(new ResponseFile(fichier.getName(), fichier.getOriginalFilename(), fichier.getContentType()));
    return response;
}
```

Avec un objet optionnel de données JSON en début de flux et un nombre déterminé de fichiers consommé sous forme de [MultipartFile](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/multipart/MultipartFile.html) et un fichier optionnel :
```java
@PostMapping(path = TEST_UPLOAD_URL_PATH, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public Response testUpload(@RequestPart(required = false) Request request, @RequestPart MultipartFile fichier1, @RequestPart(required = false) MultipartFile fichier2) {
    final Response response = new Response(request);
    response.addFile(new ResponseFile(fichier1.getName(), fichier1.getOriginalFilename(), fichier1.getContentType()));
    if (fichier2 != null) {
        response.addFile(new ResponseFile(fichier2.getName(), fichier2.getOriginalFilename(), fichier2.getContentType()));
    }
    return response;
}
```

Avec un objet de données JSON en début de flux et un nombre illimité de fichiers consommé sous forme de [MultipartFile](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/multipart/MultipartFile.html) :
```java
@PostMapping(path = TEST_UPLOAD_FILES_URL_PATH, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public Response testUploadUnlimitedFilesMultipart(@RequestPart Request request, StreamingMultipartHttpServletRequest streamingRequest) {
    final Response response = new Response(request);
    streamingRequest.consumeFiles(f -> response.addFile(new ResponseFile(f.getName(), f.getOriginalFilename(), f.getContentType())));
    return response;
}
```

Avec un objet de données JSON en début de flux et un nombre illimité de fichiers consommé sous forme de [FileItemInput](https://javadoc.io/doc/org.apache.commons/commons-fileupload2-core/latest/org/apache/commons/fileupload2/core/FileItemInput.html) :
```java
@PostMapping(path = TEST_UPLOAD_FILES_URL_2_PATH, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public Response testUploadUnlimitedFilesStream(@RequestPart Request request, StreamingMultipartHttpServletRequest streamingRequest) {
    final Response response = new Response(request);
    streamingRequest.consumeStreams(f -> response.addFile(new ResponseFile(f.getFieldName(), f.getName(), f.getContentType())));
    return response;
}
```

Avec un objet de données JSON en début de flux et un nombre déterminé de fichiers consommé sous forme de [MultipartFile](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/multipart/MultipartFile.html) et un nombre illimité optionnel de fichiers :
```java
@PostMapping(path = TEST_UPLOAD_URL_PATH, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public Response testUpload(@RequestPart Request request, @RequestPart MultipartFile fichier1, @RequestPart MultipartFile fichier2, StreamingMultipartHttpServletRequest streamingRequest) {
    final Response response = new Response(request);
    response.addFile(new ResponseFile(fichier1.getName(), fichier1.getOriginalFilename(), fichier1.getContentType()));
    response.addFile(new ResponseFile(fichier2.getName(), fichier2.getOriginalFilename(), fichier2.getContentType()));
    streamingRequest.consumeFiles(f -> response.addFile(new ResponseFile(f.getName(), f.getOriginalFilename(), f.getContentType())));
    return response;
}
```

### Informations importantes
Les objets doivent être envoyée en début de flux avant les fichiers.  
Ils sont décodés immédiatement pour les injecter dans les arguments (= paramètres) de la fonction du Controller.

## Configuration
Utilisation des properties Multipart de Spring Boot :
[Spring boot Multipart Configuration](https://docs.spring.io/spring-boot/3.5/appendix/application-properties/index.html#application-properties.web.spring.servlet.multipart.enabled)

### Limitation des tailles
Pour désactiver les tailles limites par défaut :

```properties
spring.servlet.multipart.max-file-size=-1
spring.servlet.multipart.max-request-size=-1
```

Pour voir les unités de valeurs possibles :
[DataSize](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/util/unit/DataSize.html)

### Désactivation
Pour désactiver l'utilisation du streaming et utiliser le mode par défaut de Spring :
```properties
spring.servlet.streaming.multipart.enabled=false
```

Par défaut l'utilisation du streaming est activé.

## Projets dépendants
- [springboot-bench-webserver](https://github.com/flc-it/springboot-bench-webserver)