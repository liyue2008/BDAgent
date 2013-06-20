package bdagent.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;


/**
 * 配置文件工具类
 * */
public class ConfigHelp {
	private static Properties properties;
	private final static Logger logger = LoggerFactory.getLogger(ConfigHelp.class);
	/**
	 * 读取配置文件
	 * @param key
	 * 参数名
	 * @param defaultValue
	 * 默认值
	 * @return 参数值
	 * */
	public static String getConifg(String key,String defaultValue){

		return getProperties(key,defaultValue);

	}
	
	/**
	 * 获取配置文件所有配置项
	 * @return 配置的Map值
	 * */
	public static Map<String,String> getAllConfig(){
		Map<String,String> result = new HashMap<String, String>();

        for (Entry<Object, Object> pairs : properties.entrySet()) {
            result.put(String.valueOf(pairs.getKey()), String.valueOf(pairs.getValue()));
        }

		return result;
	}
	
	/**
	 * 读取本机的配置文件,不去判断远程模式及工程模式
	 * @param key
	 * 参数名
	 * @param defaultValue
	 * 默认值
	 * @return 参数值
	 * */
	public static String getLocalConifg(String key,String defaultValue){
		return getProperties(key,defaultValue);
	}
	
	
	public static URL findAsResource(String path) {
		URL url = null;

		// First, try to locate this resource through the current
		// context classloader.
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		if (contextClassLoader != null) {
			url = contextClassLoader.getResource(path);
		}
		if (url != null)
			return url;
		// Next, try to locate this resource through the system classloader
		url = ClassLoader.getSystemClassLoader().getResource(path);

		// Anywhere else we should look?
		return url;
	}
	

	
	/**
	 * 读取配置文件
	 * */
	private static String getProperties(String key,String defaultValue){
		if(properties==null){
			try{
				String path = ProgramPathHelp.getProgramPath() +System.getProperty("file.separator")+"configuration.properties";
				URL url;
				File file = new File(path);
				try {
					if (file.exists()) {

						url = file.toURI().toURL();
					} else {
						path="configuration.properties";
						url = new URL(path);
					}
				} catch (MalformedURLException e) {
					url = ConfigHelp.findAsResource(path);
				}
				if (null == url) {
					throw new FileNotFoundException(path);
				}
				InputStream in =  new BufferedInputStream(new FileInputStream(new File(url.toURI())));
				properties =  new Properties(); 
				properties.load(in);

			}catch(Exception ex){
				logger.warn("read configuration.properties while error", ex);
			}
		}

        try {
            String result = properties.getProperty(key);
            if(result!=null && !"".equals(result)){
                return result;
            }
        } catch (Exception e) {
            logger.warn("read configuration.properties while error", e);
        }
        return defaultValue;
	}
}
	
