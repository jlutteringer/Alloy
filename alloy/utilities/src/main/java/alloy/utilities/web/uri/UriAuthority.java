package alloy.utilities.web.uri;

import java.util.Optional;

/**
 * Created by jlutteringer on 5/26/16.
 */
public class UriAuthority {
	private final UriAuthentication authentication;
	private final String host;
	private final Integer port;

	public UriAuthority(String host) {
		this(null, host, null);
	}

	public UriAuthority(UriAuthentication authentication, String host) {
		this(authentication, host, null);
	}

	public UriAuthority(String host, Integer port) {
		this(null, host, port);
	}

	public UriAuthority(UriAuthentication authentication, String host, Integer port) {
		this.authentication = authentication;
		this.host = host;
		this.port = port;
	}

	public Optional<UriAuthentication> getAuthentication() {
		return Optional.ofNullable(authentication);
	}

	public String getHost() {
		return host;
	}

	public Optional<Integer> getPort() {
		return Optional.ofNullable(port);
	}

	public String toString() {
		String url = "";

		Optional<UriAuthentication> authentication = this.getAuthentication();
		if(authentication.isPresent()) {
			url = url + authentication.get() + "@";
		}

		url = url + UrlEncoder.encode(host);

		Optional<Integer> port = this.getPort();
		if(port.isPresent()) {
			url = url + ":" + port.get();
		}

		return url;
	}
}