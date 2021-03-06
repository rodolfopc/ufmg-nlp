package com.ufmg.masters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.ufmg.objects.HaremList;
import com.ufmg.objects.HaremObject;

public class EntityExtractor {
	
	public List<HaremObject> entities;
	
	public EntityExtractor() {
		entities = new ArrayList<HaremObject>();
		
		String[] haremFiles = { "../arquivos/CDSegundoHAREM_TEMPO.xml" , "../arquivos/CDSegundoHAREMReRelEM.xml"};
		
		for (int i = 0; i < haremFiles.length; i++) {
			HaremList list = openFile(haremFiles[i]);
			if(list!=null)
				entities.addAll(list.getList());
		}
		
	}
	
	
	public static void main(String[] args) throws IOException {
		
		
		
		
	}
	
	public void initializer() throws IOException{
		getEntities("../arquivos/coleccoes/CDSegundoHAREM_TEMPO.xml");
		getEntities("../arquivos/coleccoes/CDSegundoHAREMReRelEM.xml");
	}
	
	public HaremList openFile(String filePath){

		Serializer serializer = new Persister();
		try {
			File source = new File(filePath);
			HaremList entityFile = serializer.read(HaremList.class, source);
			return entityFile;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static void getEntities(String filePath) throws IOException{
//		BufferedReader readTree = new BufferedReader(new FileReader("/Users/rodolfopc/git/ufmg-qg/arquivos/coleccoes/CDSegundoHAREM_TEMPO.xml"),"UTF-8");
		File fileDir = new File(filePath);
		BufferedReader readTree = new BufferedReader(new InputStreamReader( new FileInputStream(fileDir),"ISO-8859-1"));


		Serializer serializer = new Persister();
		
		StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<harem><list>");
		String line = readTree.readLine();
		while (line != null) {
			int inicio = line.indexOf("<EM");
			int fim = line.indexOf("</EM>");
			
			if(inicio!=-1 && fim!=-1){
				String entity = line.substring(inicio, fim+5);
				sb.append(entity+"\n");

				try {
					HaremObject obj = serializer.read(HaremObject.class, entity);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			line = readTree.readLine();
		}
		sb.append("</list></harem>");
		
		String fileName = filePath.substring(filePath.lastIndexOf("/")+1, filePath.lastIndexOf("."));
		
		saveFile(sb, "../arquivos/"+fileName+".xml");
		
	}
	
	public static void saveFile(StringBuilder sb, String path){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(path, "UTF-8");
			
			writer.println(sb.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(writer!=null)
				writer.close();
		}
	}

	
	HashMap<String, String> palavrasEntity;
	public HashMap<String, String> extract(String[] palavras){
		String frase = Arrays.toString(palavras);
		palavrasEntity = new HashMap<String, String>();
		for (int i = 0; i < entities.size(); i++) {
			if(entities.get(i).getEM()!=null){
				if(frase.contains(entities.get(i).getEM())){
					if (!palavrasEntity.containsKey(entities.get(i).getEM())) {
						palavrasEntity.put(entities.get(i).getEM(), entities.get(i).getCATEG());
					}
				}
			}
		}
		System.out.print(palavrasEntity.toString()+"\n");
		
		return palavrasEntity;
	}
	
	
	
	
	
}
