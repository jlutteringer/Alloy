package alloy.utilities.domain;

import java.util.Map;

/**
 * Created by jlutteringer on 1/16/18.
 */
public interface Serializer<T> {
	Map<String, Object> serialize(T element);

	T deserialize(Map<String, Object> map);
}