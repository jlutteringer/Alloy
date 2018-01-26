package alloy.elasticsearch

import alloy.search.SearchQuery
import alloy.search.SearchService
import java.lang.RuntimeException
import java.util.stream.Stream

/**
 * Created by jlutteringer on 1/17/18.
 */
class ElasticSearchService: SearchService {
    override fun <T> search(query: SearchQuery): Stream<T> {
        throw RuntimeException()
    }

}