package connect.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

public class ElasticsearchApi {

    private static final String LOCALHOST = "127.0.0.1";
    private static final int PORT = 9200;
    private static final String TYPE = "taiga";
    private static final java.util.logging.Logger logger = Logger.getLogger(ElasticsearchApi.class.getName());

    /*
    public static SearchResponse getTaskReference(String topic, int reference) throws IOException {

        final Logger logger = LoggerFactory.getLogger(ElasticsearchApi.class);

        logger.info("Initiating Elasticsearch connection");

        TransportClient client = new PreBuiltTransportClient(Settings.builder().put("cluster.name", "elasticsearch").build())
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));

        SearchResponse response = client.prepareSearch(topic)
                .setTypes(TYPE)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.termQuery("reference", Integer.toString(reference)))
                .get();

        client.close();

        logger.info("Finished Elasticsearch connection");

        return response;
    }
*/
    public static SearchResponse getTaskReference(String topic, int reference) throws IOException {

        //System.out.println("Initiating Elasticsearch connection");
        //System.out.println("port " + PORT);
        //System.out.println("topic " + topic);
        //System.out.println("ref " + reference);

        RestClient lowLevelClient = RestClient.builder(new HttpHost("localhost", PORT))
                .build();
        RestHighLevelClient client = new RestHighLevelClient(lowLevelClient);
        SearchResponse response = client.search(new SearchRequest(topic)
                .source( new SearchSourceBuilder()
                        .query(QueryBuilders.matchQuery("reference", Integer.toString(reference)))
                )
        );
        lowLevelClient.close();
        //System.out.println("Finished Elasticsearch connection");
        return response;
    }

    public static void main(String[] args) throws IOException {

        SearchResponse a = getTaskReference("taiga_pes_x12b.tasks", 54);

        //System.out.println(a.getProfileResults().get("reference"));

        SearchHits searchHits = a.getHits();
        for (SearchHit hit : searchHits) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            System.out.println(sourceAsMap.get("id"));
            System.out.println(sourceAsMap.get("reference"));
        }

    }

}