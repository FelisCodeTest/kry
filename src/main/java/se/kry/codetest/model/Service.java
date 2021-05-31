package se.kry.codetest.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Service {
    String name;
    String url;
    String host;
    String lastStatus;
    String creationDate;
    public Service(String name, String url){
        this(name, url, null, "UNKNOWN");
    }

    public Service(String name, String url, String creationDate, String lastStatus){
        this.name = name;
        this.url = url;
        if (creationDate == null)
            this.creationDate = getDate();
        else
            this.creationDate = creationDate;
        URI uri = null;
        try {
            uri = new URI(url);
            this.host = uri.getHost();
        } catch (URISyntaxException e) {
            this.host = null;
            e.printStackTrace();
        }
        this.lastStatus = lastStatus;
    }
    private String getDate(){
        //get date in ISO8601 format
        String pattern = "yyyy-MM-dd hh:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date date = new Date();
        return  sdf.format(date);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String domain) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(String lastStatus) {
        this.lastStatus = lastStatus;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }



}
