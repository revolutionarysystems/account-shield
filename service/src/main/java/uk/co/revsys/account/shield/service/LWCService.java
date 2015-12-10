package uk.co.revsys.account.shield.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("accountId") String accountId, @QueryParam("userId") String userId){
        if(userId == null || userId.isEmpty()){
            return Response.status(Response.Status.BAD_REQUEST).entity("You must provide a userId").build();
        }
        try{
            User user = accountShield.getUser(accountId, userId);
            return Response.ok().header("Access-Control-Allow-Origin", "*").entity(new JSONObject(user).toString()).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/sessions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSessions(@PathParam("accountId") String accountId, @QueryParam("userId") String userId, @DefaultValue("0") @QueryParam("offset") int offset, @DefaultValue("5") @QueryParam("limit") int limit){
        if(userId == null || userId.isEmpty()){
            return Response.status(Response.Status.BAD_REQUEST).entity("You must provide a userId").build();
        }
        try {
            List<Session> sessions = accountShield.getSessions(accountId, userId, offset, limit);
            return Response.ok().header("Access-Control-Allow-Origin", "*").entity(objectMapper.writeValueAsString(sessions)).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/sessions/{sessionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSession(@PathParam("accountId") String accountId, @QueryParam("userId") String userId, @PathParam("sessionId") String sessionId){
        if(userId == null || userId.isEmpty()){
            return Response.status(Response.Status.BAD_REQUEST).entity("You must provide a userId").build();
        }
        try {
            Session session = accountShield.getSession(accountId, userId, sessionId);
            System.out.println("session = " + session);
            return Response.ok().header("Access-Control-Allow-Origin", "*").entity(objectMapper.writeValueAsString(session)).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/sessions/{sessionId}/history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSessionHistory(@PathParam("accountId") String accountId, @QueryParam("userId") String userId, @PathParam("sessionId") String sessionId){
        if(userId == null || userId.isEmpty()){
            return Response.status(Response.Status.BAD_REQUEST).entity("You must provide a userId").build();
        }
        try {
            List<Page> history = accountShield.getSessionHistory(accountId, userId, sessionId);
            System.out.println("history = " + history);
            return Response.ok().header("Access-Control-Allow-Origin", "*").entity(objectMapper.writeValueAsString(history)).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/devices")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevices(@PathParam("accountId") String accountId, @QueryParam("userId") String userId){
        if(userId == null || userId.isEmpty()){
            return Response.status(Response.Status.BAD_REQUEST).entity("You must provide a userId").build();
        }
        try {
            List<Device> devices = accountShield.getDevices(accountId, userId);
            return Response.ok().header("Access-Control-Allow-Origin", "*").entity(objectMapper.writeValueAsString(devices)).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/devices/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevice(@PathParam("accountId") String accountId, @QueryParam("userId") String userId, @PathParam("deviceId") String deviceId){
        if(userId == null || userId.isEmpty()){
            return Response.status(Response.Status.BAD_REQUEST).entity("You must provide a userId").build();
        }
        try {
            Device device = accountShield.getDevice(accountId, userId, deviceId);
            return Response.ok().header("Access-Control-Allow-Origin", "*").entity(objectMapper.writeValueAsString(device)).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/devices/{deviceId}/sessions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSessionsForDevice(@PathParam("accountId") String accountId, @QueryParam("userId") String userId, @PathParam("deviceId") String deviceId, @DefaultValue("0") @QueryParam("offset") int offset, @DefaultValue("5") @QueryParam("limit") int limit){
        if(userId == null || userId.isEmpty()){
            return Response.status(Response.Status.BAD_REQUEST).entity("You must provide a userId").build();
        }
        try {
            List<Session> sessions = accountShield.getSessionsForDevice(accountId, userId, deviceId, offset, limit);
            return Response.ok().header("Access-Control-Allow-Origin", "*").entity(objectMapper.writeValueAsString(sessions)).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/devices/{deviceId}/disown")
    @Produces(MediaType.APPLICATION_JSON)
    public Response disownDevice(@PathParam("accountId") String accountId, @QueryParam("userId") String userId, @PathParam("deviceId") String deviceId){
        if(userId == null || userId.isEmpty()){
            return Response.status(Response.Status.BAD_REQUEST).entity("You must provide a userId").build();
        }
        try {
            List<Session> sessions = accountShield.disownDevice(accountId, userId, deviceId);
            return Response.ok().header("Access-Control-Allow-Origin", "*").entity(objectMapper.writeValueAsString(sessions)).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }


}
