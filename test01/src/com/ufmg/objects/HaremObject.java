package com.ufmg.objects;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root
public class HaremObject {

	@Text(required=false)
   private String EM;

   @Attribute(required=false)
   private String ID;
   @Attribute(required=false)
   private String CATEG;
   @Attribute (required=false)
   private String TIPO;
   @Attribute (required=false)
   private String SUBTIPO;
   @Attribute(required=false)
   private String COREL;
   @Attribute(required=false)
   private String COMENT;
   @Attribute(required=false)
   private String TEMPO_REF;
   @Attribute(required=false)
   private String VAL_NORM;
   @Attribute(required=false)
   private String SENTIDO;
   @Attribute(required=false)
   private String VAL_DELTA;
   @Attribute(required=false)
   private String TIPOREL;
   
   
   public HaremObject(String eM, String iD, String cATEG, String tIPO,
		String cOREL, String tIPOREL) {
		super();
		EM = eM;
		ID = iD;
		CATEG = cATEG;
		TIPO = tIPO;
		COREL = cOREL;
		TIPOREL = tIPOREL;
	}
   
   


	public HaremObject() {
		super();
	}




	public String getEM() {
		return EM;
	}
	
	
	public void setEM(String eM) {
		EM = eM;
	}
	
	
	public String getID() {
		return ID;
	}
	
	
	public void setID(String iD) {
		ID = iD;
	}
	
	
	public String getCATEG() {
		return CATEG;
	}
	
	
	public void setCATEG(String cATEG) {
		CATEG = cATEG;
	}
	
	
	public String getTIPO() {
		return TIPO;
	}
	
	
	public void setTIPO(String tIPO) {
		TIPO = tIPO;
	}
	
	
	public String getCOREL() {
		return COREL;
	}
	
	
	public void setCOREL(String cOREL) {
		COREL = cOREL;
	}
	
	
	public String getTIPOREL() {
		return TIPOREL;
	}
	
	
	public void setTIPOREL(String tIPOREL) {
		TIPOREL = tIPOREL;
	}




	@Override
	public String toString() {
		return "HaremObject [EM=" + EM + ", ID=" + ID + ", CATEG=" + CATEG
				+ ", TIPO=" + TIPO + ", SUBTIPO=" + SUBTIPO + ", COREL="
				+ COREL + ", COMENT=" + COMENT + ", TEMPO_REF=" + TEMPO_REF
				+ ", VAL_NORM=" + VAL_NORM + ", SENTIDO=" + SENTIDO
				+ ", VAL_DELTA=" + VAL_DELTA + ", TIPOREL=" + TIPOREL + "]\n";
	}

   
	
}
