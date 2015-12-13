package com.ufmg.objects;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class HaremList {
	
	@ElementList
   private List<HaremObject> list;

	@Override
	public String toString() {
		return "HaremList [list=" + list + "]";
	}

	public List<HaremObject> getList() {
		return list;
	}

	public void setList(List<HaremObject> list) {
		this.list = list;
	}
	
	
	
	
}
