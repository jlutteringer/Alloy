package alloy.utilities.web.uri;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import alloy.utilities.core._Maps;
import alloy.utilities.core._Strings;
import alloy.utilities.domain.Either;
import alloy.utilities.domain.Tuple.Pair;

/**
 * Created by jlutteringer on 5/26/16.
 */
public class UriParser {
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	public static UriContainer parseOrThrow(String uriString) {
		Optional<UriContainer> uriContainer = parseOrEmpty(uriString);
		if (uriContainer.isPresent()) {
			return uriContainer.get();
		}
		return null;
	}

	public static Optional<UriContainer> parseOrEmpty(String uriString) {
		return parse(uriString).getLeft();
	}

	// TODO does this need to be so defensive? log errors?
	public static Either<UriContainer, Exception> parse(String uriString) {
		try {
			if(StringUtils.isBlank(uriString)) {
				uriString = "/";
			}
			UriComponents components = UriComponentsBuilder.fromUriString(uriString).build();

			UriAuthentication authentication = null;
			Optional<Pair<String, String>> rawAuthentication = _Strings.splitPair(components.getUserInfo(), ":");
			if(rawAuthentication.isPresent()) {
				authentication = new UriAuthentication(rawAuthentication.get().getFirst(), rawAuthentication.get().getSecond());
			}

			UriAuthority authority = null;
			if(components.getHost() != null) {
				authority = new UriAuthority(authentication, components.getHost(), components.getPort());
			}

			UrlScheme scheme = null;
			if(components.getScheme() != null) {
				scheme = new UrlScheme(components.getScheme());
			}

			UriLocation location = new UriLocation(components.getPathSegments(), _Maps.multimap(components.getQueryParams()), components.getFragment());
			return Either.left(new UriContainer(scheme, authority, location));
		}
		catch (Exception e) {
			return Either.right(e);
		}
	}
}