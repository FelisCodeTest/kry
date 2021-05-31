package se.kry.codetest.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Service {
    String name;
    String url;
    String host;
    String lastStatus;
    String creationDate;
    public Service(String name, String url) throws URISyntaxException{
        this.name = name;
        this.url = url;

        //Set date in ISO8601 format
        String pattern = "yyyy-MM-dd hh:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date date = new Date();
        this.creationDate = sdf.format(date);

        URI uri = new URI(url);
        this.host = uri.getHost();

        this.lastStatus = "UNKNOWN";
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
