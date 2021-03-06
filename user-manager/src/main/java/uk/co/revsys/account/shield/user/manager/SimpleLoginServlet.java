package uk.co.revsys.account.shield.user.manager;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import uk.co.revsys.account.shield.AccountShieldClient;
import uk.co.revsys.account.shield.AccountShieldException;
import uk.co.revsys.account.shield.LoginCheck;
import uk.co.revsys.account.shield.UserAlreadyExistsException;
import uk.co.revsys.account.shield.UserNotFoundException;
import uk.co.revsys.user.manager.model.User;

public class SimpleLoginServlet extends uk.co.revsys.user.manager.servlet.SimpleLoginServlet {

    private AccountShieldClient accountShieldClient;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        accountShieldClient = webApplicationContext.getBean(AccountShieldClient.class);
    }

    @Override
    public void doSuccess(HttpServletRequest req, HttpServletResponse resp) throws javax.servlet.ServletException, IOException {
        Subject subject = SecurityUtils.getSubject();
        User user = subject.getPrincipals().oneByType(User.class);
        String userId = user.getId();
        String sessionId = req.getParameter("sessionId");
        try {
            try {
                LoginCheck loginCheck = accountShieldClient.checkLogin(sessionId, userId);
                if (loginCheck.getRequiresVerification()) {
                    accountShieldClient.requestDeviceVerification(sessionId, userId);
                    subject.logout();
                    resp.setStatus(200);
                    resp.getOutputStream().print("verifyDevice.html?sessionId=" + sessionId + "&userId=" + userId);
                    resp.getOutputStream().flush();
                    resp.getOutputStream().close();
                    return;
                }
            } catch (UserNotFoundException ex) {
                uk.co.revsys.account.shield.User asUser = new uk.co.revsys.account.shield.User(userId);
                asUser.setEmail(user.getUsername());
                try {
                    accountShieldClient.registerUser(asUser);
                } catch (UserAlreadyExistsException exc) {
                    // Ignore - should never happen
                }
            }
        } catch (AccountShieldException ex) {

        }
        super.doSuccess(req, resp);
    }

}
