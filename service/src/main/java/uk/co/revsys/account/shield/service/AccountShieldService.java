package uk.co.revsys.account.shield.service;

import javax.ws.rs.Consumes;
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
import uk.co.revsys.account.shield.DeviceCheck;
import uk.co.revsys.account.shield.User;

@Path("/")
public class AccountShieldService {

    private AccountShield accountShield;

    public AccountShieldService(AccountShield accountShield) {
        this.accountShield = accountShield;
    }
    
    @POST
    @Path("{sessionId}/registerUser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(@PathParam("sessionId") String sessionId, String jsonString){
        JSONObject json = new JSONObject(jsonString);
        User user = new User(json.getString("id"));
        user.setEmail(json.getString("email"));
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
    public Response updateUser(@PathParam("userId") String userId, String jsonString){
        JSONObject json = new JSONObject(jsonString);
        User user = new User(userId);
        user.setEmail(json.getString("email"));
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
    
    private String getAccountId(){
        return SecurityUtils.getSubject().getPrincipals().oneByType(uk.co.revsys.user.manager.model.User.class).getAccount();
    }
    
    private Response handleException(Exception exception){
        JSONObject json = new JSONObject();
        json.put("type", exception.getClass().getSimpleName());
        json.put("message", exception.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json.toString()).build();
    }
    
}
