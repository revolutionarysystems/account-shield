package uk.co.revsys.account.shield.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONObject;
import uk.co.revsys.account.shield.AccountShield;
import uk.co.revsys.account.shield.Device;
import uk.co.revsys.account.shield.Page;
import uk.co.revsys.account.shield.Session;
import uk.co.revsys.account.shield.User;

@Path("/lwc/{accountId}")
public class LWCService extends AbstractService{

    private AccountShield accountShield;
    private ObjectMapper objectMapper = new ObjectMapper();

    public LWCService(AccountShield accountShield) {
        this.accountShield = accountShield;
    }
    
    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("accountId") String accountId, @PathParam("userId") String userId){
        try{
            User user = accountShield.getUser(accountId, userId);
            return Response.ok().header("Access-Control-Allow-Origin", "*").entity(new JSONObject(user).toString()).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/{userId}/sessions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSessions(@PathParam("accountId") String accountId, @PathParam("userId") String userId){
        try {
            List<Session> sessions = accountShield.getSessions(accountId, userId);
            return Response.ok().header("Access-Control-Allow-Origin", "*").entity(objectMapper.writeValueAsString(sessions)).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/{userId}/sessions/{sessionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSession(@PathParam("accountId") String accountId, @PathParam("userId") String userId, @PathParam("sessionId") String sessionId){
        try {
            Session session = accountShield.getSession(accountId, userId, sessionId);
            System.out.println("session = " + session);
            return Response.ok().header("Access-Control-Allow-Origin", "*").entity(objectMapper.writeValueAsString(session)).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/{userId}/sessions/{sessionId}/history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSessionHistory(@PathParam("accountId") String accountId, @PathParam("userId") String userId, @PathParam("sessionId") String sessionId){
        try {
            List<Page> history = accountShield.getSessionHistory(accountId, userId, sessionId);
            System.out.println("history = " + history);
            return Response.ok().header("Access-Control-Allow-Origin", "*").entity(objectMapper.writeValueAsString(history)).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/{userId}/devices")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevices(@PathParam("accountId") String accountId, @PathParam("userId") String userId){
        try {
            List<Device> devices = accountShield.getDevices(accountId, userId);
            return Response.ok().header("Access-Control-Allow-Origin", "*").entity(objectMapper.writeValueAsString(devices)).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/{userId}/devices/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevice(@PathParam("accountId") String accountId, @PathParam("userId") String userId, @PathParam("deviceId") String deviceId){
        try {
            Device device = accountShield.getDevice(accountId, userId, deviceId);
            return Response.ok().header("Access-Control-Allow-Origin", "*").entity(objectMapper.writeValueAsString(device)).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/{userId}/devices/{deviceId}/sessions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSessionsForDevice(@PathParam("accountId") String accountId, @PathParam("userId") String userId, @PathParam("deviceId") String deviceId){
        try {
            List<Session> sessions = accountShield.getSessionsForDevice(accountId, userId, deviceId);
            return Response.ok().header("Access-Control-Allow-Origin", "*").entity(objectMapper.writeValueAsString(sessions)).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
}
