package com.web.tech.controller;

import com.web.tech.model.User;
import com.web.tech.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
public class LoginController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public LoginController(UserService userService,
                           AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/login")
    public String showLoginPage(Model model, HttpServletRequest request) {
        // Force logout of any current user
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("user");
            session.removeAttribute("SPRING_SECURITY_CONTEXT");
            try {
                session.invalidate();
            } catch (IllegalStateException e) {
                // Session might already be invalidated
            }
        }

        // Clear security context
        SecurityContextHolder.clearContext();

        // Handle logout request
        if (request.getParameter("logout") != null) {
            model.addAttribute("logoutMessage", "You have been successfully logged out");
        }

        // Handle error message if present
        if (request.getParameter("error") != null) {
            model.addAttribute("errorMessage", "Invalid credentials");
        }

        return "login";
    }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            String normalizedEmail = email.toLowerCase().trim();
            User user = userService.findByEmail(normalizedEmail);

            if (user == null) {
                System.out.println("User not found for email: " + normalizedEmail);
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid credentials");
                return "redirect:/login?error=true";
            }

            System.out.println("User email: " + normalizedEmail);
            System.out.println("User role from DB: " + user.getRole());

            String userRole = user.getRole() != null ? user.getRole().trim() : "USER";
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if ("ADMIN".equalsIgnoreCase(userRole)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else {
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }

            System.out.println("Assigned authorities: " + authorities);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(normalizedEmail, password, authorities);
            authToken.setDetails(new WebAuthenticationDetails(request));

            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("Security context authenticated: " + authentication.isAuthenticated());
            System.out.println("Security context authorities: " + authentication.getAuthorities());

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            session = request.getSession(true);

            // Persist security context to session
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            User sessionUser = new User();
            sessionUser.setId(user.getId());
            sessionUser.setEmail(user.getEmail());
            sessionUser.setRole(userRole);
            sessionUser.setFirstName(user.getFirstName());
            sessionUser.setLastName(user.getLastName());

            session.setAttribute("user", sessionUser);
            session.setAttribute("authenticated", true);

            System.out.println("Session user role set to: " + sessionUser.getRole());
            System.out.println("Redirecting to /admin for user: " + normalizedEmail);

            if ("ADMIN".equalsIgnoreCase(userRole)) {
                return "redirect:/admin";
            } else {
                return "redirect:/shop";
            }

        } catch (AuthenticationException e) {
            System.out.println("Authentication exception: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid credentials");
            return "redirect:/login?error=true";
        } catch (Exception e) {
            System.out.println("Unexpected error during login: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Unexpected error");
            return "redirect:/login?error=true";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}