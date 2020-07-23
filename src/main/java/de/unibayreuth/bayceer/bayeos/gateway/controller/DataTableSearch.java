package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@SessionScope
@Component
public class DataTableSearch {
	
	private DataTablesInput input;

	public DataTablesInput getInput() {
		return input;
	}

	public void setInput(DataTablesInput input) {
		this.input = input;
	}
	
	public String getSearchString() {
		if (input != null) {
			return input.getSearch().getValue();
		} else {
			return null;
		}
	}
		
	public int getStart() {
		if (input != null) {
			return input.getStart();
		} else {
			return 0;
		}
	}
	
		
	public List<Object[]> getOrder(){
		List<Object[]> r = new ArrayList<Object[]>();
		if (input!=null && input.getOrder()!=null) {						
			for (Order p:input.getOrder()) {
				Object[] o = new Object[2];
				o[0] = p.getColumn(); 
				o[1] = p.getDir();
				r.add(o);
			}							
		} else {
			Object[] o = new Object[2];
			o[0] = 0; 
			o[1] = "asc";
			r.add(o);
		}
		return r;
	}
	
	

}
