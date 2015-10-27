package uk.co.revsys.account.shield.service;

import javax.ws.rs.core.Response;
import org.json.JSONObject;

public abstract class AbstractService {

    protected Response handleException(Exception exception) {
        exception.printStackTrace();
        JSONObject json = new JSONObject();
        json.put("type", exception.getClass().getSimpleName());
        json.put("message", exception.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Access-Control-Allow-Origin", "*").entity(json.toString()).build();
    }
}
