package com.borrowbox.config;

import com.borrowbox.model.Item;
import com.borrowbox.model.Student;
import com.borrowbox.repository.ItemRepository;
import com.borrowbox.repository.StudentRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoDataInitializer {
    @Bean
    CommandLineRunner loadDemoData(StudentRepository students, ItemRepository items) {
        return args -> {
            Student asha = students.findByEmail("asha@borrowbox.demo")
                    .orElseGet(() -> students.save(new Student("Asha", "CSE", "asha@borrowbox.demo")));
            Student rahul = students.findByEmail("rahul@borrowbox.demo")
                    .orElseGet(() -> students.save(new Student("Rahul", "ECE", "rahul@borrowbox.demo")));
            Student anjali = students.findByEmail("anjali@borrowbox.demo")
                    .orElseGet(() -> students.save(new Student("Anjali", "Mechanical", "anjali@borrowbox.demo")));

            if (items.count() == 0) {
                List<Item> demoItems = List.of(
                        new Item("Engineering Mathematics Textbook", "Books", "A well-kept reference for calculus, linear algebra and core engineering mathematics.", "Very good", "CSE block", 14, "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=960&q=85", asha),
                        new Item("Scientific Calculator", "Electronics", "Casio scientific calculator with fresh batteries. Great for exams and lab work.", "Excellent", "ECE block", 7, "https://images.unsplash.com/photo-1587145820266-a5951ee6f620?auto=format&fit=crop&w=960&q=85", rahul),
                        new Item("Lab Coat", "Lab equipment", "Clean, white lab coat in size M. Recently washed and ready for practicals.", "Good", "Mechanical block", 3, "https://images.unsplash.com/photo-1584467735871-bc577273e332?auto=format&fit=crop&w=960&q=85", anjali),
                        new Item("Engineering Drawing Set", "Tools", "Complete compass, divider, set squares and pencil set in a protective case.", "Very good", "CSE block", 5, "https://images.unsplash.com/photo-1513364776144-60967b0f800f?auto=format&fit=crop&w=960&q=85", asha),
                        new Item("Physics Laboratory Record", "Books", "Neatly maintained first-year physics practical record with all experiments completed.", "Good", "ECE block", 10, "https://images.unsplash.com/photo-1532012197267-da84d127e765?auto=format&fit=crop&w=960&q=85", rahul),
                        new Item("Data Structures Textbook", "Books", "Data structures and algorithms textbook with helpful notes in the margins.", "Good", "Mechanical block", 14, "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=960&q=85", anjali),
                        new Item("Vernier Caliper", "Tools", "Digital vernier caliper, tested and calibrated, with storage case.", "Excellent", "Mechanical block", 4, "https://images.unsplash.com/photo-1581092160562-40aa08e78837?auto=format&fit=crop&w=960&q=85", anjali),
                        new Item("Electronics Components Kit", "Electronics", "Starter components kit including resistors, LEDs, jumper wires and a mini breadboard.", "Very good", "ECE block", 7, "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=960&q=85", rahul)
                );
                items.saveAll(demoItems);
            }
        };
    }
}
