package uk.co.revsys.account.shield.user.manager;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import uk.co.revsys.account.shield.AccountShieldClient;
import uk.co.revsys.account.shield.AccountShieldException;
import uk.co.revsys.account.shield.InvalidVerificationCodeException;

public class VerificationServlet extends HttpServlet{

    private AccountShieldClient accountShieldClient;
    private String loginUrl;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        accountShieldClient = webApplicationContext.getBean(AccountShieldClient.class);
        ShiroFilterFactoryBean shiroFactoryFactoryBean = webApplicationContext.getBean(ShiroFilterFactoryBean.class);
        loginUrl = shiroFactoryFactoryBean.getLoginUrl();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String sessionId = req.getParameter("sessionId");
        String userId = req.getParameter("userId");
        String verificationCode = req.getParameter("verificationCode");
        try {
            accountShieldClient.verifyDevice(sessionId, userId, verificationCode);
            resp.sendRedirect("verifyDevice.html?sessionId=" + sessionId + "&userId=" + userId);
//            resp.sendRedirect(userId);
        } catch (InvalidVerificationCodeException ex) {
            resp.sendRedirect("deviceVerificationFailed.html");
        } catch (AccountShieldException ex) {
            resp.sendRedirect("deviceVerificationFailed.html");
        }
    }

}
