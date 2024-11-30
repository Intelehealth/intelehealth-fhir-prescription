package org.ih.prescription.data.exchange.service.utils;

import java.io.UnsupportedEncodingException;

import org.ih.prescription.data.exchange.domain.FhirResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;

public class HttpWebClient {

	static ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
			.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10000000)).build();

	public static String get(String baseURL, String APIURL, String username, String password)
			throws UnsupportedEncodingException {
		System.err.println(baseURL + "/" + APIURL);
		WebClient webClient = WebClient.builder().baseUrl(baseURL)
				.defaultHeaders(httpHeaders -> httpHeaders.setBasicAuth(username, password))
				.exchangeStrategies(exchangeStrategies).build();
		return webClient.get().uri(APIURL)

				.headers(httpHeaders -> httpHeaders.setBasicAuth(username, password)).retrieve()
				.bodyToMono(String.class).block();
	}

	public static String post(String baseURL, String APIURL, String username, String password, String paylaod)
			throws UnsupportedEncodingException {

		WebClient webClient = WebClient.builder().baseUrl(baseURL)
				// .defaultHeaders(httpHeaders -> httpHeaders.setBasicAuth(username,
				// password))
				.exchangeStrategies(exchangeStrategies).build();

		try {
			return webClient.post().uri(APIURL).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.body(Mono.just(paylaod), String.class).retrieve().bodyToMono(String.class).block();
		} catch (WebClientResponseException e) {
			System.err.println(e);
			System.err.println(e.getResponseBodyAsString());
			throw e;

		}

	}

	public static FhirResponse post(String baseURL, String APIURL, String paylaod) throws UnsupportedEncodingException {
		WebClient webClient = WebClient.builder().baseUrl(baseURL)
//				.defaultHeaders(httpHeaders -> httpHeaders.setBasicAuth(username, password))
				.exchangeStrategies(exchangeStrategies).build();
		FhirResponse response = new FhirResponse();
		try {
			String result = webClient.post().uri(APIURL)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.body(Mono.just(paylaod), String.class).retrieve().bodyToMono(String.class).block();

			response.setResponse(result);
			response.setStatusCode("200");
			response.setMessage(null);
		} catch (WebClientResponseException e) {

			System.err.println(e);
			System.err.println(e.getStatusCode());
			System.err.println(e.getResponseBodyAsString());

			response.setMessage(e.getMessage());
			response.setStatusCode(e.getStatusCode().toString());
			response.setResponse(e.getResponseBodyAsString());

		}
		return response;

	}

	public static FhirResponse put(String baseURL, String APIURL, String paylaod) {

		WebClient webClient = WebClient.builder().baseUrl(baseURL)
				// .defaultHeaders(httpHeaders -> httpHeaders.add("content-type",
				// "application/fhir+json;charset=utf-8 "))
				.exchangeStrategies(exchangeStrategies).build();

		FhirResponse response = new FhirResponse();

		try {
			String result = webClient.put().uri(APIURL)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.body(Mono.just(paylaod), String.class).retrieve().bodyToMono(String.class).block();

			response.setResponse(result);
			response.setStatusCode("200");
			response.setMessage(null);
		} catch (WebClientResponseException e) {

			System.err.println(e);
			System.err.println(e.getStatusCode());
			System.err.println(e.getResponseBodyAsString());

			response.setMessage(e.getMessage());
			response.setStatusCode(e.getStatusCode().toString());
			response.setResponse(e.getResponseBodyAsString());

		} catch (Exception e) {
			System.err.println(e);
			response.setMessage(e.getMessage());
			response.setResponse(e.getMessage());
			response.setStatusCode(HttpStatus.EXPECTATION_FAILED.toString());
		}
		return response;

	}

	public static FhirResponse postWithBasicAuth(String baseURL, String APIURL, String username, String password,
			String paylaod) {
		System.err.println(baseURL + "" + APIURL + "-" + username + "-" + password + "-" + paylaod);
		WebClient webClient = WebClient.builder().baseUrl(baseURL)
				.defaultHeaders(httpHeaders -> httpHeaders.setBasicAuth(username, password))
				.exchangeStrategies(exchangeStrategies).build();

		FhirResponse response = new FhirResponse();

		try {
			String result = webClient.post().uri(APIURL)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.body(Mono.just(paylaod), String.class).retrieve().bodyToMono(String.class).block();

			response.setResponse(result);
			response.setStatusCode("200");
			response.setMessage(null);

		} catch (WebClientResponseException e) {

			System.err.println(e);
			System.err.println(e.getStatusCode());
			System.err.println(e.getResponseBodyAsString());

			response.setMessage(e.getMessage());
			response.setStatusCode(e.getStatusCode().toString());
			response.setResponse(e.getResponseBodyAsString());

		} catch (Exception e) {
			System.err.println(e);
			response.setMessage(e.getMessage());
			response.setResponse(e.getMessage());
			response.setStatusCode(HttpStatus.EXPECTATION_FAILED.toString());
		}
		return response;

	}

}
