package com.web.tech.controller;

import com.web.tech.model.User;
import com.web.tech.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public LoginController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        // Check if user is already authenticated

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            // Fixed string comparison with equals()

            // User is already logged in, redirect based on role
            try {
                User user = userService.findByEmail(auth.getName());
                if ("ADMIN".equals(user.getRole())) {
                    return "redirect:/admin/dashboard";
                } else {
                    return "redirect:/user/dashboard";
                }
            } catch (Exception e) {
                // If user not found, continue to login page
            }
        }

        return "login";
    }

    @PostMapping("/login-process")
    public String processLogin(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "remember-me", required = false) boolean rememberMe,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            // Create authentication token
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(email, password);

            // Add details to the authentication token
            authToken.setDetails(new WebAuthenticationDetails(request));

            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(authToken);

            // Set the authentication in the security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get the authenticated user
            User user = userService.findByEmail(email);

            // Redirect based on role
            if ("ADMIN".equals(user.getRole())) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/user/dashboard";
            }

        } catch (AuthenticationException e) {
            // Authentication failed
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid email or password");
            return "redirect:/login?error=true";
        }
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        // Get authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        model.addAttribute("user", user);

        return "admin/dashboard";
    }

    @GetMapping("/user/dashboard")
    public String userDashboard(Model model) {
        // Get authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        model.addAttribute("user", user);

        return "user/dashboard";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}