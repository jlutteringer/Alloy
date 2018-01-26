package alloy.utilities.web.uri;

import java.util.Optional;

/**
 * Created by jlutteringer on 5/26/16.
 */
public class UrlScheme {
	private String value;

	public UrlScheme(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		UrlScheme that = (UrlScheme) o;

		return value != null ? value.equals(that.value) : that.value == null;
	}

	@Override
	public int hashCode() {
		return value != null ? value.hashCode() : 0;
	}

	public String toString() {
		return UrlEncoder.encode(value);
	}

	public static final UrlScheme HTTP = new UrlScheme("http");
	public static final UrlScheme HTTPS = new UrlScheme("https");

	public enum UrlSchemePortDefaults {
		HTTP(UrlScheme.HTTP, 80), HTTPS(UrlScheme.HTTPS, 443);

		private UrlScheme value;
		private int defaultPort;

		UrlSchemePortDefaults(UrlScheme value, Integer defaultPort) {
			this.value = value;
			this.defaultPort = defaultPort;
		}

		public UrlScheme getValue() {
			return value;
		}

		public int getDefaultPort() { return defaultPort; }

		public static Optional<Integer> defaultPort(UrlScheme urlScheme) {
			for(UrlSchemePortDefaults protocolDefaults : UrlSchemePortDefaults.values()) {
				if(protocolDefaults.getValue().equals(urlScheme)) {
					return Optional.of(protocolDefaults.getDefaultPort());
				}
			}

			return Optional.empty();
		}
	}
}