package ranker;

import java.util.ArrayList;

import ranker.Page;


public  class SeDocument
{

	int id;

	double idf;

	String word;

	//Page array
	Page[] pages;

	public SeDocument(int id, double idf,String word,Page[] pages)
	{

		this.id=id;
		this.idf=idf;
		this.word=word;
		this.pages=pages;
	}

}