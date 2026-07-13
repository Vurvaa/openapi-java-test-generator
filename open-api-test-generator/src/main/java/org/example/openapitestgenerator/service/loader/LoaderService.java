package org.example.openapitestgenerator.service.loader;

import org.example.openapitestgenerator.exceptions.OpenApiLoadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LoaderService {
    private static final Logger log = LoggerFactory.getLogger(LoaderService.class);

    private static final List<String> FALLBACK_SPEC_PATHS = List.of(
            "/swagger.json",
            "/openapi.json",
            "/v3/api-docs",
            "/v2/api-docs",
            "/api-docs",
            "/swagger/v1/swagger.json",
            "/swagger/v2/swagger.json"
    );

    private static final List<Pattern> SPEC_URL_PATTERNS = List.of(
            Pattern.compile("url[\"']?\\s*[:=]\\s*[\"']([^\"']+\\.json)[\"']", Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\"']([^\"']*swagger[^\"']*\\.json)[\"']", Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\"']([^\"']*openapi[^\"']*\\.json)[\"']", Pattern.CASE_INSENSITIVE),
            Pattern.compile("url[\"']?\\s*[:=]\\s*[\"']([^\"']+\\.yaml)[\"']", Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\"']([^\"']*swagger[^\"']*\\.yaml)[\"']", Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\"']([^\"']*openapi[^\"']*\\.yaml)[\"']", Pattern.CASE_INSENSITIVE)
    );

    private final RestClient restClient;

    public LoaderService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public String load(String url) {
        if (url == null || url.isBlank()) {
            log.error("OpenAPI spec URL is null or blank");
            throw new OpenApiLoadException("OpenAPI spec URL must not be null or blank");
        }

        try {
            log.info("Loading OpenAPI spec from {}", url);

            ResponseEntity<String> response = get(url);

            if (isHtml(response)) {
                log.warn("URL {} returned HTML instead of OpenAPI spec. Trying to resolve spec URL", url);

                Optional<String> resolvedSpecUrl = resolveSpecUrlFromHtml(url, response.getBody());
                if (resolvedSpecUrl.isEmpty()) {
                    log.error("Could not find OpenAPI spec URL inside Swagger UI page: {}", url);
                    throw new OpenApiLoadException("The URL points to a Swagger UI page. ");
                }

                url = resolvedSpecUrl.get();

                log.info("Found OpenAPI spec at {}", url);
                response = get(url);
            }

            String body = response.getBody();
            if (body == null || body.isBlank()) {
                log.error("Empty response body received from {}", url);
                throw new OpenApiLoadException("Empty response body from " + url);
            }

            log.info("Successfully loaded OpenAPI spec from {}", url);
            return body;

        } catch (RestClientException e) {
            log.error("Failed to fetch OpenAPI spec from {}", url, e);
            throw new OpenApiLoadException("Failed to fetch spec from " + url + ": " + e.getMessage());
        }
    }

    private ResponseEntity<String> get(String url) {
        return restClient
                .get()
                .uri(URI.create(url))
                .retrieve()
                .toEntity(String.class);
    }

    private boolean isHtml(ResponseEntity<String> response) {
        MediaType contentType = response.getHeaders().getContentType();
        if (contentType != null && contentType.isCompatibleWith(MediaType.TEXT_HTML)) {
            return true;
        }

        String body = response.getBody();
        if (body == null) {
            return false;
        }

        String trimmed = body.stripLeading().toLowerCase();

        return trimmed.startsWith("<!doctype") || trimmed.startsWith("<html");
    }

    private Optional<String> resolveSpecUrlFromHtml(String pageUrl, String html) {
        if (html == null || html.isBlank()) {
            log.warn("Swagger UI page {} returned empty HTML body", pageUrl);
            return Optional.empty();
        }

        for (Pattern pattern : SPEC_URL_PATTERNS) {
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                String specUrl = toAbsolute(pageUrl, matcher.group(1));
                log.info("OpenAPI spec URL found in HTML: {}", specUrl);
                return Optional.of(specUrl);
            }
        }

        URI pageUri;
        try {
            pageUri = URI.create(pageUrl);
        } catch (IllegalArgumentException e) {
            log.error("Failed to create URI from page URL: {}", pageUrl, e);
            return Optional.empty();
        }

        String origin = pageUri.getScheme() + "://" + pageUri.getAuthority();

        return FALLBACK_SPEC_PATHS.stream()
                .map(path -> origin + path)
                .filter(this::isSpecEndpoint)
                .findFirst();
    }

    private boolean isSpecEndpoint(String url) {
        try {
            ResponseEntity<String> response = get(url);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Fallback spec endpoint {} returned non-success status: {}",
                        url,
                        response.getStatusCode()
                );
                return false;
            }

            String body = response.getBody();
            if (body == null || body.isBlank()) {
                log.warn("Fallback spec endpoint {} returned empty body", url);
                return false;
            }

            String trimmed = body.stripLeading();

            return trimmed.startsWith("{") || trimmed.startsWith("openapi:")
                    || trimmed.startsWith("swagger:");

        } catch (RestClientException e) {
            log.warn("Failed to check fallback spec endpoint {}", url, e);
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid fallback spec endpoint URL: {}", url, e);
            return false;
        }
    }

    private String toAbsolute(String baseUrl, String specUrl) {
        return URI.create(baseUrl)
                .resolve(specUrl)
                .toString();
    }
}