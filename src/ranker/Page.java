package ranker;

import java.util.ArrayList;

public class Page
{
	double ntf;
	int urlId;
	double tfidf;
	ArrayList<String>tags;
	public Page(double ntf,int urlId, ArrayList<String>tags)
	{
		this.urlId=urlId;
		this.ntf=ntf;
		this.tags=tags;
	}
	public void setTFIDF(double tfidf)
	{
	this.tfidf=tfidf;
	}
}
