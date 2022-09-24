package com.cst438.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Course;
import com.cst438.domain.CourseDTOG;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Student;
import com.cst438.domain.StudentRepository;



@RestController
@CrossOrigin(origins = {"http://localhost:8080","http://localhost:8081"})
public class CourseController {
	
	@Autowired
	EnrollmentRepository enrollmentRepository;
	
	@Autowired
	StudentRepository studentRepository;

	@Autowired
	CourseRepository courseRepository;
	

	@PutMapping("/course/{course_id}")
	@Transactional
	public void updateCourseGrades( @RequestBody CourseDTOG courseDTO, @PathVariable("course_id") int course_id) {
		
		// find course
		Course course = courseRepository.findByCourse_id(course_id);
		if (course == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course ID is invalid: '" + course_id + "'.");
		}

		for (CourseDTOG.GradeDTO gradeDTO : courseDTO.grades) {

			Student student = studentRepository.findByEmail(gradeDTO.student_email);
			if (student == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student email is invalid: '" + gradeDTO.student_email + "'.");
			}
			// update existing enrollment record
			Enrollment enrollment = enrollmentRepository.findByEmailAndCourseId(gradeDTO.student_email, course_id);
			enrollment.setCourseGrade(gradeDTO.grade);
			enrollmentRepository.save(enrollment);

		}
		
	}

}




