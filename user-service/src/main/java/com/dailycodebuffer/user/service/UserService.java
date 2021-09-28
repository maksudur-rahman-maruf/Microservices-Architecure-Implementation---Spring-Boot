package com.dailycodebuffer.user.service;

import com.dailycodebuffer.user.VO.Department;
import com.dailycodebuffer.user.VO.ResponseTemplateVO;
import com.dailycodebuffer.user.entity.User;
import com.dailycodebuffer.user.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    public User saveUser(User user) {
        log.info("Inside saveUser method of UserService");
        return userRepository.save(user);
    }

// Before Adding Circuit Breaker
//    public ResponseTemplateVO getUserWithDepartment(Long userId) {
//        log.info("Inside getUserWithDepartment method of UserService");
//        ResponseTemplateVO vo = new ResponseTemplateVO();
//        User user = userRepository.findByUserId(userId);
//
//        Department department =
//                restTemplate.getForObject("http://DEPARTMENT-SERVICE/departments/" + user.getDepartmentId(),
//                        Department.class);
//
//        vo.setUser(user);
//        vo.setDepartment(department);
//
//        return vo;
//    }

    @CircuitBreaker(name="userService", fallbackMethod = "userFallBack")
    @RateLimiter(name="userService")
//    @Retry(name="userService", fallbackMethod = "userFallBack")  // Retry Or (CircuitBreaker, RateLimiter)
    public ResponseEntity getUserWithDepartment(Long userId) {
        log.info("Inside getUserWithDepartment method of UserService");
        ResponseTemplateVO vo = new ResponseTemplateVO();
        User user = userRepository.findByUserId(userId);
        if(user == null) return new ResponseEntity("Can't find the user", HttpStatus.OK);

        Department department =
                restTemplate.getForObject("http://DEPARTMENT-SERVICE/departments/" + user.getDepartmentId(),
                        Department.class);

        vo.setUser(user);
        vo.setDepartment(department);

        return new ResponseEntity(vo, HttpStatus.OK);
    }

   public ResponseEntity userFallBack(Exception e) {
        return new ResponseEntity("Department Service is Down!!!", HttpStatus.NOT_FOUND);
   }
}
