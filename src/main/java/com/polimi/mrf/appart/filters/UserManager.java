package com.polimi.mrf.appart.filters;

import com.polimi.mrf.appart.HashGenerator;
import com.polimi.mrf.appart.beans.UserAuthServiceBean;
import com.polimi.mrf.appart.beans.UserServiceBean;
import com.polimi.mrf.appart.entities.User;
import com.polimi.mrf.appart.entities.UserAuthToken;
import jakarta.ejb.EJB;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.RandomStringUtils;


import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.Date;

@WebFilter(filterName="UserManagerFilter", urlPatterns="/api/reserved/*")
public class UserManager extends HttpFilter {

    @EJB
    UserAuthServiceBean userAuthServiceBean;

    //private static final int COOKIE_TIMEOUT=15768000; //6 months


    @Override
    public void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {

        //HttpSession session = req.getSession();
        User user = null;

        /*if (session != null && session.getAttribute("loggeduser") != null) {
            user = (User) session.getAttribute("loggeduser");
        } else {*/
            Cookie[] cookies = req.getCookies();

            if (cookies != null) {
                // process auto login for remember me feature
                String selector = "";
                String rawValidator = "";

                for (Cookie aCookie : cookies) {
                    if (aCookie.getName().equals("selector")) {
                        selector = aCookie.getValue();
                    } else if (aCookie.getName().equals("validator")) {
                        rawValidator = aCookie.getValue();
                    }
                }

                if (!"".equals(selector) && !"".equals(rawValidator)) {
                    UserAuthToken token = userAuthServiceBean.findAuthTokenBySelector(selector);

                    if (token != null) {
                        String hashedValidatorDatabase = token.getValidator();
                        String hashedValidatorCookie = HashGenerator.generateSHA256(rawValidator);

                        if (hashedValidatorCookie.equals(hashedValidatorDatabase)) {
                            //session = req.getSession();
                            //session.setAttribute("loggeduser", token.getUser());
                            user = token.getUser();

                        /*
                        // update new token in database
                        String newSelector = RandomStringUtils.randomAlphanumeric(12);
                        String newRawValidator = RandomStringUtils.randomAlphanumeric(64);

                        String newHashedValidator = HashGenerator.generateSHA256(newRawValidator);

                        token.setSelector(newSelector);
                        token.setValidator(newHashedValidator);
                        token.setLastUse(new Date());
                        userAuthServiceBean.update(token);

                        // update cookie
                        Cookie cookieSelector = new Cookie("selector", newSelector);
                        cookieSelector.setMaxAge(COOKIE_TIMEOUT);

                        Cookie cookieValidator = new Cookie("validator", newRawValidator);
                        cookieValidator.setMaxAge(COOKIE_TIMEOUT);

                        res.addCookie(cookieSelector);
                        res.addCookie(cookieValidator);

                         */
                        }

                    }
                }
            }
        //}
        if (user == null) {
            res.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            return;
        } else {
            req.setAttribute("user", user);
            chain.doFilter(req, res);
        }



                /*
        String email=req.getParameter("email");
        String password=req.getParameter("password");

        if (email==null || password==null || email.length()==0 || password.length()==0) {
            res.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            return;
        }

        User user=userServiceBean.getUser(email, password);
        if (user==null) {
            res.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        } else {
            req.setAttribute("user", user);
            chain.doFilter(req, res);
        }
         */
    }

    @Override
    public void destroy() {
    }
}
