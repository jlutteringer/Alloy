package alloy.utilities.web.uri;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Multimap;

import alloy.utilities.core._Lists;
import alloy.utilities.core._Maps;

/**
 * Created by jlutteringer on 4/28/16.
 */
public class UriLocation {
	public static final UriLocation EMPTY_LOCATION = new UriLocation();

	private final List<String> path;
	private final Multimap<String, String> query;
	private String fragment;

	public UriLocation() {
		this(_Lists.list(), _Maps.multimap(), null);
	}

	public UriLocation(List<String> path, Multimap<String, String> query, String fragment) {
		this.path = _Lists.list(path);
		this.query = _Maps.multimap(query);
		this.fragment = fragment;
	}

	public List<String> getPath() {
		return path;
	}

	public UriLocation addPath(String... path) {
		return addPath(_Lists.list(path));
	}

	public UriLocation addPath(List<String> path) {
		return new UriLocation(_Lists.concat(this.path, path), this.query, this.fragment);
	}

	public Multimap<String, String> getQuery() {
		return query;
	}

	public Optional<String> getFragment() {
		return Optional.ofNullable(fragment);
	}

	public UriLocation addRelativeLocation(UriLocation location) {
		String fragment = location.getFragment().orElse(this.getFragment().orElse(null));
		return new UriLocation(_Lists.concat(this.path, location.path), _Maps.combine(this.query, location.query), fragment);
	}

	public String toString() {
		String url = "";

		if(path.isEmpty()) {
			url = "/";
		}
		else {
			for (String pathString : path) {
				url = url + '/' + UrlEncoder.encode(pathString);
			}
		}

		if (!query.isEmpty()) {
			url = url + '?';

			for (Entry<String, String> parameter : query.entries()) {
				url = url + UrlEncoder.encode(parameter.getKey()) + "=" + UrlEncoder.encode(parameter.getValue()) + "&";
			}
			url = StringUtils.removeEnd(url, "&");
		}

		Optional<String> fragment = this.getFragment();
		if(fragment.isPresent()) {
			url = url + '#' + fragment.get();
		}

		return url;
	}

	public static UriLocation from(String uri) {
		return UriParser.parseOrThrow(uri).getLocation();
	}
}