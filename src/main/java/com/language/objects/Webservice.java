package com.language.objects;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.language.utils.Datei;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import com.language.ancestor.Objekt;
import com.language.sys.Sys;

/**
 * @Created 20.03.2022
 * @Author Nihar
 * @Description
 * This object is used to send requests to a specific webservice.
 * The answer from the webservice is only used in reference to the transfer-object.
 */
public class Webservice extends Objekt
{
	//	Attribute: HTTP-Request
	CloseableHttpClient httpClient = HttpClients.createDefault();
	HttpPost httpDetectPost = null;
	HttpPost httpLanguagePost = null;
	String apiKey = null;
	
	//	Attribute: JSON
    JSONParser jsonParser = new JSONParser();

	/* ************************************* */
	/* CONSTRUCTOR */
	/* ************************************* */
    
	public Webservice() { }

	/* ************************************* */
	/* OBJEKT-ANWEISUNGEN */
	/* ************************************* */

	/**
	 * This function gets a translated text by calling a specific webservice.
	 * <b>For this function the webservice connection is needed!
	 * @param translatedText Text which should be translated in another language.
	 *                       For example:
	 *                       TranslatedText = 'Hello world.'
	 * @param sourceLanguage Country code from the text which should be translated.
	 *                       For example:
	 *                       sourceLanguage = 'EN'
	 * @param translateLanguage Country code in which the text should be translated.
	 *                          For example:
	 *                          translateLanguage = 'DE'
	 * @return A string with the translated text. For example: 'Hallo welt.'
	 */
	public String of_getTranslatedTextBySpecificSource(String translatedText, String sourceLanguage, String translateLanguage) 
	{
        //	Attribute die wir �bertragen wollen, definieren...
		List<NameValuePair> entityForm = new ArrayList<>();
		entityForm.add(new BasicNameValuePair("q", translatedText));
		entityForm.add(new BasicNameValuePair("source", sourceLanguage));
		entityForm.add(new BasicNameValuePair("target", translateLanguage));
		entityForm.add(new BasicNameValuePair("format", "text"));
		if(apiKey != null)
		{
			entityForm.add(new BasicNameValuePair("api_key", apiKey));
		}
		entityForm.add(new BasicNameValuePair("content-type", "application/x-www-form-urlencoded"));
		
		//	URL-EcodedEntity erstellen, damit die REST-API-SST das auch erkennt und verwenden kann!
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(entityForm, Consts.UTF_8);
        httpLanguagePost.setEntity(entity);
        
        try
        {
        	//	Nach dem Ausf�hren auf den Response warten...
        	CloseableHttpResponse response = httpClient.execute(httpLanguagePost);
        	
        	//	ResponseBody in String konvertieren.
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
				
            //	String zum JSONObject konvertieren um einfach das Attribute auszulesen.
            JSONObject jsonObject = (JSONObject) jsonParser.parse(responseBody);
            
            if(jsonObject != null) 
            {
                String translatedMessage = (String) jsonObject.get("translatedText");
                
                if(translatedMessage != null) 
                {
                	return translatedMessage;
                }
            }
		}
        catch (Exception ignored) { }
		
		return translatedText;
	}

	/**
	 * Overload of function of_getTranslatedTextBySpecificSource();
	 * <b>This function is using by default 'auto-translation' as the sourceLanguage!</>
	 * @param translatedText Text which should be translated.
	 * @param translateLanguage Country code of the language in which the text should be translated.
	 *                          For example: 'EN'
	 * @return A string in the translated language.
	 */
	public String of_getTranslatedTextByAutoSource(String translatedText, String translateLanguage) 
	{
		return of_getTranslatedTextBySpecificSource(translatedText, "auto", translateLanguage);
	}

	/**
	 * This function checks the user defined webservice.
	 * <b>Not supported yet, this function uses the default-webservice as default!</>
	 * @return 1 = SUCCESS, -1 = ERROR
	 */
	public int of_checkConnection4WebService() 
	{
		//	In den Einstellungen schauen, ob ein eigener Webservice angegeben wurde...
		if(Settings.of_getInstance() != null)
		{
			Datei datei = Settings.of_getInstance().of_getSettingsFile();

			if(datei != null && datei.of_fileExists())
			{
				String sectionKey = Sys.of_getPaket();
				boolean lb_ownWebservice = datei.of_getBooleanByKey(sectionKey + ".Settings.OwnWebservice");

				if(lb_ownWebservice)
				{
					Sys.of_debug("[WebService]: ...try to use own webservice...");

					Datei webserviceFile = new Datei(Sys.of_getMainFilePath()+"external\\webservice.yml");
					String libreKey = "Webservice.Libretranslate";

					//	Read libre-settings.
					boolean lb_libreTranslate = webserviceFile.of_getSetBoolean(libreKey + ".Use", true);

					if(lb_libreTranslate)
					{
						Sys.of_debug("[WebService]: Use a LibreTranslate-Webservice... try to connect...");
						String httpDetect = webserviceFile.of_getSetString(libreKey + ".Check4ConnectionHTTPDetect", "http://localhost:5000/detect");
						int rc = -1;
						httpDetectPost = new HttpPost(httpDetect);

						//	Do not check for http-detect if the given string is empty or null!
						if(httpDetect == null || httpDetect.isEmpty())
						{
							httpDetectPost = null;
							rc = 1;
						}

						//	Check for the api-key!
						apiKey = webserviceFile.of_getSetString(libreKey + ".ApiKey", "");

						rc = of_checkConnection2DefaultWebService();

						if(rc == 1)
						{
							String httpTranslate = webserviceFile.of_getSetString(libreKey + ".TranslateHTTPRequest", "http://localhost:5000/translate");
							webserviceFile.of_save("Webservice.of_checkConnection4WebService(); #10 - Save the werbservice.yml");

							if(httpTranslate != null && !httpTranslate.isEmpty())
							{
								Sys.of_debug("[WebService]: ...we use the own webservice.");
								httpLanguagePost = new HttpPost(httpTranslate);
								return 1;
							}
						}
					}

					Sys.of_debug("[WebService]: No connection to the own-webservice has been established!");
				}
			}
		}

		Sys.of_debug("[WebService]: ...use default webservice...");
		apiKey = null;
		httpDetectPost = new HttpPost("http://65.108.238.67:5000/detect");
		httpLanguagePost = new HttpPost("http://65.108.238.67:5000/translate");
		return of_checkConnection2DefaultWebService();
	}

	/**
	 * This function checks the connection to the webservice.
	 * <b>If the connection is not valid, the plugin cannot be used and
	 * gets disabled!
	 * @return 1 = SUCCESS, -1 = ERROR - Disabling the plugin.
	 */
	private int of_checkConnection2DefaultWebService() 
	{
		//	RC:
		//	 1: OK
		//	-1: Error

		//	We do not check the detect-api request!
		if(httpDetectPost == null)
		{
			return 1;
		}

        //	Attribute die wir �bertragen wollen, definieren...
		List<NameValuePair> formEntity = new ArrayList<>();
		formEntity.add(new BasicNameValuePair("q", "connection"));
		if(apiKey != null)
		{
			formEntity.add(new BasicNameValuePair("api_key", apiKey));
		}

		//	URL-EcodedEntity erstellen, damit die REST-API-SST das auch erkennt und verwenden kann!
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formEntity, Consts.UTF_8);
        httpDetectPost.setEntity(entity);
		
        try
        {
        	//	ResponseBody ermitteln...
        	CloseableHttpResponse response = httpClient.execute(httpDetectPost);
        	
        	if(response != null) 
        	{
        		//	ResponseBody zum String konvertieren...
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
               
                if(responseBody != null) 
                {
                	//	Wir erhalten ien JSONArray
                	Object parsedObject = JSONValue.parse(responseBody);
                	JSONArray jsonArray = (JSONArray) parsedObject;
                	JSONObject jsonObject = (JSONObject) jsonArray.get(0);
                	
                	//	Nach dem ermitteln des JSONObjects nach dem Attribute 'language' schauen.
                	if(jsonObject != null) 
                	{
                        String value = (String) jsonObject.get("language");
                        
                        //	Wenn EN gefunden wird, war die Abfrage erfolgreich.
                        if(value != null && value.equals("en")) 
                        {
                        	return 1;
                        }
                	}
                }
        	}
		}
        catch (Exception ignored) { }
		
		return -1;
	}

	/* ************************************* */
	/* DECONSTRUCTOR */
	/* ************************************* */
	
	@Override
	public void of_unload()
	{
		try
		{
			httpClient.close();
			httpDetectPost.abort();
			httpLanguagePost.abort();
		}
		catch (Exception ignored) { }
	}
}
