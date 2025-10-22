package br.com.casadoamor.sgca.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Utility to generate BCrypt password hashes for database seeding
 * Run this class to generate hashes for the migration files
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder(12);
        
        System.out.println("==============================================");
        System.out.println("  BCrypt Password Hash Generator");
        System.out.println("==============================================");
        System.out.println();
        
        // Admin password
        String adminPassword = "Admin@123";
        String adminHash = encoder.encode(adminPassword);
        System.out.println("Password: " + adminPassword);
        System.out.println("Hash: " + adminHash);
        System.out.println();
        
        // Test password
        String testPassword = "Teste@123";
        String testHash = encoder.encode(testPassword);
        System.out.println("Password: " + testPassword);
        System.out.println("Hash: " + testHash);
        System.out.println();
        
        // Verify
        System.out.println("Verification:");
        System.out.println("Admin@123 matches: " + encoder.matches(adminPassword, adminHash));
        System.out.println("Teste@123 matches: " + encoder.matches(testPassword, testHash));
        
        System.out.println();
        System.out.println("==============================================");
        System.out.println("Copy the hash above to your migration file");
        System.out.println("==============================================");
    }
}
