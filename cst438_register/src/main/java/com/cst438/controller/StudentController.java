package com.cst438.controller;

/*
 * Imports, not all required but may need in the future
 */

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Course;
import com.cst438.domain.CourseDTOG;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.ScheduleDTO;
import com.cst438.domain.Student;
import com.cst438.domain.StudentDTO;
import com.cst438.domain.StudentRepository;
import com.cst438.service.GradebookService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "https://registerf-cst438.herokuapp.com/"})
public class StudentController {
	
	/*
	 * Services, not all required but may need in the future
	 */
		
	@Autowired
	CourseRepository courseRepository;
	
	@Autowired
	StudentRepository studentRepository;
	
	@Autowired
	EnrollmentRepository enrollmentRepository;
	
	@Autowired
	GradebookService gradebookService;
	
	
	
	
	/*
	 * Check if admin
	 */
	@GetMapping("/admin")

	public void checkAdmin( @AuthenticationPrincipal OAuth2User principal  ) {
		
    	String adminEmail = principal.getAttribute("email");
        if (!isEmailAdmin(adminEmail)) {
            System.out.println("Not an administrator");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not an administrator");
        }
		return;
	}
	
		
	
	/*
	 * Get students
	 */
	@GetMapping("/student")

    public List<StudentDTO> getStudents() {
        Iterable<Student> students =studentRepository.findAll();

        List<StudentDTO> studentsDTO = new ArrayList<StudentDTO>();

        for (Student s: students) {
            studentsDTO.add(createStudentDTO(s));
        }

        return studentsDTO;
    }
	

	
	/*
	 * Delete student
	 */
	@DeleteMapping("/student/{student_id}")
	@Transactional
	public void dropStudent(  @PathVariable int student_id, @AuthenticationPrincipal OAuth2User principal  ) {
		
    	String adminEmail = principal.getAttribute("email");
        if (!isEmailAdmin(adminEmail)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");
        }

		
		Student student = studentRepository.findById(student_id).orElse(null);
		
		// verify that student is enrolled in the course.
		if (student!=null) {
			// OK.  drop the course.
			 studentRepository.deleteById(student_id);
		} else {
			// something is not right with the enrollment.  
			throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "Id invalid. "+student_id);
		}
	}
	
	
	
	
	/*
	 * Add student
	 */
	@PostMapping("/student")
		@Transactional
		public StudentDTO addStudent( @RequestBody StudentDTO studentDTO, @AuthenticationPrincipal OAuth2User principal  ) { 
		
    	String adminEmail = principal.getAttribute("email");
        if (!isEmailAdmin(adminEmail)) {
        	System.out.println("XXXXX");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");
        }
    	
    	System.out.println(adminEmail);

		Student student = studentRepository.findByEmail( studentDTO.email);
		
		if (student!=null) {
			System.out.println("student already registered. "+studentDTO.email);
			throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Student email already registered: " + studentDTO.email );
		} else {
			
			Student new_student = new Student();
			new_student.setName(studentDTO.name);
			new_student.setEmail(studentDTO.email);
		
			studentRepository.save(new_student);
			System.out.println("Student added: "+studentDTO.name);
			return createStudentDTO(new_student);
		}
	}
	
	
	/*
	 * Place hold on student
	 */
    @PostMapping("/student/place")
    @Transactional
    public StudentDTO placeStudentHold(@RequestBody StudentDTO studentDTO, @AuthenticationPrincipal OAuth2User principal) {
    	
    	String adminEmail = principal.getAttribute("email");
        if (!isEmailAdmin(adminEmail)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");
        }

        Student student = studentRepository.findByEmail(studentDTO.email);

        if (student != null) {
            student.setStatusCode(1);
            studentRepository.save(student);            
            return createStudentDTO(student);
            
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student email not found: " + studentDTO.email);
        }
    }
	
    
	/*
	 * Release hold on student
	 */
    @PostMapping("/student/release")
    @Transactional
    public StudentDTO releaseStudentHold(@RequestBody StudentDTO studentDTO, @AuthenticationPrincipal OAuth2User principal) {
    	
    	String adminEmail = principal.getAttribute("email");
        if (!isEmailAdmin(adminEmail)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");
        }
    	

        Student student = studentRepository.findByEmail(studentDTO.email);

        if (student != null) {
            student.setStatusCode(0);
            studentRepository.save(student);            
            return createStudentDTO(student);
            
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student email not found: " + studentDTO.email);
        }
    }

	/*
	 * Create Student DTO from Student
	 */
    private StudentDTO createStudentDTO(Student student) {
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.student_id = student.getStudent_id();
        studentDTO.name = student.getName();
        studentDTO.email = student.getEmail();
        studentDTO.status_code = student.getStatusCode();
        studentDTO.status = student.getStatus();
        return studentDTO;
    }
    

    
    // Check if instructor(admin) email
    private boolean isEmailAdmin(String email) {

        Course course = courseRepository.findByInstructor(email);
        if (course == null) {
            return false;
        }
        return true;
    }
	
		
}
