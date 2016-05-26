package io.github.nasso.elektrode;

import io.github.nasso.elektrode.data.IOStreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Application;

public class Updater {
	private static String repository = "https://github.com/Nasso/Elektrode";
	private static URL repoVersionFileURL;
	private static Path versionFile = Paths.get("version.info");
	
	static {
		try {
			setRepoVersionFileURL(new URL("https://raw.githubusercontent.com/Nasso/Elektrode/master/Release/version.info"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isUpdateAvailable() throws IOException{
		if(!Files.exists(versionFile)){
			Files.createFile(versionFile);
			return true;
		}
		
		InputStream in = Files.newInputStream(versionFile);
		String currVersion = IOStreamUtils.decodeUTF8(in);
		in.close();
		
		in = repoVersionFileURL.openStream();
		String lastVersion = IOStreamUtils.decodeUTF8(in);
		in.close();
		
		System.out.println("Current version: '"+currVersion+"'");
		System.out.println("Last version: '"+lastVersion+"'");
		
		return !currVersion.equals(lastVersion);
	}
	
	public static void updateIfAvailable(Application app) throws IOException{
		if(isUpdateAvailable()){
			forceUpdate(app);
		}
	}
	
	public static void forceUpdate(Application app) throws IOException{
		app.getHostServices().showDocument(repository);
	}

	public static URL getRepoVersionFileURL() {
		return repoVersionFileURL;
	}

	public static void setRepoVersionFileURL(URL repoVersionFileURL) {
		Updater.repoVersionFileURL = repoVersionFileURL;
	}

	public static Path getVersionFile() {
		return versionFile;
	}

	public static void setVersionFile(Path versionFile) {
		Updater.versionFile = versionFile;
	}
}
