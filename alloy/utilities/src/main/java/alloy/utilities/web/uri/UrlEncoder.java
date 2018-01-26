package alloy.utilities.web.uri;

import java.net.URLEncoder;

import alloy.utilities.core.Constants;
import alloy.utilities.core._Exceptions;

/**
 * Created by jlutteringer on 2/14/17.
 */
public class UrlEncoder {
	public static String encode(String string) {
		return _Exceptions.propagate(() -> URLEncoder.encode(string, Constants.DEFAULT_ENCODING.name()));
	}
}