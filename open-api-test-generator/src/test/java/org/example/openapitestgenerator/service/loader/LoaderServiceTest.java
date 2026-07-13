package org.example.openapitestgenerator.service.loader;

import org.example.openapitestgenerator.exceptions.OpenApiLoadException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static org.mockito.Mockito.lenient;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_HTML;

@ExtendWith(MockitoExtension.class)
class LoaderServiceTest {

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private LoaderService loaderService;

    private String currentUrl;

    private final List<String> requestedUrls = new ArrayList<>();

    @BeforeEach
    void setUp() {
        requestedUrls.clear();
        currentUrl = null;

        when(restClientBuilder.build()).thenReturn(restClient);

        loaderService = new LoaderService(restClientBuilder);

        lenient().when(restClient.get()).thenReturn(requestSpec);

        lenient().when(requestSpec.uri(any(URI.class))).thenAnswer(invocation -> {
            URI uri = invocation.getArgument(0);
            currentUrl = uri.toString();
            requestedUrls.add(uri.toString());
            return requestSpec;
        });

        lenient().when(requestSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void loadShouldReturnBodyWhenDirectJsonSpec() {
        String url = "https://api.example.com/openapi.json";
        String spec = """
                {
                  "openapi": "3.0.0",
                  "info": {
                    "title": "Test API",
                    "version": "1.0.0"
                  }
                }
                """;

        stubResponses(Map.of(
                url, ResponseEntity.ok()
                        .contentType(APPLICATION_JSON)
                        .body(spec)
        ));

        String result = loaderService.load(url);

        assertThat(result).isEqualTo(spec);
        assertThat(requestedUrls).containsExactly(url);
    }

    @Test
    void loadShouldReturnBodyWhenDirectYamlSpec() {
        String url = "https://api.example.com/openapi.yaml";
        String spec = """
                openapi: 3.0.0
                info:
                  title: Test API
                  version: 1.0.0
                """;

        stubResponses(Map.of(
                url, ResponseEntity.ok(spec)
        ));

        String result = loaderService.load(url);

        assertThat(result).isEqualTo(spec);
        assertThat(requestedUrls).containsExactly(url);
    }

    @Test
    void loadShouldResolveRelativeJsonSpecUrlFromHtml() {
        String pageUrl = "https://api.example.com/docs/index.html";
        String resolvedSpecUrl = "https://api.example.com/docs/openapi.json";

        String html = """
                <html>
                  <script>
                    SwaggerUIBundle({
                      url: './openapi.json'
                    });
                  </script>
                </html>
                """;

        String spec = """
                {
                  "openapi": "3.0.0"
                }
                """;

        stubResponses(Map.of(
                pageUrl, ResponseEntity.ok()
                        .contentType(TEXT_HTML)
                        .body(html),
                resolvedSpecUrl, ResponseEntity.ok()
                        .contentType(APPLICATION_JSON)
                        .body(spec)
        ));

        String result = loaderService.load(pageUrl);

        assertThat(result).isEqualTo(spec);
        assertThat(requestedUrls).containsExactly(pageUrl, resolvedSpecUrl);
    }

    @Test
    void loadShouldResolveYamlSpecUrlFromHtml() {
        String pageUrl = "https://api.example.com/swagger-ui.html";
        String resolvedSpecUrl = "https://api.example.com/docs/swagger.yaml";

        String html = """
                <html>
                  <script>
                    const config = {
                      url: "/docs/swagger.yaml"
                    };
                  </script>
                </html>
                """;

        String spec = """
                openapi: 3.0.0
                info:
                  title: Test API
                  version: 1.0.0
                """;

        stubResponses(Map.of(
                pageUrl, ResponseEntity.ok()
                        .contentType(TEXT_HTML)
                        .body(html),
                resolvedSpecUrl, ResponseEntity.ok(spec)
        ));

        String result = loaderService.load(pageUrl);

        assertThat(result).isEqualTo(spec);
        assertThat(requestedUrls).containsExactly(pageUrl, resolvedSpecUrl);
    }

    @Test
    void loadShouldUseFallbackPathWhenHtmlDoesNotContainSpecUrl() {
        String pageUrl = "https://api.example.com/swagger-ui/index.html";
        String fallbackSpecUrl = "https://api.example.com/swagger.json";

        String html = """
                <html>
                  <body>Swagger UI page without explicit spec url</body>
                </html>
                """;

        String spec = """
                {
                  "swagger": "2.0"
                }
                """;

        stubResponses(Map.of(
                pageUrl, ResponseEntity.ok()
                        .contentType(TEXT_HTML)
                        .body(html),
                fallbackSpecUrl, ResponseEntity.ok()
                        .contentType(APPLICATION_JSON)
                        .body(spec)
        ));

        String result = loaderService.load(pageUrl);

        assertThat(result).isEqualTo(spec);
    }

    @Test
    void loadShouldThrowOpenApiLoadExceptionWhenUrlIsNull() {
        String expectedMessage = "OpenAPI spec URL must not be null or blank";
        OpenApiLoadException exception = Assertions
                .assertThrows(OpenApiLoadException.class,
                () -> loaderService.load(null));

        Assertions.assertEquals(expectedMessage, exception.getMessage());

        assertThat(requestedUrls).isEmpty();
    }

    @Test
    void loadShouldThrowOpenApiLoadExceptionWhenRestClientFails() {
        String url = "https://api.example.com/openapi.json";

        when(responseSpec.toEntity(String.class))
                .thenThrow(new RestClientException("Connection failed"));

        Assertions.assertThrows(OpenApiLoadException.class,
                () -> loaderService.load(url));
    }

    @Test
    void loadShouldThrowOpenApiLoadExceptionWhenResponseBodyIsBlank() {
        String url = "https://api.example.com/openapi.json";

        stubResponses(Map.of(
                url, ResponseEntity.ok()
                        .contentType(APPLICATION_JSON)
                        .body("   ")
        ));


        Assertions.assertThrows(OpenApiLoadException.class,
                () -> loaderService.load(url));
    }

    @Test
    void load_shouldThrowOpenApiLoadException_whenResolvedSpecBodyIsBlank() {
        String pageUrl = "https://api.example.com/swagger-ui.html";
        String resolvedSpecUrl = "https://api.example.com/docs/openapi.json";

        String html = """
            <html>
              <script>
                SwaggerUIBundle({
                  url: "/docs/openapi.json"
                });
              </script>
            </html>
            """;

        stubResponses(Map.of(
                pageUrl, ResponseEntity.ok()
                        .contentType(TEXT_HTML)
                        .body(html),
                resolvedSpecUrl, ResponseEntity.ok()
                        .contentType(APPLICATION_JSON)
                        .body("")
        ));

        Assertions.assertThrows(OpenApiLoadException.class,
                () -> loaderService.load(pageUrl));
    }


    private void stubResponses(Map<String, ResponseEntity<String>> responses) {
        when(responseSpec.toEntity(String.class)).thenAnswer(invocation -> {
            ResponseEntity<String> response = responses.get(currentUrl);

            if (response == null) {
                throw new AssertionError("Unexpected request URL: " + currentUrl);
            }

            return response;
        });
    }
}