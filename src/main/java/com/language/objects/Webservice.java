package com.language.objects;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

public class Webservice extends Objekt
{
	//	Attribute: HTTP-Request
	CloseableHttpClient httpClient = HttpClients.createDefault();
	HttpPost httpDetectPost = new HttpPost("http://mc-altis.de:5000/detect");
	HttpPost httpLanguagePost = new HttpPost("http://mc-altis.de:5000/translate");
	
	//	Attribute: JSON
    JSONParser jsonParser = new JSONParser();

	/* CONSTRUCTOR */
	/***************************************/
    
	public Webservice() { }

	/* OBJEKT-ANWEISUNGEN */
	/***************************************/
	
	public String of_getTranslatedTextBySpecificSource(String translatedText, String sourceLanguage, String translateLanguage) 
	{
        //	Attribute die wir �bertragen wollen, definieren...
		List<NameValuePair> entityForm = new ArrayList<>();
		entityForm.add(new BasicNameValuePair("q", translatedText));
		entityForm.add(new BasicNameValuePair("source", sourceLanguage));
		entityForm.add(new BasicNameValuePair("target", translateLanguage));
		entityForm.add(new BasicNameValuePair("format", "text"));
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
	
	public String of_getTranslatedTextByAutoSource(String translatedText, String translateLanguage) 
	{
		return of_getTranslatedTextBySpecificSource(translatedText, "auto", translateLanguage);
	}
	
	public int of_checkConnection4WebService() 
	{
		//	In den Einstellungen schauen, ob ein eigener Webservice angegeben wurde...
		/*
		if(main.SETTINGS != null)
		{
			Datei datei = main.SETTINGS.of_getSettingsFile();

			if(datei != null && datei.of_fileExists())
			{
				String sectionKey = Sys.of_getPaket();
				boolean lb_ownWebservice = datei.of_getBooleanByKey(sectionKey + ".Settings.OwnWebservice");

				if(lb_ownWebservice)
				{
					Sys.of_debug("[WebService]: ...use own webservice...");

					Datei webserviceFile = new Datei(Sys.of_getMainFilePath()+"external\\webservice.yml");

					if(webserviceFile != null)
					{
						//	Inhalte einlesen...
						//	Todo...

						return 1;
					}
				}
			}
		}
		*/

		Sys.of_debug("[WebService]: ...use default webservice...");
		return of_checkConnection2DefaultWebService();
	}
	
	private int of_checkConnection2DefaultWebService() 
	{
		//	RC:
		//	 1: OK
		//	-1: Error
		
        //	Attribute die wir �bertragen wollen, definieren...
		List<NameValuePair> formEntity = new ArrayList<>();
		formEntity.add(new BasicNameValuePair("q", "connection"));
		
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

	/* DECONSTRUCTOR */
	/***************************************/
	
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
