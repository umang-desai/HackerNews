import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.*;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class HackerNewsStories {

	/**
	 * https://hacker-news.firebaseio.com/v0/topstories.json
	 * https://hacker-news.firebaseio.com/v0/newstories.json
	 * https://hacker-news.firebaseio.com/v0/beststories.json
	 */

	private String base_url = "https://hacker-news.firebaseio.com/v0/";
	private String file_json = ".json";
	private String item = "item";
	private final String top = "topstories" + file_json;
	private final String best = "beststories" + file_json;
	private final String news = "newstories" + file_json;
	
	public HackerNewsStories() throws IOException{
	}

	private String executeQuery(String query, int limit) throws IOException {
		URL url = new URL(base_url + query);
		print(url.toString());
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");

		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}
		//Get IDs of all top stories.
		String output = getResult(conn);
		print(output);
		//Count by votes.
		
		String yourJson = output;
		Type listType = new TypeToken<ArrayList<Integer>>() {}.getType();
		ArrayList<Integer> list = new Gson().fromJson(yourJson, listType);		
		
		Map<Integer, Integer> scoreList = new HashMap<Integer, Integer>();
		Map<Integer, Article> articles = new HashMap<Integer, Article>();
		List<Article> finalArticles = new ArrayList<Article>();

		//Fetch details of all the articles in top stories. 
		for(Integer i : list){
			Article article = getArticle(i);
			articles.put(i, article);
			scoreList.put(i, article.getScore());
		}
		
		//Sort articles by score. 
		Map<Integer, Integer> sortedMap = scoreList.entrySet().stream()
                .sorted(Collections.reverseOrder(Entry.comparingByValue())).limit(limit)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
                         (e1,e2) -> e1, LinkedHashMap::new));
		
		Set<Integer> ids = sortedMap.keySet();
		for(Integer i : ids){
			Article article = articles.get(i);
			finalArticles.add(article);
		}
		conn.disconnect();
		listType = new TypeToken<List<Article>>() {}.getType();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(finalArticles, listType);
	}
	
	private Article getArticle(int id) throws IOException{
		URL art_url = new URL(base_url + item + "/" + id +".json");
		HttpURLConnection conn = (HttpURLConnection) art_url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");

		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}
		String output = getResult(conn);
		conn.disconnect();
		return new Gson().fromJson(output, Article.class);
	}

	private String getResult(HttpURLConnection conn) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		String l = null;
		String l_all = "";
		while ((l = br.readLine()) != null) {
			l_all += "\n" + l;
		}
		br.close();
		return l_all;
	}
	
	private String getQueryType(String query){
		if(query.equals("top")){
			return top;
		}
		
		return null;
	}
	
	public static void main(String[] args) throws IOException{
		HackerNewsStories news = new HackerNewsStories();
		String query = "top";
		//TODO implement functionality to accept limit on results returned. Default is 10.
		int limit = 20;
		if(args.length==2){
			limit = Integer.parseInt(args[1]);
		}
		String output = news.executeQuery(news.getQueryType(query), limit);
		print(output);
	}
	
	private static void print(String str){
		System.out.println(str);
	}
}
