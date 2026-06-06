package com.supportflow.config;

import com.supportflow.entity.Project;
import com.supportflow.entity.SupportTask;
import com.supportflow.entity.User;
import com.supportflow.entity.CategoryMapping;
import com.supportflow.repository.ProjectRepository;
import com.supportflow.repository.TaskRepository;
import com.supportflow.repository.UserRepository;
import com.supportflow.repository.CategoryMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryMappingRepository categoryMappingRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Write database columns of 'users' to a text file for debugging
            try {
                java.util.List<java.util.Map<String, Object>> cols = jdbcTemplate.queryForList("SHOW COLUMNS FROM users");
                java.io.FileWriter writer = new java.io.FileWriter("db_columns_debug.txt");
                for (java.util.Map<String, Object> col : cols) {
                    writer.write(col.get("Field") + " : " + col.get("Type") + "\n");
                }
                writer.close();
                System.out.println("DEBUG COLUMNS written successfully.");
            } catch (Exception e) {
                try {
                    java.io.FileWriter writer = new java.io.FileWriter("db_columns_debug.txt");
                    writer.write("ERROR on SHOW COLUMNS: " + e.getMessage());
                    writer.close();
                } catch (Exception ignored) {}
            }

            // Self-repair MySQL database users table columns if Hibernate ddl-auto didn't do it
            try {
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN email VARCHAR(255) NULL");
                System.out.println("Ran raw SQL migration: email column ensured on 'users' table.");
            } catch (Exception e) {
                // Column probably already exists or table doesn't exist yet
            }

            try {
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN mobile VARCHAR(255) NULL");
                System.out.println("Ran raw SQL migration: mobile column ensured on 'users' table.");
            } catch (Exception e) {
                // Column probably already exists
            }

            try {
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN gateway_active_notify TINYINT(1) DEFAULT 1 NULL");
                System.out.println("Ran raw SQL migration: gateway_active_notify column ensured on 'users' table.");
            } catch (Exception e) {
                // Column probably already exists
            }

            // If table has existing records without emails or mobiles, update them
            try {
                jdbcTemplate.update("UPDATE users SET email = 'admin@enterprise.com', mobile = '+15551001', gateway_active_notify = 1 WHERE LOWER(id) = 'admin' AND (email IS NULL OR email = '')");
                jdbcTemplate.update("UPDATE users SET email = 'john.doe@enterprise.com', mobile = '+15551002', gateway_active_notify = 1 WHERE LOWER(id) = 'john.d' AND (email IS NULL OR email = '')");
                jdbcTemplate.update("UPDATE users SET email = 'sarah.miller@enterprise.com', mobile = '+15551003', gateway_active_notify = 1 WHERE LOWER(id) = 'sarah.m' AND (email IS NULL OR email = '')");
                jdbcTemplate.update("UPDATE users SET email = 'alpha.support@enterprise.com', mobile = '+15551004', gateway_active_notify = 1 WHERE LOWER(id) = 'support.alpha' AND (email IS NULL OR email = '')");
                System.out.println("Seeded emails/mobiles for default administrators and support staff.");
            } catch (Exception e) {
                System.out.println("Skipped default user detail update: " + e.getMessage());
            }

            // Write absolute database logs to secure and viewable /java-backend/db_debug_startup.json
            try {
                java.io.FileWriter writer = new java.io.FileWriter("/java-backend/db_debug_startup.json");
                writer.write("{\n");
                
                // Write columns
                writer.write("  \"columns\": [\n");
                try {
                    java.util.List<java.util.Map<String, Object>> cols = jdbcTemplate.queryForList("SHOW COLUMNS FROM users");
                    for (int i = 0; i < cols.size(); i++) {
                        java.util.Map<String, Object> col = cols.get(i);
                        writer.write("    {\"field\": \"" + col.get("Field") + "\", \"type\": \"" + col.get("Type") + "\"}" + (i < cols.size() - 1 ? "," : "") + "\n");
                    }
                } catch (Exception ex) {
                    writer.write("    {\"error\": \"" + ex.getMessage() + "\"}\n");
                }
                writer.write("  ],\n");

                // Write rows
                writer.write("  \"rows\": [\n");
                try {
                    java.util.List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM users");
                    for (int i = 0; i < rows.size(); i++) {
                        java.util.Map<String, Object> row = rows.get(i);
                        writer.write("    {");
                        java.util.List<String> entries = new java.util.ArrayList<>();
                        for (java.util.Map.Entry<String, Object> entry : row.entrySet()) {
                            entries.add("\"" + entry.getKey() + "\": \"" + (entry.getValue() != null ? entry.getValue().toString().replace("\"", "\\\"") : "null") + "\"");
                        }
                        writer.write(String.join(", ", entries));
                        writer.write("}" + (i < rows.size() - 1 ? "," : "") + "\n");
                    }
                } catch (Exception ex) {
                    writer.write("    {\"error\": \"" + ex.getMessage() + "\"}\n");
                }
                writer.write("  ]\n");
                writer.write("}\n");
                writer.close();
                System.out.println("DEBUG: Successfully wrote raw db state to /java-backend/db_debug_startup.json");
            } catch (Exception ex) {
                System.out.println("DEBUG ERROR: Could not write diagnostics file: " + ex.getMessage());
            }

            if (categoryMappingRepository.count() == 0) {
                System.out.println("Initializing sample categories and subcategories...");
                List<CategoryMapping> mappings = Arrays.asList(
                    // Incident
                    new CategoryMapping("Incident", "Application Issue"),
                    new CategoryMapping("Incident", "Production Issue"),
                    new CategoryMapping("Incident", "Infrastructure Issue"),
                    new CategoryMapping("Incident", "Service Failure"),
                    // Request
                    new CategoryMapping("Request", "Access Request"),
                    new CategoryMapping("Request", "Software Request"),
                    new CategoryMapping("Request", "Hardware Request"),
                    new CategoryMapping("Request", "Support Request"),
                    // Change
                    new CategoryMapping("Change", "Deployment"),
                    new CategoryMapping("Change", "Configuration Change"),
                    new CategoryMapping("Change", "Patch Release"),
                    new CategoryMapping("Change", "Emergency Change"),
                    // Development Task
                    new CategoryMapping("Development Task", "Requirement Analysis"),
                    new CategoryMapping("Development Task", "Issue Analysis"),
                    new CategoryMapping("Development Task", "Code Analysis"),
                    new CategoryMapping("Development Task", "Development"),
                    new CategoryMapping("Development Task", "Bug Fix"),
                    new CategoryMapping("Development Task", "Testing"),
                    new CategoryMapping("Development Task", "Code Review"),
                    new CategoryMapping("Development Task", "UAT Support"),
                    new CategoryMapping("Development Task", "Automation"),
                    new CategoryMapping("Development Task", "Integration"),
                    // Operational Task
                    new CategoryMapping("Operational Task", "Follow-up"),
                    new CategoryMapping("Operational Task", "Monitoring"),
                    new CategoryMapping("Operational Task", "Documentation"),
                    new CategoryMapping("Operational Task", "Coordination"),
                    new CategoryMapping("Operational Task", "Meetings"),
                    new CategoryMapping("Operational Task", "Validation"),
                    new CategoryMapping("Operational Task", "Reporting"),
                    new CategoryMapping("Operational Task", "Support Activity"),
                    // Enhancement
                    new CategoryMapping("Enhancement", "Improvement"),
                    new CategoryMapping("Enhancement", "New Feature"),
                    new CategoryMapping("Enhancement", "Revamp"),
                    // Problem
                    new CategoryMapping("Problem", "Problem Analysis"),
                    new CategoryMapping("Problem", "Recurring Issue"),
                    new CategoryMapping("Problem", "Performance Analysis"),
                    new CategoryMapping("Problem", "Root Cause Analysis"),
                    // Security
                    new CategoryMapping("Security", "Vulnerability"),
                    new CategoryMapping("Security", "Access Violation"),
                    new CategoryMapping("Security", "Security Incident")
                );
                categoryMappingRepository.saveAll(mappings);
            }

            if (userRepository.count() == 0) {
                System.out.println("Initializing sample users...");
                User u1 = new User("Admin", "Admin User", "Administrator", "Active", "root123");
                u1.setRecoveryQuestion("First pet's name?");
                u1.setRecoveryAnswer("buddy");
                u1.setEmail("admin@enterprise.com");
                u1.setMobile("+15550001");
                u1.setGatewayActiveNotify(true);

                User u2 = new User("John.D", "John Doe", "Support Specialist", "Active", "user123");
                u2.setRecoveryQuestion("Favorite color?");
                u2.setRecoveryAnswer("blue");
                u2.setEmail("john.doe@enterprise.com");
                u2.setMobile("+15550002");
                u2.setGatewayActiveNotify(true);

                User u3 = new User("Sarah.M", "Sarah Miller", "L2 Engineer", "Active", "user123");
                u3.setRecoveryQuestion("Birth city?");
                u3.setRecoveryAnswer("london");
                u3.setEmail("sarah.miller@enterprise.com");
                u3.setMobile("+15550003");
                u3.setGatewayActiveNotify(true);

                User u4 = new User("Support.Alpha", "Alpha Support", "Standard User", "Active", "user123");
                u4.setRecoveryQuestion("Favorite food?");
                u4.setRecoveryAnswer("pizza");
                u4.setEmail("alpha.support@enterprise.com");
                u4.setMobile("+15550004");
                u4.setGatewayActiveNotify(true);

                userRepository.saveAll(Arrays.asList(u1, u2, u3, u4));
            }

            if (projectRepository.count() == 0) {
                System.out.println("Initializing sample projects...");
                Project p1 = new Project("HR-Portal");
                p1.setDescription("Human Resources Management System");
                
                Project p2 = new Project("E-Commerce");
                p2.setDescription("Online Shopping Platform");
                
                Project p3 = new Project("Internal-CRM");
                p3.setDescription("Customer Relationship Management");
                
                Project p4 = new Project("Mobile-App");
                p4.setDescription("Native Android and iOS Apps");

                Project p5 = new Project("KAUST");
                p5.setDescription("King Abdullah University of Science and Technology Portal");

                Project p6 = new Project("GMAIL");
                p6.setDescription("Google Workspace Mail Gateway Integration");

                projectRepository.saveAll(Arrays.asList(p1, p2, p3, p4, p5, p6));
            } else {
                if (projectRepository.findByName("KAUST").isEmpty()) {
                    Project p = new Project("KAUST");
                    p.setDescription("King Abdullah University of Science and Technology Portal");
                    projectRepository.save(p);
                }
                if (projectRepository.findByName("GMAIL").isEmpty()) {
                    Project p = new Project("GMAIL");
                    p.setDescription("Google Workspace Mail Gateway Integration");
                    projectRepository.save(p);
                }
            }

            if (taskRepository.count() == 0) {
                System.out.println("Initializing sample data...");
                
                SupportTask task1 = new SupportTask();
                task1.setTicketId("INC-1001");
                task1.setProjectId("HR-Portal");
                task1.setSupportLevel("L1");
                task1.setPriority("P3");
                task1.setGenerationDate(LocalDateTime.now().minusDays(2));
                task1.setResponseDate(LocalDateTime.now().minusDays(2).plusHours(1));
                task1.setStatus("In-Progress");
                task1.setDescription("Cannot access salary slip module");
                task1.setAssignedTo("Sarah.M");
                task1.setCreatedBy("Admin");
                
                SupportTask task2 = new SupportTask();
                task2.setTicketId("INC-1002");
                task2.setProjectId("E-Commerce");
                task2.setSupportLevel("L2");
                task2.setPriority("P1");
                task2.setGenerationDate(LocalDateTime.now().minusDays(1));
                task2.setResponseDate(LocalDateTime.now().minusDays(1).plusMinutes(15));
                task2.setClosureDate(LocalDateTime.now().minusHours(2));
                task2.setStatus("Closed");
                task2.setUserIntimated(true);
                task2.setDescription("Checkout gateway timeout");
                task2.setSolution("Restarted payment service and cleared cache");
                task2.setRemarks("Issue resolved permanently");
                task2.setAssignedTo("Admin");
                task2.setCreatedBy("Sarah.M");

                taskRepository.saveAll(Arrays.asList(task1, task2));
                System.out.println("Sample data initialized.");
            }
        };
    }
}
