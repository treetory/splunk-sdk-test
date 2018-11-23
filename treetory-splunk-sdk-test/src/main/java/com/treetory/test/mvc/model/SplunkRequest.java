package com.treetory.test.mvc.model;

import java.io.Serializable;

public class SplunkRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8333156916380283832L;

	private final String[] queries = {
			"search index=\"doosan-action\" virusAction=\"DELETED\" | timechart count(virusAction) span=1d",									// 악성코드 삭제 추이
			"search index=\"doosan-action\" virusAction=\"QUARANTINED\" | timechart count(virusAction) span=1d",								// 악성코드 격리 추이
			"search index=\"doosan-action\" virusAction=\"CURED\" | timechart count(virusAction) span=1d",										// 악성코드 치료 추이
			"search index=\"doosan-action\" virusAction=\"NOT CURED\" | timechart count(virusAction) span=1d",									// 악성코드 불치료 추이
			"search index=\"doosan-action\" | timechart sum(eval(severity=\"Critical\")) by groupName | stats sum",								// 경보상태
			"search index=\"doosan-alert\" | timechart sum(eval(severity=\"Critical\")) by groupName | stats sum | eval zount=1 | table *",		// 바이러스 감염 노드
			"search index=\"doosan-alert\" virusName=* | top limit=5 ipAddr  showperc=false",													// 바이러스 감염빈도 TOP5
			"search index=\"doosan-alert\" | timechart  sum(eval(severity=\"Warning\")) by groupName | stats sum | eval zount=1 | table *",		// 악성 바이러스 감염 노드
			"search index=\"doosan-alert\" virusName=* virusAction=\"NOT CURED\" | top limit=5 ipAddr  showperc=false  |",						// 악성 바이러스 감염빈도 TOP5
			"search index=\"doosan-status\" *"
	};
	
	private String query;
	
	public SplunkRequest(int i) {
		this.query = queries[i];
	}
	
	public String getQuery() {
		return this.query;
	}
}
