package alloy.utilities.web.uri;

import java.util.Map;

import org.springframework.http.HttpMethod;

/**
 * Created by jlutteringer on 5/17/16.
 */
// TODO fill out this class
public class UriRequest {
	private final UriContainer url;
	private final HttpMethod method;
	private final Object payload;
	private final Map<String, Object> headers;

	public UriRequest(UriContainer url, HttpMethod method, Object payload, Map<String, Object> headers) {
		this.url = url;
		this.method = method;
		this.payload = payload;
		this.headers = headers;
	}

	public UriContainer getUrl() {
		return url;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public Object getPayload() {
		return payload;
	}

	public Map<String, Object> getHeaders() {
		return headers;
	}
}