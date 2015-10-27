package uk.co.revsys.account.shield.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.shiro.SecurityUtils;
import org.json.JSONObject;
import uk.co.revsys.account.shield.AccountShield;
import uk.co.revsys.account.shield.Device;
import uk.co.revsys.account.shield.DeviceCheck;
import uk.co.revsys.account.shield.Session;
import uk.co.revsys.account.shield.User;

@Path("/")
public class MainService extends AbstractService{

    private AccountShield accountShield;
    private ObjectMapper objectMapper = new ObjectMapper();

    public MainService(AccountShield accountShield) {
        this.accountShield = accountShield;
    }
    
    @POST
    @Path("/registerUser")
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(@FormParam("sessionId") String sessionId, @FormParam("userId") String userId, @FormParam("email") String email){
        User user = new User(userId);
        user.setEmail(email);
        try {
            accountShield.registerUser(getAccountId(), sessionId, user);
            return Response.ok().build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("userId") String userId){
        try{
            User user = accountShield.getUser(getAccountId(), userId);
            return Response.ok().entity(new JSONObject(user).toString()).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @POST
    @Path("/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@PathParam("userId") String userId, @FormParam("email") String email){
        User user = new User(userId);
        user.setEmail(email);
        try{
            accountShield.updateUser(getAccountId(), user);
            return Response.ok().build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/checkDevice")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkDevice(@QueryParam("sessionId") String sessionId, @QueryParam("userId") String userId){
        try {
            DeviceCheck deviceCheck = accountShield.checkDevice(getAccountId(), sessionId, userId);
            return Response.ok().entity(new JSONObject(deviceCheck).toString()).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/requestDeviceVerification")
    @Produces(MediaType.APPLICATION_JSON)
    public Response requestDeviceVerification(@QueryParam("sessionId") String sessionId, @QueryParam("userId") String userId){
        try {
            accountShield.requestDeviceVerification(getAccountId(), sessionId, userId);
            return Response.ok().build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/verifyDevice")
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifyDevice(@QueryParam("sessionId") String sessionId, @QueryParam("userId") String userId, @QueryParam("verificationCode") String verificationCode){
        try {
            accountShield.verifyDevice(getAccountId(), sessionId, userId, verificationCode);
            return Response.ok().build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/{userId}/sessions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSessions(@PathParam("userId") String userId){
        try {
            List<Session> sessions = accountShield.getSessions(getAccountId(), userId);
            return Response.ok().header("Access-Control-Allow-Origin", "*").entity(objectMapper.writeValueAsString(sessions)).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/{userId}/sessions/{sessionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSession(@PathParam("userId") String userId, @PathParam("sessionId") String sessionId){
        try {
            Session session = accountShield.getSession(getAccountId(), userId, sessionId);
            System.out.println("session = " + session);
            return Response.ok().entity(objectMapper.writeValueAsString(session)).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/{userId}/devices")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevices(@PathParam("userId") String userId){
        try {
            List<Device> devices = accountShield.getDevices(getAccountId(), userId);
            return Response.ok().entity(objectMapper.writeValueAsString(devices)).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    @GET
    @Path("/{userId}/device/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevices(@PathParam("userId") String userId, @PathParam("deviceId") String deviceId){
        try {
            Device device = accountShield.getDevice(getAccountId(), userId, deviceId);
            return Response.ok().entity(objectMapper.writeValueAsString(device)).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }
    
    private String getAccountId(){
        return SecurityUtils.getSubject().getPrincipals().oneByType(uk.co.revsys.user.manager.model.User.class).getAccount();
    }
    
}