package com.nnoco.tool.downloader.itebook;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class Downloader {
	private final OkHttpClient client = new OkHttpClient();
	
	// 1. 웹 페이지에서 링크를 찾는다.
	
	// 2. 요청 후에 302 응답을 받는다.
	
	// 3. 응답으로 온 Location으로 요청하여 파일을 받는당
	
	public void downloadAll() {
		for(int i = 1 ; i < 4900 ; i++) {
			download(i);
		}
	}
	
	private String getFileUrl(int i) throws IOException {
		Document document = Jsoup.connect("http://it-ebooks.info/book/" + i).get();
		
		Elements anchors = document.select("a");
		for(Element anchor : anchors) {
			if (anchor.attr("href").startsWith("http://filepi.com")) {
				return anchor.attr("href");
			}
		}
		
		return null;
	}
	
	private void download(int id) {
		try {
			String url = getFileUrl(id);
			if (null == url) return;
			
			download(url, id);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void download(final String url, final int id) throws IOException {
		System.out.println("start download from : " + url + " , " + id);
		Request request = new Request.Builder()
			.url(url)
			.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
			.header("Accept-Encoding", "gzip, deflate, sdch")
			.header("Accept-Language", "ko-KR,ko;q=0.8,en-US;q=0.6,en;q=0.4")
			.header("Connection", "keep-alive")
			.header("Cookie", "PHPSESSID=kkbjqcrvn9iq9vkp68na3flh26")
			.header("Host", "cdn3.filepi.com")
			.header("Referer", "http://it-ebooks.info/book/" + id)
			.header("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.89 Safari/537.36")
			.build();
		
		client.newCall(request).enqueue(new Callback() {
			
			public void onResponse(Response res) throws IOException {
				String contentDisposition = res.header("Content-Disposition");
				String fileName = contentDisposition.substring(contentDisposition.indexOf('"') + 1, contentDisposition.lastIndexOf('"'));
				
				System.out.println("write to " + fileName + " from " + url + " , " + id  + " size: " + res.body().contentLength());
				boolean result = write(String.format("%05d_",  id) + fileName, (int)res.body().contentLength(), res.body().byteStream());
				if(!result) {
					System.out.println("! Doesn't match " + fileName + " id: " + id);
				}
			}
			
			public void onFailure(Request req, IOException e) {
				System.out.println("failed to download from : " + url + " , " + id);
				
			}
		});
//		System.out.println(response.code());
//		System.out.println(response.body().contentLength());
//		System.out.println(response.body().string());
//		
//		Headers headers = response.headers();
//		for(String name : headers.names()) {
//			System.out.println(name + " : " + headers.get(name));
//		}
	}
	
	private boolean write(String fileName, int contentLength,
			InputStream byteStream) throws IOException {
		BufferedInputStream in = new BufferedInputStream(byteStream);
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("downloads\\" + fileName));
		
		byte[] buffer = new byte[512];
		int sum = 0;
		
		while(true) {
			int length = in.read(buffer, 0, buffer.length);
			if (length == -1) break;
			sum += length;
			out.write(buffer, 0, length);
		}
		
		System.out.println("Completed to write to " + fileName + " contentLength:" + contentLength + " effective:" + sum);

		out.flush();
		out.close();
		in.close();
		
		return contentLength == sum;
	}

	public static void main(String[] args){
		new Downloader().downloadAll();
	}
	
	
}
