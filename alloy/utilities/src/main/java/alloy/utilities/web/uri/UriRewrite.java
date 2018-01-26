package alloy.utilities.web.uri;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import alloy.utilities.core._Lists;
import alloy.utilities.core._Optionals;

/**
 * Created by jlutteringer on 2/14/17.
 */
public class UriRewrite {
	@FunctionalInterface
	public interface UriRewriter {
		Optional<UriContainer> rewriteUri(UriContainer uri);

		default Optional<UriContainer> rewriteUri(String uriString) {
			return UriParser.parse(uriString).getLeft().flatMap(this::rewriteUri);
		}

		default String rewriteUriString(String uriString) {
			return rewriteUri(uriString).map(Object::toString).orElse(uriString);
		}
	}

	public static class CompositeUriRewriter implements UriRewriter {
		private List<UriRewriter> rewriters;

		public CompositeUriRewriter(List<UriRewriter> rewriters) {
			this.rewriters = rewriters;
		}

		public CompositeUriRewriter(UriRewriter... rewriters) {
			this.rewriters = _Lists.list(rewriters);
		}

		@Override
		public Optional<UriContainer> rewriteUri(UriContainer uri) {
			return _Optionals.firstSome(rewriters.stream().map(rewriter -> rewriter.rewriteUri(uri)));
		}
	}

	public static class MapSourceUriRewriter implements UriRewriter {
		private Supplier<Map<String, UriContainer>> mapSource;

		public MapSourceUriRewriter(Supplier<Map<String, UriContainer>> mapSource) {
			this.mapSource = mapSource;
		}

		@Override
		public Optional<UriContainer> rewriteUri(UriContainer uri) {
			return Optional.ofNullable(mapSource.get().get(uri.toString()));
		}
	}

	public static class KeyedUrlRewriter implements UriRewriter {
		private String key;
		private Supplier<Optional<UriContainer>> supplier;

		public KeyedUrlRewriter(String key, Supplier<Optional<UriContainer>> supplier) {
			this.key = key;
			this.supplier = supplier;
		}

		@Override
		public Optional<UriContainer> rewriteUri(UriContainer uri) {
			if(uri.toString().equals(key)) {
				return supplier.get();
			}
			return Optional.empty();
		}
	}
}