package connect.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;


public class ElasticsearchApi {

    public static void main(String[] args) throws IOException {

        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http"),
                new HttpHost("localhost", 9300, "http")).build();

        RestHighLevelClient client =
                new RestHighLevelClient(restClient);

        GetRequest getRequest = new GetRequest(
                "github_asw_g11d.commits",
                "github",
                "b50bf9cdcd57513e2b364e2ac15a4365d10ed3a4");

        GetResponse getResponse = client.get(getRequest);

        System.out.println(getResponse);

        restClient.close();

    }

}