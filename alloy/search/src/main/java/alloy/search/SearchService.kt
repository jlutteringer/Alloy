package alloy.search

import alloy.utilities.domain.Momento
import alloy.utilities.domain.Momento.Momentizer
import alloy.utilities.domain.Serializer
import java.util.stream.Stream

/**
 * Created by jlutteringer on 1/17/18.
 */
interface SearchService {
    fun <T> search(query: SearchQuery): Stream<T>

    fun <T> save(documents: Collection<T>, serializer: Serializer<T>, momentizer: Momentizer<T, String>)
}

interface IndexService {

}

data class SearchQuery(val name: String)