package alloy.utilities.web.uri;


import alloy.utilities.core.AlloyAuthentication;

/**
 * Created by jlutteringer on 2/14/17.
 */
public class UriAuthentication extends AlloyAuthentication {
	public UriAuthentication(String principal, String password) {
		super(principal, password);
	}

	public String toString() {
		return UrlEncoder.encode(this.getPrincipal()) + ":" + UrlEncoder.encode(this.getPassword());
	}
}