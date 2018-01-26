package alloy.utilities.web.uri;

import java.util.Optional;

/**
 * Created by jlutteringer on 2/14/17.
 */
public class UriContainer {
	private UrlScheme scheme;
	private UriAuthority authority;
	private UriLocation location;

	public UriContainer(UriAuthority authority) {
		this(null, authority, UriLocation.EMPTY_LOCATION);
	}

	public UriContainer(UrlScheme scheme, UriAuthority authority) {
		this(scheme, authority, UriLocation.EMPTY_LOCATION);
	}

	public UriContainer(UriAuthority authority, UriLocation location) {
		this(null, authority, location);
	}

	public UriContainer(UrlScheme scheme, UriAuthority authority, UriLocation location) {
		this.scheme = scheme;
		this.authority = authority;
		this.location = location;
	}

	public Optional<UrlScheme> getScheme() {
		return Optional.ofNullable(scheme);
	}

	public Optional<UriAuthority> getAuthority() {
		return Optional.ofNullable(authority);
	}

	public UriLocation getLocation() {
		return location;
	}

	public String toString() {
		String url = "";
		Optional<UrlScheme> scheme = this.getScheme();
		if(scheme.isPresent()) {
			url = url + scheme.get() + ':';
		}

		Optional<UriAuthority> authority = this.getAuthority();
		if(authority.isPresent()) {
			if(scheme.isPresent()) {
				url = url + "//";
			}

			url = url + authority.get();
		}

		url = url + this.getLocation();
		return url;
	}

	public UriContainer addRelativeLocation(UriLocation location) {
		return new UriContainer(scheme, authority, this.getLocation().addRelativeLocation(location));
	}
}