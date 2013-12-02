package com.andresjesse.jpctblend;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Exporter Info, used to store parsed information from XML.
 * 
 * @author andres
 * 
 */
public class ExporterInfo {
	private String author;
	private String contact;
	private Date date;// stored to future use
	private String script;
	private int version;

	public ExporterInfo(String author, String contact, Date date,
			String script, int version) {
		this.author = author;
		this.contact = contact;
		this.date = date;
		this.script = script;
		this.version = version;
	}

	public String getAuthor() {
		return author;
	}

	public String getContact() {
		return contact;
	}

	public Date getDate() {
		return date;
	}

	public String getScript() {
		return script;
	}

	public int getVersion() {
		return version;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public String toString() {
		Format formatter = new SimpleDateFormat("yyyy-MM-dd");
		return script + " v" + version + ", by " + author + " (" + contact
				+ "), " + formatter.format(date);
	}
}
