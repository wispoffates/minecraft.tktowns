package io.github.wispoffates.minecraft.tktowns.datastore;

import io.github.wispoffates.minecraft.tktowns.Town;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class YamlStore implements DataStore {
	
	final static String CONFIG_FILE = "tktowns.json";
	
	protected transient String configDir;
	protected transient Gson gson;
	protected transient GeneralConfig gconf;
	
	
	public YamlStore(File configDir) {
		this.configDir = configDir.toString();
		gson = new GsonBuilder().setPrettyPrinting().create();
		File generalConfigFile = new File(this.configDir,CONFIG_FILE);
		if(!generalConfigFile.exists()) {
			//Config file doesn't exist probably first launch... lets right something out
			this.gconf = new GeneralConfig();
			jsonToFile(this.gconf,CONFIG_FILE);
		} else {
			this.gconf = jsonFromFile(CONFIG_FILE,GeneralConfig.class);
		}
		
	}
	
	private void jsonToFile(Object config, String relativePath) {
		FileOutputStream outputStream = null;
		try { 
			  String output = gson.toJson(config);
			  outputStream = new FileOutputStream(new File(this.configDir,relativePath));
			  outputStream.write(output.getBytes());
			} catch (Exception e) {
			  e.printStackTrace();
			} finally {
				if(outputStream != null)
					try {
						outputStream.close();
					} catch (IOException e) {
						//Don't care
					}
			}
	}
	
	private <T extends Object> T jsonFromFile(String relativePath, Class<T> type) {
		FileInputStream inputStream = null;
		BufferedReader bufferedReader = null;
		T ret = null;
		try {
			inputStream = new FileInputStream(new File(this.configDir,relativePath));
			InputStreamReader isr = new InputStreamReader(inputStream);
			bufferedReader = new BufferedReader(isr);
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
			}
			String json = sb.toString();
			ret = gson.fromJson(json, type);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bufferedReader != null)
					bufferedReader.close();
				if (inputStream != null)
					inputStream.close();
			} catch (IOException e) {
				// Don't care
			}
		}
		return ret;
	}
	
	@Override
	public int townCost() {
		return gconf.townCost;
	}

	@Override
	public void townCost(int cost) {
		gconf.townCost = cost;
		jsonToFile(this.gconf,CONFIG_FILE);
	}

	@Override
	public int townUpkeepInterval() {
		return gconf.townUpkeepInterval;
	}

	@Override
	public void townUpkeepInterval(int interval) {
		gconf.townUpkeepInterval = interval;
		jsonToFile(this.gconf,CONFIG_FILE);
	}

	@Override
	public TimeUnit townUpkeepUnit() {
		return gconf.townUpkeepUnit;
	}

	@Override
	public void townUpkeepUnit(TimeUnit unit) {
		gconf.townUpkeepUnit = unit;
		jsonToFile(this.gconf,CONFIG_FILE);
	}

	@Override
	public int townUpkeepCost() {
		return gconf.townUpkeepCost;
	}

	@Override
	public void townUpkeepCost(int cost) {
		gconf.townUpkeepCost = cost;
		jsonToFile(this.gconf,CONFIG_FILE);
	}

	@Override
	public Map<String, Town> loadTowns() {
		Map<String, Town> towns = new HashMap<String, Town>();
		File townsDir = new File(this.configDir,"towns");
		if(townsDir.exists() && townsDir.isDirectory()) {  //Only need to load anything if the directory is there
			String[] files = townsDir.list();
			for(String file : files) {
				Town t = jsonFromFile("towns"+File.separator+file,Town.class);
				towns.put(t.getName(), t);
			}
		}
		return towns;
	}

	@Override
	public void saveTowns(List<Town> towns) {
		for(Town t : towns) {
			saveTown(t);
		}
		
	}

	@Override
	public void saveTown(Town town) {
		File townsDir = new File(this.configDir,"towns");
		if(!townsDir.exists()) {
			//create the directory
			townsDir.mkdirs();
		}
		
		this.jsonToFile(town, "towns" + File.separator + town.getId() + ".json");
	}

	@Override
	public void deleteTown(Town town) {
		File deletedDir = new File(this.configDir,"deleted_towns");
		if(!deletedDir.exists()) {
			//create the directory
			deletedDir.mkdirs();
		}
		
		File townsDir = new File(this.configDir,"towns");
		File townFile = new File(townsDir,town.getId()+".json");
		townFile.renameTo(new File(deletedDir,town.getId()+".json"));
	}

}
